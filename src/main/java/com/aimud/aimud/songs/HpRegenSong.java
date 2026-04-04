package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "hpregen")
public class HpRegenSong extends Song {

    protected HpRegenSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Healing";
    }

    @Override
    public Long getSongId() {
        return 3009L;
    }

    @Override
    public Integer getSongLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Accelerates health regeneration through an uplifting melody. Usage: sing hpregen [target]";
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
            effectName = "HP Regen +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "HP Regen +15";
        } else if (skillRank <= 75) {
            effectName = "HP Regen +20";
        } else {
            effectName = "HP Regen +25";
        }

        Effect hpRegenEffect = this.effectService.getEffectByName(effectName).block();

        if (hpRegenEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour healing melody finds no harmony.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), hpRegenEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing an uplifting, mending song to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s sings a song that begins mending your wounds!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s is filled with a bright, restorative energy.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
