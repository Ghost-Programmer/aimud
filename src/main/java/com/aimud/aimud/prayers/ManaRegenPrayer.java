package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@DivinePrayer(name = "manaregen")
public class ManaRegenPrayer extends Prayer {

    protected ManaRegenPrayer(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Divine Mana Regen";
    }

    @Override
    public Long getPrayerId() {
        return 2010L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Accelerates mana regeneration over time through divine grace. Usage: pray manaregen [target]";
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
            effectName = "Mana Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Mana Regen +15";
        } else if (skillRank <= 75) {
            effectName = "Mana Regen +20";
        } else {
            effectName = "Mana Regen +25";
        }

        Effect manaRegenEffect = this.effectService.getEffectByName(effectName).block();

        if (manaRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour restorative prayer fails to find form.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getPrayerSkillName(), manaRegenEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nDivine clarity begins restoring %s's mana.",
                        mobile == target ? "your" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s surrounds you with restorative grace!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s glows softly with magical recovery.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
