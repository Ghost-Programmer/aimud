package io.nadia.ai.aimud.spells.dot.aoe;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.spells.Spell;
import java.util.List;

/**
 * Implementation of the embercloud spell.
 */
@MagicSpell(name = "embercloud")
public class EmberCloud extends Spell {

    /**
     * Constructs the embercloud dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public EmberCloud(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Ember Cloud";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1100L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 14;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getManaCost(Mobile mobile) {
        return super.getManaCost(mobile) * 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "An AoE level 14 spell that unleashes Ember Cloud on targets.. Damage and duration scale with skill rank. Usage: cast 'embercloud' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile primaryTarget) {
        if (primaryTarget != null && !this.mobileService.canTarget(primaryTarget)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + primaryTarget.getName() + ".");
            return false;
        }

        if (primaryTarget == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            }
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, primaryTarget);
        if (targets.isEmpty()) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nThere is no one to affect.");
            }
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        
        int numDice = Math.max(1, Math.min(10, (rank / 10) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6;

        String effectName = "Fire Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou unleash a massive blast of Ember Cloud starting at " + primaryTarget.getName() + "!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " unleashes a massive blast of Ember Cloud at " + primaryTarget.getName() + "!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getSpellSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " engulfs you in Ember Cloud!");
                }
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYour spell fizzles.");
            }
            return false;
        }
    }
}


