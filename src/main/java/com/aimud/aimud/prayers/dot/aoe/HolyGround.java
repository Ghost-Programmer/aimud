package com.aimud.aimud.prayers.dot.aoe;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.prayers.Prayer;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@DivinePrayer(name = "holyground")
public class HolyGround extends Prayer {

    public HolyGround(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() { return "Holy Ground"; }

    @Override
    public Long getPrayerId() { return 2513L; }

    @Override
    public Integer getPrayerLevel() { return 30; }

    @Override
    public Integer getManaCost(Mobile mobile) { return super.getManaCost(mobile) * 2; }

    @Override
    public String getDescription() {
        return "A level 30 AoE Damage-Over-Time holy prayer. Usage: pray 'holyground' <target>";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile primaryTarget) {
        if (primaryTarget == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, primaryTarget);
        if (targets.isEmpty()) return false;

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        int duration = Math.max(4, Math.min(20, (rank / 5) + 4));
        int diceSize = 6 + (rank / 10);

        String effectName = "Holy Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou engulf the area around " + primaryTarget.getName() + " with Holy Ground!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " casts a crushing Holy Ground centered on " + primaryTarget.getName() + "!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getPrayerSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " burns you with Holy Ground over time!");
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour prayer fizzles.");
            return false;
        }
    }
}
