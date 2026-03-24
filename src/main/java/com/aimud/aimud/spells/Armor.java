package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.CharacterEffect;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.SkillsType;

@MagicSpell(name = "armor")
public class Armor extends Spell {


    protected Armor(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Magic Armor";
    }

    @Override
    public Long getSpellId() {
        return 1001L;
    }

    @Override
    public Integer getSpellLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Protects the caster from physical attacks by increasing their armor class. Usage: cast armor ";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        int cost;

        if (spellSkill <= 51) {
            cost = 25 + (spellSkill / 5);
        } else if (spellSkill <= 61) {
            cost = 125;
        } else if (spellSkill <= 75) {
            cost = 175;
        } else {
            cost = 250;
        }

        return cost;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 5;
        int duration = 10 + skillRank;

        String effectName;

        if (skillRank <= 51) {
            effectName = "Armor +" + ((skillRank / 5)+1);
        } else if (skillRank <= 61) {
            effectName = "Armor +15";
        } else if (skillRank <= 75) {
            effectName = "Armor +20";
        } else {
            effectName = "Armor +25";
        }

        Effect armorEffect = this.effectService.getEffectByName(effectName).block();

        if( this.applyEffect(mobile, this.getSpellSkillName(), armorEffect, tickCount)){

            if (mobile instanceof Character) {
                this.communicationService.sendTextMessage((Character) mobile, String.format("\n\nYou feel a shimmering field of force surround %s.",
                        mobile == target ? "you" : target.getName()));
            }

            if (target instanceof Character && mobile != target) {
                this.communicationService.sendTextMessage((Character) target, String.format("\n\n%s protects you with a magical suit of armor!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s's skin shimmers briefly as a magical field of armor forms around %s.",
                    mobile.getName(), mobile == target ? "them" : target.getName()));

        }
        return true;
    }
}
