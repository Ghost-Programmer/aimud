package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.EffectType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * Implementation of the dispel_magic spell.
 */
@MagicSpell(name = "dispel_magic")
public class DispelMagicSpell extends Spell {

    /**
     * Constructs the dispel_magic dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected DispelMagicSpell(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Dispel Magic";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1002L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Removes one negative magical effect or damage-over-time condition from the target. Usage: cast dispel_magic [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget() != null ? mobile.getTarget() : mobile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getManaCost(Mobile mobile) {
        return 20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            target = getDefaultTarget(mobile);
            if (target == null) {
                communicationService.sendTextMessage(mobile, "\n\nYou have no valid target.");
                return false;
            }
        }

        CharacterEffect effectToRemove = null;
        
        for (CharacterEffect ce : target.getSpellEffects()) {
            Effect effect = ce.getEffect();
            if (effect == null) continue;

            EffectType type = effect.getEffectType();
            
            boolean isDoT = (type == EffectType.FIRE_DAMAGE ||
                             type == EffectType.COLD_DAMAGE ||
                             type == EffectType.ELECTRICAL_DAMAGE ||
                             type == EffectType.POISON_DAMAGE ||
                             type == EffectType.SONIC_DAMAGE ||
                             type == EffectType.BASHING_DAMAGE ||
                             type == EffectType.PIERCING_DAMAGE ||
                             type == EffectType.SLASHING_DAMAGE);
            
            boolean isDebuff = effect.getModifier1() < 0;

            if (isDoT || isDebuff) {
                effectToRemove = ce;
                break;
            }
        }

        if (effectToRemove != null) {
            effectService.removeCharacterEffectFromMobile(target, effectToRemove).subscribe();
            
            String effectName = effectToRemove.getName() != null ? effectToRemove.getName() : "a malicious spell";

            if (mobile.getUserId() != null) {
                 communicationService.sendTextMessage(mobile, String.format("\n\nYou successfully dispel %s from %s.", 
                    effectName, mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                 communicationService.sendTextMessage(target, String.format("\n\n%s waves their hand and dispels %s from you.", 
                    mobile.getName(), effectName));
            }

            communicationService.roomMessage(mobile, String.format("\n\nA dark aura suddenly disperses from %s.", 
                mobile == target ? mobile.getName() : target.getName()));

            return true;
        }

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYour dispel magic finds no negative enchantments to remove.");
        }
        return false;
    }
}

