package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

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
