package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@DivinePrayer(name = "charisma")
public class Charisma extends Prayer {

    protected Charisma(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Divine Charisma";
    }

    @Override
    public Long getPrayerId() {
        return 2001L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 4;
    }

    @Override
    public String getDescription() {
        return "Blesses the target with divine charisma. Usage: pray charisma [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int prayerSkill = skillService.getSkillRank(mobile, getPrayerSkillName());
        int cost;

        if (prayerSkill <= 51) {
            cost = 25 + (prayerSkill / 5);
        } else if (prayerSkill <= 61) {
            cost = 125;
        } else if (prayerSkill <= 75) {
            cost = 175;
        } else {
            cost = 250;
        }

        return cost;
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        int skillRank = skillService.getSkillRank(mobile, getPrayerSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER) + 5;

        String effectName;

        if (skillRank <= 51) {
            effectName = "Charisma +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Charisma +15";
        } else if (skillRank <= 75) {
            effectName = "Charisma +20";
        } else {
            effectName = "Charisma +25";
        }

        Effect chaEffect = this.effectService.getEffectByName(effectName).block();

        if (chaEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour prayer goes unanswered.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getPrayerSkillName(), chaEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou bless %s with divine charisma.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s blesses you with divine charisma!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s appears more majestic and commanding.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
