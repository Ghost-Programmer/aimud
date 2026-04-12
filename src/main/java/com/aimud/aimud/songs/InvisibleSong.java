package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "invisible")
public class InvisibleSong extends Song {

    protected InvisibleSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Shadows";
    }

    @Override
    public Long getSongId() {
        return 3006L;
    }

    @Override
    public Integer getSongLevel() {
        return 15;
    }

    @Override
    public String getDescription() {
        return "Renders the target invisible by weaving a song of shadows. Usage: sing invisible [target]";
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
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        int tickCount = Math.min(5, 1 + (skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));

        Effect invisEffect = this.effectService.getEffectByName("Invisible").block();

        if (invisEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nThe shadows refuse to listen to your song.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), invisEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing a shadowy tune, fading %s from view.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s's shadowy song wraps around you, turning you invisible!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s fades completely from sight as a shadowy tune fills the area.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
