package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "dexterity")
public class DexteritySong extends Song {

    protected DexteritySong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Dexterity";
    }

    @Override
    public Long getSongId() {
        return 3005L;
    }

    @Override
    public Integer getSongLevel() {
        return 4;
    }

    @Override
    public String getDescription() {
        return "Grants the target graceful agility. Usage: sing dexterity [target]";
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
        int skillRank = skillService.getSkillRank(mobile, getSongSkillName());
        int tickCount = Math.min(5, 1 + (skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));

        String effectName;

        if (skillRank <= 51) {
            effectName = "Dexterity +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Dexterity +15";
        } else if (skillRank <= 75) {
            effectName = "Dexterity +20";
        } else {
            effectName = "Dexterity +25";
        }

        Effect dexEffect = this.effectService.getEffectByName(effectName).block();

        if (dexEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour song stumbles out of rhythm.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), dexEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing an upbeat song of dexterity to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s grants you agility with an upbeat song!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s moves lightly, guided by an upbeat rhythm.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
