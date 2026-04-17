package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the hpregen spell.
 */
@MagicSpell(name = "hpregen")
public class HpRegen extends Spell {

    /**
     * Constructs the hpregen dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected HpRegen(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                      CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "HP Regen";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1005L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Accelerates health regeneration over time. Usage: cast hpregen";
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
            effectName = "HP Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "HP Regen +15";
        } else if (skillRank <= 75) {
            effectName = "HP Regen +20";
        } else {
            effectName = "HP Regen +25";
        }

        Effect hpRegenEffect = effectService.getEffectByName(effectName).block();
        if (hpRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour healing prayer fails to find form.");
            }
            return false;
        }

        if (applyEffect(mobile, getSpellSkillName(), hpRegenEffect, tickCount)) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile,
                        String.format("\n\nWarm vitality begins mending %s.", mobile == target ? "you" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                communicationService.sendTextMessage(target,
                        String.format("\n\n%s surrounds you with restorative magic!", mobile.getName()));
            }

            communicationService.roomMessage(mobile,
                    String.format("\n\n%s glows briefly with restorative energy.", mobile.getName()));
            return true;
        }

        return false;
    }
}



