package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the complete healing prayer.
 */
@DivinePrayer(name = "complete healing")
public class CompleteHealing extends Prayer {

    /**
     * Constructs the complete healing dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected CompleteHealing(SkillService skillService, MobileService mobileService, CharacterService characterService,
                              CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() {
        return "Complete Healing";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() {
        return 2002L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() {
        return 90;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Completely restores all hit points to the target. Usage: pray complete healing [target]";
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
        int castSkill = skillService.getSkillRank(mobile, SkillsType.SAY_PRAYER);
        return Math.max(0, 1000 - (castSkill * 5));
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

        if (target == null) {
            target = mobile;
        }

        int actualHeal = target.getMaxHp() - target.getCurrentHp();
        target.setCurrentHp(target.getMaxHp());
        characterService.save(target).subscribe();

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, 
                String.format("\n\nYou invoke divine power onto %s, completely restoring their health by %d points.", 
                mobile == target ? "yourself" : target.getName(), actualHeal));
        }

        if (target.getUserId() != null && mobile != target) {
            communicationService.sendTextMessage(target,
                String.format("\n\n%s invokes pure divine power onto you, completely restoring your health!", 
                mobile.getName()));
        }

        return true;
    }
}

