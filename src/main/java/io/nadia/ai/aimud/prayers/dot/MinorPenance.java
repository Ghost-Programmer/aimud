package io.nadia.ai.aimud.prayers.dot;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;

/**
 * Implementation of the minorpenance prayer.
 */
@Component
@DivinePrayer(name = "minorpenance")
public class MinorPenance extends Prayer {

    /**
     * Constructs the minorpenance dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public MinorPenance(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() { return "Minor Penance"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() { return 2508L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() { return 4; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 4 Damage-Over-Time holy prayer. Usage: pray 'minorpenance' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target != null && !this.mobileService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6 + (rank / 10);

        String effectName = "Holy Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            this.applyEffect(target, mobile, this.getPrayerSkillName(), effect, duration);
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou brand " + target.getName() + " with Minor Penance.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " brands you with Minor Penance!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " brands " + target.getName() + " with Minor Penance.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}


