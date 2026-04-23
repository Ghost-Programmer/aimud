package io.nadia.ai.aimud.prayers.dot.aoe;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Implementation of the holyground prayer.
 */
@Component
@DivinePrayer(name = "holyground")
public class HolyGround extends Prayer {

    /**
     * Constructs the holyground dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public HolyGround(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() { return "Holy Ground"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() { return 2513L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() { return 30; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getManaCost(Mobile mobile) { return super.getManaCost(mobile) * 2; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 30 AoE Damage-Over-Time holy prayer. Usage: pray 'holyground' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile primaryTarget) {
        if (primaryTarget != null && !this.mobileService.canTarget(primaryTarget)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + primaryTarget.getName() + ".");
            return false;
        }

        if (primaryTarget == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, primaryTarget);
        if (targets.isEmpty()) return false;

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6 + (rank / 10);

        String effectName = "Holy Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou engulf the area around " + primaryTarget.getName() + " with Holy Ground!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " casts a crushing Holy Ground centered on " + primaryTarget.getName() + "!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getPrayerSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " burns you with Holy Ground over time!");
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}


