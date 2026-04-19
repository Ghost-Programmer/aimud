package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the intelligence prayer.
 */
@DivinePrayer(name = "intelligence")
public class Intelligence extends Prayer {

    /**
     * Constructs the intelligence dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Intelligence(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() {
        return "Divine Intelligence";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() {
        return 2008L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() {
        return 20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Blesses the target with divine intelligence. Usage: pray intelligence [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
            effectName = "Intelligence +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Intelligence +15";
        } else if (skillRank <= 75) {
            effectName = "Intelligence +20";
        } else {
            effectName = "Intelligence +25";
        }

        Effect intelligenceEffect = this.effectService.getEffectByName(effectName).block();

        if (intelligenceEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour prayer goes unanswered.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getPrayerSkillName(), intelligenceEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou bless %s with divine intelligence.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s blesses you with divine intelligence!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s's eyes sparkle with newfound brilliance.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}

