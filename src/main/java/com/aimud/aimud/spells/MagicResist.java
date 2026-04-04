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

@MagicSpell(name = "mresist")
public class MagicResist extends Spell {

    protected MagicResist(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                          CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Magic Resist";
    }

    @Override
    public Long getSpellId() {
        return 1004L;
    }

    @Override
    public Integer getSpellLevel() {
        return 12;
    }

    @Override
    public String getDescription() {
        return "Hardens your aura against magical damage. Usage: cast mresist";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        if (spellSkill <= 51) {
            return 35 + (spellSkill / 5);
        } else if (spellSkill <= 75) {
            return 110;
        }
        return 160;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 5;

        String effectName;
        if (skillRank <= 51) {
            effectName = "Magic Resist +1";
        } else if (skillRank <= 75) {
            effectName = "Magic Resist +5";
        } else {
            effectName = "Magic Resist +10";
        }

        Effect magicResistEffect = effectService.getEffectByName(effectName).block();
        if (magicResistEffect == null) {
            if (mobile instanceof Mobile) {
                communicationService.sendTextMessage((Mobile) mobile, "\n\nYour warding chant fades before it forms.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), magicResistEffect, tickCount)) {
            if (mobile instanceof Mobile) {
                communicationService.sendTextMessage((Mobile) mobile,
                        String.format("\n\nA shimmering ward settles over %s.", mobile == target ? "you" : target.getName()));
            }

            if (target instanceof Mobile && mobile != target) {
                communicationService.sendTextMessage((Mobile) target,
                        String.format("\n\n%s wraps you in a ward against magic!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s is briefly outlined by a protective arcane veil.", mobile.getName()));
            return true;
        }

        return false;
    }
}


