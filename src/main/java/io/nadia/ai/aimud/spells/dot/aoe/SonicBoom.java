package io.nadia.ai.aimud.spells.dot.aoe;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.spells.Spell;
import java.util.List;

@MagicSpell(name = "sonicboom")
public class SonicBoom extends Spell {

    public SonicBoom(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Sonic Boom";
    }

    @Override
    public Long getSpellId() {
        return 1117L;
    }

    @Override
    public Integer getSpellLevel() {
        return 30;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return super.getManaCost(mobile) * 2;
    }

    @Override
    public String getDescription() {
        return "An AoE level 30 spell that unleashes Sonic Boom on targets.. Damage and duration scale with skill rank. Usage: cast 'sonicboom' <target>";
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
        int diceSize = 8;

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou unleash a massive blast of Sonic Boom starting at " + primaryTarget.getName() + "!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " unleashes a massive blast of Sonic Boom at " + primaryTarget.getName() + "!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getSpellSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " engulfs you in Sonic Boom!");
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
