package com.aimud.aimud.songs.dot;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.songs.Song;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;
import org.springframework.stereotype.Component;

@Component
@BardSong(name = "hauntingmelody")
public class HauntingMelody extends Song {

    public HauntingMelody(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() { return "Haunting Melody"; }

    @Override
    public Long getSongId() { return 3508L; }

    @Override
    public Integer getSongLevel() { return 4; }

    @Override
    public String getDescription() {
        return "A level 4 Damage-Over-Time song. Usage: sing 'hauntingmelody' <target>";
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
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
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou subject " + target.getName() + " to the agonizing duration of Haunting Melody.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " afflicts you with Haunting Melody!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " subjects " + target.getName() + " to Haunting Melody.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour song fizzles out of tune.");
            return false;
        }
    }
}
