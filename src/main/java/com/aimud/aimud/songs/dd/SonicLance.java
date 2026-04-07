package com.aimud.aimud.songs.dd;

import com.aimud.aimud.Dice;
import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.songs.Song;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;

@Component
@BardSong(name = "soniclance")
public class SonicLance extends Song {

    public SonicLance(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() { return "Sonic Lance"; }

    @Override
    public Long getSongId() { return 3503L; }

    @Override
    public Integer getSongLevel() { return 85; }

    @Override
    public String getDescription() {
        return "A level 85 direct damage song. Usage: sing 'soniclance' <target>";
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSongSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        int numDice = 2 + Math.round((rank - 1) * 38.0f / 99.0f);
        int diceSize = 8;
        int damage = new Dice(numDice, diceSize).getTotal();

        boolean resist = false;
        if (target.getMagicResist() > new Dice(1, 100).getTotal()) {
            damage /= 2;
            resist = true;
        }

        if (mobile.getUserId() != null) {
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou strike %s with the reverberations of Sonic Lance for %d sonic damage!", target.getName(), damage));
        }
        if (target.getUserId() != null) {
            this.communicationService.sendTextMessage(target, String.format("\n\n%s's Sonic Lance tears through you for %d sonic damage!", mobile.getName(), damage));
        }
        this.communicationService.roomMessage(mobile, String.format("\n\n%s shreds %s with Sonic Lance!", mobile.getName(), target.getName()));

        target.setCurrentHp(target.getCurrentHp() - damage);
        int hateAmount = damage;
        target.addHate(mobile.getId(), hateAmount);

        if (target.getCurrentHp() <= 0) {
            target.setCurrentHp(0);
            String deathMsg = "\n" + target.getName() + " is DEAD!!";
            if (mobile.getUserId() != null && mobile.getId() != target.getId()) this.communicationService.sendTextMessage(mobile, deathMsg);
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\nYou have died...");
                this.communicationService.sendTextMessage(target, deathMsg);
            }
            this.communicationService.roomMessage(target, deathMsg);
            target.setTarget(null);
            if (mobile.getTarget() == target) mobile.setTarget(null);
            this.characterService.findAllByRoomId(target.getCurrentRoomId()).forEach(m -> m.removeHate(target.getId()));
        } else {
            if (target.getTarget() == null) target.setTarget(mobile);
        }

        if (target.getUserId() != null) this.characterService.save(target).subscribe();
        else this.mopbileService.saveMobile(target).subscribe();

        return !resist;
    }
}
