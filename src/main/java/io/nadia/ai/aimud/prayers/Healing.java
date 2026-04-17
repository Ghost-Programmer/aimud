package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

/**
 * Implementation of the healing prayer.
 */
@DivinePrayer(name = "healing")
public class Healing extends Prayer {

    /**
     * Constructs the healing dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Healing(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                      CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() {
        return "Healing";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() {
        return 2006L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() {
        return 25;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Heals a standard amount of hit points. Usage: pray healing [target]";
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
        return super.getManaCost(mobile) * 10;
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

        int healAmount = getDamage(mobile) * 25; 

        int newHp = Math.min(target.getMaxHp(), target.getCurrentHp() + healAmount);
        int actualHeal = newHp - target.getCurrentHp();
        
        target.setCurrentHp(newHp);
        characterService.save(target).subscribe();

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, 
                String.format("\n\nYou lay your hands on %s and heal %d points of damage.", 
                mobile == target ? "yourself" : target.getName(), actualHeal));
        }

        if (target.getUserId() != null && mobile != target) {
            communicationService.sendTextMessage(target,
                String.format("\n\n%s lays their hands on you and heals %d points of damage.", 
                mobile.getName(), actualHeal));
        }

        return true;
    }
}

