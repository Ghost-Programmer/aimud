package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of the chainlightning spell.
 */
@Component
@MagicSpell(name = "chainlightning")
public class ChainLightning extends Spell {

    /**
     * Constructs the chainlightning dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public ChainLightning(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Chain Lightning";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1001L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 60;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Summons a devastating chain of electrical energy that arcs between multiple targets. Usage: cast chainlightning <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.mobileService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou need a target to initiate the chain lightning on.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, target);
        if (targets.isEmpty()) {
            this.communicationService.sendTextMessage(mobile, "\n\nThere is no one to strike with lightning.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        // Scales from 10d10 at rank 1 up to 50d10 at rank 100
        int numDice = 10 + Math.round((rank - 1) * 40.0f / 99.0f);
        int diceSize = 10;

        if (mobile.getUserId() != null) {
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou unleash a storm of chain lightning starting at %s!", target.getName()));
        }
        this.communicationService.roomMessage(mobile, String.format("\n\n%s unleashes chain lightning at %s, electricity arcing to everyone nearby!", mobile.getName(), target.getName()));

        for (Mobile tgt : targets) {
            int damage = new Dice(numDice, diceSize).getTotal();

            boolean resist = false;
            // minimal magic saving throw mechanic
            if (tgt.getMagicResist() > new Dice(1, 100).getTotal()) {
                damage = damage / 2;
                resist = true;
            }

            if (tgt.getUserId() != null) {
                if (resist) {
                    this.communicationService.sendTextMessage(tgt, String.format("\n\nYou resist %s's chain lightning, taking half damage!", mobile.getName()));
                } else {
                    this.communicationService.sendTextMessage(tgt, String.format("\n\nYou are electrocuted by %s's chain lightning for %d damage!", mobile.getName(), damage));
                }
            }

            tgt.setCurrentHp(tgt.getCurrentHp() - damage);

            int hateAmount = damage;
            if (tgt.isHateWizard()) hateAmount *= 5;
            tgt.addHate(mobile.getId(), hateAmount);
            if (mobile.getTarget() == null) {
                if (!this.mobileService.setTarget(mobile, tgt)) continue;
            }

            if (tgt.getCurrentHp() <= 0) {
                tgt.setCurrentHp(0);
                String deathMsg = "\n" + tgt.getName() + " is DEAD!!";
                if (mobile.getUserId() != null && mobile.getId() != tgt.getId()) {
                    this.communicationService.sendTextMessage(mobile, deathMsg);
                }
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\nYou have died...");
                    this.communicationService.sendTextMessage(tgt, deathMsg);
                }
                this.communicationService.roomMessage(tgt, deathMsg);

                this.mobileService.setTarget(tgt, null);
                if (mobile.getTarget() == tgt) {
                    this.mobileService.setTarget(mobile, null);
                }

                this.mobileService.findAllByRoomId(tgt.getCurrentRoomId())
                        .forEach(m -> m.removeHate(tgt.getId()));
            } else {
                if (tgt.getTarget() == null) {
                    if (!this.mobileService.setTarget(tgt, mobile)) continue;
                }
            }

            if (tgt.getUserId() != null) {
                 this.mobileService.save(tgt).subscribe();
            } else {
                 this.mobileService.saveMobile(tgt).subscribe();
            }
        }

        return true;
    }
}


