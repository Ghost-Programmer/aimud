package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "fly")
public class FlySong extends Song {

    protected FlySong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Flight";
    }

    @Override
    public Long getSongId() {
        return 3003L;
    }

    @Override
    public Integer getSongLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Grants the target the ability to fly through an uplifting melody. Usage: sing fly [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        int songSkill = skillService.getSkillRank(mobile, getSongSkillName());
        int cost;

        if (songSkill <= 51) {
            cost = 25 + (songSkill / 5);
        } else if (songSkill <= 61) {
            cost = 125;
        } else if (songSkill <= 75) {
            cost = 175;
        } else {
            cost = 250;
        }

        return cost;
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        int tickCount = Math.min(5, 1 + (skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));

        Effect flyEffect = this.effectService.getEffectByName("Fly").block();

        if (flyEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour song fails to elevate anyone.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), flyEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing an uplifting song, granting flight to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s's uplifting song causes your feet to lift off the ground!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s begins to hover as an uplifting melody fills the air.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
