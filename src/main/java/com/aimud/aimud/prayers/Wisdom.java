package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@DivinePrayer(name = "wisdom")
public class Wisdom extends Prayer {

    protected Wisdom(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Divine Wisdom";
    }

    @Override
    public Long getPrayerId() {
        return 2013L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Blesses the target with divine wisdom. Usage: pray wisdom [target]";
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
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        int skillRank = skillService.getSkillRank(mobile, getPrayerSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER) + 5;

        String effectName;

        if (skillRank <= 51) {
            effectName = "Wisdom +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Wisdom +15";
        } else if (skillRank <= 75) {
            effectName = "Wisdom +20";
        } else {
            effectName = "Wisdom +25";
        }

        Effect wisdomEffect = this.effectService.getEffectByName(effectName).block();

        if (wisdomEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour prayer goes unanswered.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getPrayerSkillName(), wisdomEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou bless %s with divine wisdom.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s blesses you with divine wisdom!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s looks more enlightened.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
