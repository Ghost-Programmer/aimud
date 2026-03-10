package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.repository.ItemRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final EffectService effectService;

    public ItemService(ItemRepository itemRepository, EffectService effectService) {
        this.itemRepository = itemRepository;
        this.effectService = effectService;
    }

    public Flux<Item> getAllItems() {
        return itemRepository.findAll()
                .flatMap(this::loadEffectsAndValue);
    }

    @Cacheable(value = "items", key = "#id")
    public Mono<Item> getItem(Long id) {
        return itemRepository.findById(id)
                .flatMap(this::loadEffectsAndValue);
    }

    private Mono<Item> loadEffectsAndValue(Item item) {
        return effectService.getEffectsByItem(item.getId())
                .collectList()
                .map(effects -> {
                    item.setEffects(effects);
                    item.setValue(calculateItemValue(item));
                    return item;
                });
    }

    @CachePut(value = "items", key = "#item.id", condition = "#item.id != null")
    public Mono<Item> saveItem(Item item) {
        List<Effect> effects = item.getEffects();
        return itemRepository.save(item)
                .flatMap(savedItem -> {
                    if (effects == null || effects.isEmpty()) {
                        return Mono.just(savedItem);
                    }
                    return effectService.deleteByItemId(savedItem.getId())
                            .thenMany(Flux.fromIterable(effects))
                            .flatMap(effect -> {
                                effect.setId(null);
                                effect.setItemId(savedItem.getId());
                                return effectService.saveEffect(effect);
                            })
                            .collectList()
                            .map(savedEffects -> {
                                savedItem.setEffects(savedEffects);
                                savedItem.setValue(calculateItemValue(savedItem));
                                return savedItem;
                            });
                });
    }

    @CacheEvict(value = "items", key = "#id")
    public Mono<Void> deleteItem(Long id) {
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
