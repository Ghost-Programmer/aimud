package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.EffectService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.types.SkillsType;

@MagicSpell(name = "manaregen")
public class ManaRegen extends Spell {

    protected ManaRegen(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                        CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Mana Regen";
    }

    @Override
    public Long getSpellId() {
        return 1002L;
    }

    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Accelerates mana recovery over time. Usage: cast manaregen";
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
            effectName = "Mana Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Mana Regen +15";
        } else if (skillRank <= 75) {
            effectName = "Mana Regen +20";
        } else {
            effectName = "Mana Regen +25";
        }

        Effect manaRegenEffect = effectService.getEffectByName(effectName).block();
        if (manaRegenEffect == null) {
            if (mobile instanceof Mobile) {
                communicationService.sendTextMessage((Mobile) mobile, "\n\nThe weave fizzles. No matching mana regeneration effect exists.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), manaRegenEffect, tickCount)) {
            if (mobile instanceof Mobile) {
                communicationService.sendTextMessage((Mobile) mobile,
                        String.format("\n\nA calm pulse of arcane energy settles over %s.", mobile == target ? "you" : target.getName()));
            }

            if (target instanceof Mobile && mobile != target) {
                communicationService.sendTextMessage((Mobile) target,
                        String.format("\n\n%s blesses you with flowing mana!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s is surrounded by a steady arcane shimmer.", mobile.getName()));
            return true;
        }

        return false;
    }
}


