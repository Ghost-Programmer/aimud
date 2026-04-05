package com.aimud.aimud.prayers.debuff;

import com.aimud.aimud.Dice;
import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.prayers.Prayer;

@MudCommand(name = "pray 'minor stupify'")
public class MinorStupify extends Prayer {

    public MinorStupify(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Minor Stupify";
    }

    @Override
    public Long getPrayerId() {
        return 3010L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Lowers Wisdom and Intelligence and Charisma. Magnitude and duration scale with your skill. Usage: pray 'minor stupify' <target>";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target == null) {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            }
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        
        int numDice = Math.max(1, Math.min(10, (rank / 10) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 4;

        int drop = new Dice(numDice, diceSize).getTotal();
        
        boolean applied = false;
        String[] effectsToApply = {"Wisdom", "Intelligence", "Charisma"};
        
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
                this.communicationService.sendTextMessage(mobile, "\n\nYou cast Minor Stupify on " + target.getName() + ", dropping their stats by " + drop + ".");
            }
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " casts Minor Stupify on you! You feel weaker!");
            }
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " casts Minor Stupify on " + target.getName() + ".");
            return true;
        } else {
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fails.");
            }
            return false;
        }
    }
}
