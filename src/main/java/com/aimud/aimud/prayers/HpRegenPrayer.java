package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@DivinePrayer(name = "hpregen")
public class HpRegenPrayer extends Prayer {

    protected HpRegenPrayer(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Divine HP Regen";
    }

    @Override
    public Long getPrayerId() {
        return 2007L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Accelerates health regeneration over time through divine grace. Usage: pray hpregen [target]";
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
            effectName = "HP Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "HP Regen +15";
        } else if (skillRank <= 75) {
            effectName = "HP Regen +20";
        } else {
            effectName = "HP Regen +25";
        }

        Effect hpRegenEffect = this.effectService.getEffectByName(effectName).block();

        if (hpRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour healing prayer fails to find form.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getPrayerSkillName(), hpRegenEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nWarm vitality begins mending %s.",
                        mobile == target ? "you" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s surrounds you with restorative grace!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s glows briefly with restorative energy.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
