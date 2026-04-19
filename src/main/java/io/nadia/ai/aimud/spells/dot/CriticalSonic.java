package io.nadia.ai.aimud.spells.dot;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.spells.Spell;

/**
 * Implementation of the critical sonic spell.
 */
@MagicSpell(name = "critical sonic")
public class CriticalSonic extends Spell {

    /**
     * Constructs the critical sonic dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public CriticalSonic(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Critical Sonic";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1017L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 80;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 80 spell that assaults the target with sound waves. Damage and duration scale with skill rank. Usage: cast 'critical sonic' <target>";
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

        if (target == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            }
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        
        int numDice = Math.max(1, Math.min(10, (rank / 10) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 20;

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            this.applyEffect(target, mobile, this.getSpellSkillName(), effect, duration);
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou cast Critical Sonic on " + target.getName() + ".");
            }
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " casts Critical Sonic on you!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " casts Critical Sonic on " + target.getName() + ".");
            return true;
        } else {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYour spell fizzles.");
            }
            return false;
        }
    }
}

