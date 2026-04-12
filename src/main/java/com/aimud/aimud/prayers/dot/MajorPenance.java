package com.aimud.aimud.prayers.dot;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.prayers.Prayer;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;

@Component
@DivinePrayer(name = "majorpenance")
public class MajorPenance extends Prayer {

    public MajorPenance(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() { return "Major Penance"; }

    @Override
    public Long getPrayerId() { return 2510L; }

    @Override
    public Integer getPrayerLevel() { return 45; }

    @Override
    public String getDescription() {
        return "A level 45 Damage-Over-Time holy prayer. Usage: pray 'majorpenance' <target>";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6 + (rank / 10);

        String effectName = "Holy Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            this.applyEffect(target, mobile, this.getPrayerSkillName(), effect, duration);
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou brand " + target.getName() + " with Major Penance.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " brands you with Major Penance!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " brands " + target.getName() + " with Major Penance.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}
