package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the darkvision spell.
 */
@MagicSpell(name = "darkvision")
public class Darkvision extends Spell {

    protected Darkvision(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Darkvision";
    }

    @Override
    public Long getSpellId() {
        return 1001L;
    }

    @Override
    public Integer getSpellLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Allows the target to see in the dark. Usage: cast darkvision [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());
        int cost;

        if (spellSkill <= 51) {
            cost = 20 + (spellSkill / 5);
        } else if (spellSkill <= 75) {
            cost = 35;
        } else {
            cost = 50;
        }

        return cost;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        int skillRank = skillService.getSkillRank(mobile, getSpellSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC) + 10;

        int visionAmount = Math.max(1, Math.min(10, (skillRank / 10) + 1));
        String effectName = "Darkvision +" + visionAmount;

        Effect darkvisionEffect = this.effectService.getEffectByName(effectName).block();

        if (this.applyEffect(target, mobile, this.getSpellSkillName(), darkvisionEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou infuse %s's eyes with mystical energy.",
                        mobile == target ? "your" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s infuses your eyes with mystical energy, expanding your vision!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s's eyes glow briefly with a pale light.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
