package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final EffectService effectService;

    public ItemService(ItemRepository itemRepository, EffectService effectService) {
        this.itemRepository = itemRepository;
        this.effectService = effectService;
    }

    public Flux<Item> getAllItems() {
        log.info("Fetching all items");
        return itemRepository.findAll()
                .flatMap(this::loadEffectsAndValue);
    }

    public Mono<Item> getItem(Long id) {
        log.info("Fetching item with id: {}", id);
        return itemRepository.findById(id)
                .flatMap(this::loadEffectsAndValue);
    }

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

    public Mono<Item> saveItem(Item item) {
        log.info("Saving item: {} (id: {})", item.getName(), item.getId());
        List<Effect> effects = item.getEffects();
        return itemRepository.save(item)
                .flatMap(savedItem -> {
                    if (effects == null) { // Allow empty list to clear effects
                        log.debug("Effects list is null, keeping existing or doing nothing?");
                        // If null, maybe we shouldn't touch effects? Or treat as empty?
                        // For now, let's treat null as "don't change effects" if we want, or "clear".
                        // Given previous logic was clearing if null/empty, let's stick to that but be careful.
                        // Actually, previous logic was: if null or empty, return savedItem. Which means it didn't clear them?
                        // "if (effects == null || effects.isEmpty()) { ... return Mono.just(savedItem); }"
                        // This implies if you send empty list, it DOES NOT clear existing effects in DB. That seems wrong for an update.
                        // But let's follow the previous pattern for now to avoid regression, but fixed the ID reuse.
                        
                        // Wait, if I want to remove effects, I send empty list. If the code returns early, I can't remove effects.
                        // The previous code:
                        /*
                        if (effects == null || effects.isEmpty()) {
                            return Mono.just(savedItem);
                        }
                        return effectService.deleteByItemId(...)
                        */
                        // So sending empty list meant "do nothing to effects". This is weird for a PUT.
                        // I will keep it for now but maybe I should fix it later.
                        return Mono.just(savedItem);
                    }
                    
                    log.debug("Saving {} effects for item: {}", effects.size(), savedItem.getId());
                    return effectService.deleteByItemId(savedItem.getId())
                            .thenMany(Flux.fromIterable(effects))
                            .flatMap(effect -> {
                                if (effect.getId() != null) {
                                    // Link existing effect
                                    return effectService.linkItemAndEffect(savedItem.getId(), effect.getId())
                                            .then(effectService.getEffect(effect.getId())); // Return the effect for the list
                                } else {
                                    // Create new effect and link
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

    public Mono<Void> deleteItem(Long id) {
        log.info("Deleting item with id: {}", id);
        return effectService.deleteByItemId(id)
                .then(itemRepository.deleteById(id));
    }

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

        // If it's a negative modifier, it might reduce the value (but we used Math.abs above for the calculation)
        // Let's adjust so negative modifiers reduce value
        if (modifier < 0 && !isStatusEffect(effect)) {
            return -effectValue / 2;
        }

        return effectValue;
    }

    private boolean isStatusEffect(Effect effect) {
        return switch (effect.getEffectType()) {
            case FLY, WATER_BREATHING, INVISIBLE -> true;
            default -> false;
        };
    }
}
