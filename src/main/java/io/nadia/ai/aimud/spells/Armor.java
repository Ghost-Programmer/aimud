package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the armor spell.
 */
@MagicSpell(name = "armor")
public class Armor extends Spell {


    /**
     * Constructs the armor dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected Armor(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Magic Armor";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1000L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Protects the caster from physical attacks by increasing their armor class. Usage: cast armor ";
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
        int cost;

        if (spellSkill <= 51) {
            cost = 25 + (spellSkill / 5);
        } else if (spellSkill <= 61) {
            cost = 125;
        } else if (spellSkill <= 75) {
            cost = 175;
        } else {
            cost = 250;
        }

        return cost;
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
        int duration = 10 + skillRank;

        String effectName;

        if (skillRank <= 51) {
            effectName = "Armor +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Armor +15";
        } else if (skillRank <= 75) {
            effectName = "Armor +20";
        } else {
            effectName = "Armor +25";
        }

        Effect armorEffect = this.effectService.getEffectByName(effectName).block();

        if (this.applyEffect(mobile, this.getSpellSkillName(), armorEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou feel a shimmering field of force surround %s.",
                        mobile == target ? "you" : target.getName()));
            }

            if (mobile.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s protects you with a magical suit of armor!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s's skin shimmers briefly as a magical field of armor forms around %s.",
                    mobile.getName(), mobile == target ? "them" : target.getName()));

        }
        return true;
    }
}


