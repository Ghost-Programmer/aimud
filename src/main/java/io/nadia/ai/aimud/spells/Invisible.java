package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

/**
 * Implementation of the invisible spell.
 */
@MagicSpell(name = "invisible")
public class Invisible extends Spell {

    /**
     * Constructs the invisible dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Invisible(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Invisible";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1006L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 15;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Renders the target invisible to the naked eye. Usage: cast invisible [target]";
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
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            target = mobile;
        }

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = Math.max(1, skillRank) * 10;

        Effect invisEffect = this.effectService.getEffectByName("Invisible").block();

        if (invisEffect == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nThe magic fails to take hold.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSpellSkillName(), invisEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou fade %s from view.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s casts a spell, and you fade into invisibility!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s fades rapidly from sight until they are completely invisible.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}

