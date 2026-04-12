package com.aimud.aimud.songs.dot;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.songs.Song;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;
import org.springframework.stereotype.Component;

@Component
@BardSong(name = "lullabyofagony")
public class LullabyOfAgony extends Song {

    public LullabyOfAgony(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() { return "Lullaby of Agony"; }

    @Override
    public Long getSongId() { return 3511L; }

    @Override
    public Integer getSongLevel() { return 80; }

    @Override
    public String getDescription() {
        return "A level 80 Damage-Over-Time song. Usage: sing 'lullabyofagony' <target>";
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSongSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        
        // Locked duration parsing rule requested explicitly by architect
        int duration = Math.min(5, 1 + (this.skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));
        
        int diceSize = 6 + (rank / 10);

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            this.applyEffect(target, mobile, this.getSongSkillName(), effect, duration);
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou subject " + target.getName() + " to the agonizing duration of Lullaby of Agony.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " afflicts you with Lullaby of Agony!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " subjects " + target.getName() + " to Lullaby of Agony.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour song fizzles out of tune.");
            return false;
        }
    }
}
