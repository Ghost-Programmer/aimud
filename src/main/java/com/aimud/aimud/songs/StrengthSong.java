package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "strength")
public class StrengthSong extends Song {

    protected StrengthSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Strength";
    }

    @Override
    public Long getSongId() {
        return 3002L;
    }

    @Override
    public Integer getSongLevel() {
        return 20;
    }

    @Override
    public String getDescription() {
        return "Inspires the target with physical might. Usage: sing strength [target]";
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
            effectName = "Strength +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Strength +15";
        } else if (skillRank <= 75) {
            effectName = "Strength +20";
        } else {
            effectName = "Strength +25";
        }

        Effect strengthEffect = this.effectService.getEffectByName(effectName).block();

        if (strengthEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour song fails to inspire anyone.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), strengthEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing an inspiring song of strength to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s sings a song of strength for you!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s's muscles bulge as an inspiring rhythm fills the air.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
