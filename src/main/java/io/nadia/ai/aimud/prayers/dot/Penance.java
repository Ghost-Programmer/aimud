package io.nadia.ai.aimud.prayers.dot;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;

/**
 * Implementation of the penance prayer.
 */
@Component
@DivinePrayer(name = "penance")
public class Penance extends Prayer {

    /**
     * Constructs the penance dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public Penance(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() { return "Penance"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() { return 2509L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() { return 20; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 20 Damage-Over-Time holy prayer. Usage: pray 'penance' <target>";
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
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou brand " + target.getName() + " with Penance.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " brands you with Penance!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " brands " + target.getName() + " with Penance.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}

