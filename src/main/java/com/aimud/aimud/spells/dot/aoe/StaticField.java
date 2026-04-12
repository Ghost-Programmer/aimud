package com.aimud.aimud.spells.dot.aoe;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.spells.Spell;
import java.util.List;

@MagicSpell(name = "staticfield")
public class StaticField extends Spell {

    public StaticField(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Static Field";
    }

    @Override
    public Long getSpellId() {
        return 1108L;
    }

    @Override
    public Integer getSpellLevel() {
        return 14;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return super.getManaCost(mobile) * 2;
    }

    @Override
    public String getDescription() {
        return "An AoE level 14 spell that unleashes Static Field on targets.. Damage and duration scale with skill rank. Usage: cast 'staticfield' <target>";
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile primaryTarget) {
        if (primaryTarget != null && !this.characterService.canTarget(primaryTarget)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + primaryTarget.getName() + ".");
            return false;
        }

        if (primaryTarget == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            }
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, primaryTarget);
        if (targets.isEmpty()) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nThere is no one to affect.");
            }
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        
        int numDice = Math.max(1, Math.min(10, (rank / 10) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6;

        String effectName = "Electrical Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou unleash a massive blast of Static Field starting at " + primaryTarget.getName() + "!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " unleashes a massive blast of Static Field at " + primaryTarget.getName() + "!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getSpellSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " engulfs you in Static Field!");
                }
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYour spell fizzles.");
            }
            return false;
        }
    }
}
