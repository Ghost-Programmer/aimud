package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the manaregen spell.
 */
@MagicSpell(name = "manaregen")
public class ManaRegen extends Spell {

    /**
     * Constructs the manaregen dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected ManaRegen(SkillService skillService, MobileService mobileService, CharacterService characterService,
                        CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Mana Regen";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1010L;
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
        return "Accelerates mana recovery over time. Usage: cast manaregen";
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
            return 25 + (spellSkill / 5);
        } else if (spellSkill <= 61) {
            return 125;
        } else if (spellSkill <= 75) {
            return 175;
        }
        return 250;
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

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 5;

        String effectName;
        if (skillRank <= 51) {
            effectName = "Mana Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Mana Regen +15";
        } else if (skillRank <= 75) {
            effectName = "Mana Regen +20";
        } else {
            effectName = "Mana Regen +25";
        }

        Effect manaRegenEffect = effectService.getEffectByName(effectName).block();
        if (manaRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nThe weave fizzles. No matching mana regeneration effect exists.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), manaRegenEffect, tickCount)) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile,
                        String.format("\n\nA calm pulse of arcane energy settles over %s.", mobile == target ? "you" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                communicationService.sendTextMessage(target,
                        String.format("\n\n%s blesses you with flowing mana!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s is surrounded by a steady arcane shimmer.", mobile.getName()));
            return true;
        }

        return false;
    }
}



