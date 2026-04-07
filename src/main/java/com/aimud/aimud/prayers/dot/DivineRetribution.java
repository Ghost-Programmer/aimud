package com.aimud.aimud.prayers.dot;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.prayers.Prayer;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;

@Component
@DivinePrayer(name = "divineretribution")
public class DivineRetribution extends Prayer {

    public DivineRetribution(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() { return "Divine Retribution"; }

    @Override
    public Long getPrayerId() { return 2511L; }

    @Override
    public Integer getPrayerLevel() { return 80; }

    @Override
    public String getDescription() {
        return "A level 80 Damage-Over-Time holy prayer. Usage: pray 'divineretribution' <target>";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
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
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou brand " + target.getName() + " with Divine Retribution.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " brands you with Divine Retribution!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " brands " + target.getName() + " with Divine Retribution.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}
