package io.nadia.ai.aimud.prayers.debuff;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.service.*;

@MudCommand(name = "pray 'lowerregen'")
public class LowerRegen extends Prayer {

    /**
     * Constructs the pray 'lowerregen' dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public LowerRegen(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() {
        return "LowerRegen";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() {
        return 2026L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() {
        return 15;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Lowers HP Regen and Mana Regen. Magnitude and duration scale with your skill. Usage: pray 'lowerregen' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target != null && !this.mobileService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            }
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        
        int numDice = Math.max(1, Math.min(10, (rank / 10) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6;

        int drop = new Dice(numDice, diceSize).getTotal();
        
        boolean applied = false;
        String[] effectsToApply = {"HP Regen", "Mana Regen"};
        
        for (String statName : effectsToApply) {
            String effectNameStr = statName + " -" + drop;
            Effect effect = this.effectService.getEffectByName(effectNameStr).block();
            
            if (effect != null) {
                this.applyEffect(target, mobile, this.getPrayerSkillName() + " (" + statName + ")", effect, duration);
                applied = true;
            } else {
                // Fallback attempt to get largest possible if drop exceeded 200
                if (drop > 200) {
                    effect = this.effectService.getEffectByName(statName + " -200").block();
                    if (effect != null) {
                        this.applyEffect(target, mobile, this.getPrayerSkillName() + " (" + statName + ")", effect, duration);
                        applied = true;
                    }
                }
            }
        }

        if (applied) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou cast LowerRegen on " + target.getName() + ", dropping their stats by " + drop + ".");
            }
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " casts LowerRegen on you! You feel weaker!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " casts LowerRegen on " + target.getName() + ".");
            return true;
        } else {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fails.");
            }
            return false;
        }
    }
}


