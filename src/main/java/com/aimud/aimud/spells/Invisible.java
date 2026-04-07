package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@MagicSpell(name = "invisible")
public class Invisible extends Spell {

    protected Invisible(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Invisible";
    }

    @Override
    public Long getSpellId() {
        return 1006L;
    }

    @Override
    public Integer getSpellLevel() {
        return 15;
    }

    @Override
    public String getDescription() {
        return "Renders the target invisible to the naked eye. Usage: cast invisible [target]";
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
