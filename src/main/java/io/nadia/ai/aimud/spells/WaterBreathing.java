package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

@MagicSpell(name = "water breathing")
public class WaterBreathing extends Spell {

    protected WaterBreathing(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Water Breathing";
    }

    @Override
    public Long getSpellId() {
        return 1011L;
    }

    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Allows the target to breathe underwater. Usage: cast water breathing [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return 50;
    }

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

        Effect wbEffect = this.effectService.getEffectByName("Water Breathing").block();

        if (wbEffect == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nThe magic fails to take hold.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSpellSkillName(), wbEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou grant the ability to breathe water to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s casts a spell, and you feel capable of breathing water!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\nFaint gills appear briefly on %s's neck before vanishing.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
