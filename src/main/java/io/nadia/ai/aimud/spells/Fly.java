package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

/**
 * Implementation of the fly spell.
 */
@MagicSpell(name = "fly")
public class Fly extends Spell {

    /**
     * Constructs the fly dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Fly(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Fly";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1004L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Grants the target the ability to fly through the air. Usage: cast fly [target]";
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
        return 50;
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
            target = mobile;
        }

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = Math.max(1, skillRank) * 10;

        Effect flyEffect = this.effectService.getEffectByName("Fly").block();

        if (flyEffect == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nThe magic fails to take hold.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSpellSkillName(), flyEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou lift %s off the ground.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s casts a spell and your feet lift from the earth!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s suddenly lifts off the ground and begins to hover.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}


