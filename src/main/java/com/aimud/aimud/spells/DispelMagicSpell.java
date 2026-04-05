package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.CharacterEffect;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.EffectType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MagicSpell(name = "dispel_magic")
public class DispelMagicSpell extends Spell {

    protected DispelMagicSpell(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Dispel Magic";
    }

    @Override
    public Long getSpellId() {
        return 1010L;
    }

    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Removes one negative magical effect or damage-over-time condition from the target. Usage: cast dispel_magic [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget() != null ? mobile.getTarget() : mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return 20;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
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
