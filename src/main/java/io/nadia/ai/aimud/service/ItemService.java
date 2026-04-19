package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final EffectService effectService;

    /**
     * Constructs a new ItemService.
     *
     * @param itemRepository the item repository
     * @param effectService  the effect service
     */
    public ItemService(ItemRepository itemRepository, EffectService effectService) {
        this.itemRepository = itemRepository;
        this.effectService = effectService;
    }

    /**
     * Retrieves all items from the database, loads their effects and calculates their value.
     *
     * @return a {@link Flux} emitting all items
     */
    @Cacheable(value = "items")
    public Flux<Item> getAllItems() {
        log.info("Fetching all items");
        return itemRepository.findAll()
                .collectList()
                .flatMapMany(items -> {
                    java.util.List<Long> itemIds = items.stream().map(Item::getId).toList();
                    return effectService.getEffectsByItemIds(itemIds)
                            .collectList()
                            .map(itemEffects -> {
                                java.util.Map<Long, java.util.List<Effect>> effectsByItemId = itemEffects.stream()
                                        .collect(java.util.stream.Collectors.groupingBy(
                                                io.nadia.ai.aimud.model.ItemEffectDTO::itemId,
                                                java.util.stream.Collectors.mapping(io.nadia.ai.aimud.model.ItemEffectDTO::effect, java.util.stream.Collectors.toList())
                                        ));
                                for (Item item : items) {
                                    item.setEffects(effectsByItemId.getOrDefault(item.getId(), java.util.List.of()));
                                    item.setValue(calculateItemValue(item));
                                }
                                return items;
                            });
                })
                .flatMapIterable(items -> items)
                .cache();
    }

    /**
     * Retrieves a specific item by its ID, loading its effects and calculating its value.
     *
     * @param id the ID of the item
     * @return a {@link Mono} containing the item
     */
    @Cacheable(value = "item", key = "#id")
    public Mono<Item> getItem(Long id) {
        log.info("Fetching item with id: {}", id);
        return itemRepository.findById(id)
                .flatMap(this::loadEffectsAndValue)
                .cache();
    }

    /**
     * Internal method to load effects for an item and calculate its total value.
     *
     * @param item the item to populate
     * @return a {@link Mono} containing the populated item
     */
    private Mono<Item> loadEffectsAndValue(Item item) {
        log.debug("Loading effects for item: {} (id: {})", item.getName(), item.getId());
        return effectService.getEffectsByItem(item.getId())
                .collectList()
                .map(effects -> {
                    item.setEffects(effects);
                    item.setValue(calculateItemValue(item));
                    return item;
                });
    }

    /**
     * Saves an item to the database, along with any associated effects.
     * Evicts item caches upon completion.
     *
     * @param item the item to save
     * @return a {@link Mono} containing the saved item
     */
    @CacheEvict(value = {"items", "item"}, allEntries = true)
    public Mono<Item> saveItem(Item item) {
        log.info("Saving item: {} (id: {})", item.getName(), item.getId());
        List<Effect> effects = item.getEffects();
        return itemRepository.save(item)
                .flatMap(savedItem -> {
                    log.info("Item saved with id: {}. Processing effects...", savedItem.getId());
                    if (effects == null) {
                        return Mono.just(savedItem);
                    }

                    log.info("Saving {} effects for item: {}", effects.size(), savedItem.getId());
                    return effectService.deleteByItemId(savedItem.getId())
                            .thenMany(Flux.fromIterable(effects))
                            .flatMap(effect -> {
                                if (effect.getId() != null) {
                                    return effectService.linkItemAndEffect(savedItem.getId(), effect.getId())
                                            .then(effectService.getEffect(effect.getId()));
                                } else {
                                    return effectService.saveEffect(effect)
                                            .flatMap(savedEffect -> effectService.linkItemAndEffect(savedItem.getId(), savedEffect.getId()).thenReturn(savedEffect));
                                }
                            })
                            .collectList()
                            .map(savedEffects -> {
                                savedItem.setEffects(savedEffects);
                                savedItem.setValue(calculateItemValue(savedItem));
                                return savedItem;
                            });
                });
    }

    /**
     * Deletes an item and cleans up its associated effect links.
     *
     * @param id the ID of the item to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"items", "item"}, allEntries = true)
    public Mono<Void> deleteItem(Long id) {
        log.info("Deleting item with id: {}", id);
        return effectService.deleteByItemId(id)
                .then(itemRepository.deleteById(id));
    }

    /**
     * Calculates the total value of an item based on a base value and its attached effects.
     *
     * @param item the item to evaluate
     * @return the calculated value in gold
     */
    public int calculateItemValue(Item item) {
        if (item == null) {
            return 0;
        }

        int value = 50; // Base value for any item

        if (item.getEffects() != null) {
            for (Effect effect : item.getEffects()) {
                if (effect.getEffectType() != null) {
                    value += calculateEffectValue(effect);
                }
            }
        }

        return Math.max(1, value); // Ensure item value is at least 1 gold
    }

    /**
     * Internal method to calculate the gold value contribution of a specific effect.
     *
     * @param effect the effect to evaluate
     * @return the calculated value modifier
     */
    private int calculateEffectValue(Effect effect) {
        int modifier = effect.getModifier1();
        int effectValue = 0;

        switch (effect.getEffectType()) {
            case STRENGTH:
            case DEXTERITY:
            case CONSTITUTION:
            case INTELLIGENCE:
            case WISDOM:
            case CHARISMA:
                effectValue = Math.abs(modifier) * 100;
                break;
            case ARMOR:
                effectValue = Math.abs(modifier) * 50;
                break;
            case PHYSICAL_ATTACK:
            case MAGIC_ATTACK:
                effectValue = Math.abs(modifier) * 75;
                break;
            case HP_REGEN:
            case MANA_REGEN:
                effectValue = Math.abs(modifier) * 150;
                break;
            case MAGIC_RESIST:
            case DODGE:
            case CRITICAL_HIT:
                effectValue = Math.abs(modifier) * 200;
                break;
            case FIRE_DAMAGE:
            case COLD_DAMAGE:
            case SONIC_DAMAGE:
            case POISON_DAMAGE:
            case ELECTRICAL_DAMAGE:
            case SLASHING_DAMAGE:
            case BASHING_DAMAGE:
            case PIERCING_DAMAGE:
                effectValue = Math.abs(modifier) * 125;
                break;
            case FLY:
            case WATER_BREATHING:
            case INVISIBLE:
                effectValue = 1000;
                break;
            default:
                effectValue = Math.abs(modifier) * 10;
                break;
        }

        if (modifier < 0 && !isStatusEffect(effect)) {
            return -effectValue / 2;
        }

        return effectValue;
    }

    /**
     * Determines whether an effect is considered a status effect (which shouldn't penalize value).
     *
     * @param effect the effect to check
     * @return true if it's a status effect, false otherwise
     */
    private boolean isStatusEffect(Effect effect) {
        return switch (effect.getEffectType()) {
            case FLY, WATER_BREATHING, INVISIBLE -> true;
            default -> false;
        };
    }
}
