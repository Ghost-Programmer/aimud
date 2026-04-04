package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@MagicSpell(name = "hpregen")
public class HpRegen extends Spell {

    protected HpRegen(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                      CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "HP Regen";
    }

    @Override
    public Long getSpellId() {
        return 1003L;
    }

    @Override
    public Integer getSpellLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Accelerates health regeneration over time. Usage: cast hpregen";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        if (spellSkill <= 51) {
            return 25 + (spellSkill / 5);
        } else if (spellSkill <= 61) {
            return 125;
        } else if (spellSkill <= 75) {
            return 175;
        }
        return 250;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 5;

        String effectName;
        if (skillRank <= 51) {
            effectName = "HP Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "HP Regen +15";
        } else if (skillRank <= 75) {
            effectName = "HP Regen +20";
        } else {
            effectName = "HP Regen +25";
        }

        Effect hpRegenEffect = effectService.getEffectByName(effectName).block();
        if (hpRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour healing prayer fails to find form.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), hpRegenEffect, tickCount)) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile,
                        String.format("\n\nWarm vitality begins mending %s.", mobile == target ? "you" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                communicationService.sendTextMessage(target,
                        String.format("\n\n%s surrounds you with restorative magic!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s glows briefly with restorative energy.", mobile.getName()));
            return true;
        }

        return false;
    }
}


