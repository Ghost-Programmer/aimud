package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the mresist spell.
 */
@MagicSpell(name = "mresist")
public class MagicResist extends Spell {

    /**
     * Constructs the mresist dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected MagicResist(SkillService skillService, MobileService mobileService,
                          CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Magic Resist";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1009L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 12;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Hardens your aura against magical damage. Usage: cast mresist";
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
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        if (spellSkill <= 51) {
            return 35 + (spellSkill / 5);
        } else if (spellSkill <= 75) {
            return 110;
        }
        return 160;
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

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 5;

        String effectName;
        if (skillRank <= 51) {
            effectName = "Magic Resist +1";
        } else if (skillRank <= 75) {
            effectName = "Magic Resist +5";
        } else {
            effectName = "Magic Resist +10";
        }

        Effect magicResistEffect = effectService.getEffectByName(effectName).block();
        if (magicResistEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour warding chant fades before it forms.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), magicResistEffect, tickCount)) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile,
                        String.format("\n\nA shimmering ward settles over %s.", mobile == target ? "you" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                communicationService.sendTextMessage(target,
                        String.format("\n\n%s wraps you in a ward against magic!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s is briefly outlined by a protective arcane veil.", mobile.getName()));
            return true;
        }

        return false;
    }
}




