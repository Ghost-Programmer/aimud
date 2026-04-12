package com.aimud.aimud.prayers.dd.aoe;

import com.aimud.aimud.Dice;
import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.prayers.Prayer;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@DivinePrayer(name = "holyword")
public class HolyWord extends Prayer {

    public HolyWord(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() { return "Holy Word"; }

    @Override
    public Long getPrayerId() { return 2504L; }

    @Override
    public Integer getPrayerLevel() { return 15; }

    @Override
    public Integer getManaCost(Mobile mobile) { return super.getManaCost(mobile) * 2; }

    @Override
    public String getDescription() {
        return "A level 15 AoE direct damage prayer targeting hostiles. Usage: pray 'holyword' <target>";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou need a target to center your wrath.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, target);
        if (targets.isEmpty()) return false;

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        int numDice = 2 + Math.round((rank - 1) * 38.0f / 99.0f);
        int diceSize = 8;

        if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, String.format("\n\nYou invoke Holy Word upon %s and surrounding enemies!", target.getName()));
        this.communicationService.roomMessage(mobile, String.format("\n\n%s invokes fierce Holy Word spreading out from %s!", mobile.getName(), target.getName()));

        for (Mobile tgt : targets) {
            int damage = new Dice(numDice, diceSize).getTotal();
            boolean resist = false;
            if (tgt.getMagicResist() > new Dice(1, 100).getTotal()) {
                damage /= 2;
                resist = true;
            }

            tgt.setCurrentHp(tgt.getCurrentHp() - damage);
            tgt.addHate(mobile.getId(), damage);
            if (mobile.getTarget() == null) {
                mobile.setTarget(tgt);
            }

            if (tgt.getUserId() != null) {
                this.communicationService.sendTextMessage(tgt, String.format("\n\nYou are struck by %s's Holy Word for %d holy damage!", mobile.getName(), damage));
            }

            if (tgt.getCurrentHp() <= 0) {
                tgt.setCurrentHp(0);
                String deathMsg = "\n" + tgt.getName() + " is DEAD!!";
                if (mobile.getUserId() != null && mobile.getId() != tgt.getId()) this.communicationService.sendTextMessage(mobile, deathMsg);
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\nYou have died...");
                    this.communicationService.sendTextMessage(tgt, deathMsg);
                }
                this.communicationService.roomMessage(tgt, deathMsg);
                tgt.setTarget(null);
                if (mobile.getTarget() == tgt) mobile.setTarget(null);
                this.characterService.findAllByRoomId(tgt.getCurrentRoomId()).forEach(m -> m.removeHate(tgt.getId()));
            } else {
                if (tgt.getTarget() == null) tgt.setTarget(mobile);
            }

            if (tgt.getUserId() != null) this.characterService.save(tgt).subscribe();
            else this.mopbileService.saveMobile(tgt).subscribe();
        }
        return true;
    }
}
