package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.EffectType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DivinePrayer(name = "dispel_magic")
public class DispelMagicPrayer extends Prayer {

    protected DispelMagicPrayer(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Dispel Magic";
    }

    @Override
    public Long getPrayerId() {
        return 2005L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Purifies one negative magical effect or damage-over-time condition from the target. Usage: pray dispel_magic [target]";
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
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
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
            
            String effectName = effectToRemove.getName() != null ? effectToRemove.getName() : "a malicious curse";

            if (mobile.getUserId() != null) {
                 communicationService.sendTextMessage(mobile, String.format("\n\nYour holy aura purifies %s from %s.", 
                    effectName, mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                 communicationService.sendTextMessage(target, String.format("\n\nA blinding flash from %s purifies %s from you.", 
                    mobile.getName(), effectName));
            }

            communicationService.roomMessage(mobile, String.format("\n\nHoly light cleanses the shadows clinging to %s.", 
                mobile == target ? mobile.getName() : target.getName()));

            return true;
        }

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYour prayer finds no corrupting enchantments to cleanse.");
        }
        return false;
    }
}
