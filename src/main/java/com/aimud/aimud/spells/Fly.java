package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@MagicSpell(name = "fly")
public class Fly extends Spell {

    protected Fly(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Fly";
    }

    @Override
    public Long getSpellId() {
        return 1002L;
    }

    @Override
    public Integer getSpellLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Grants the target the ability to fly through the air. Usage: cast fly [target]";
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
