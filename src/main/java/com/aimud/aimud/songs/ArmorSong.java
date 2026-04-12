package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "armor")
public class ArmorSong extends Song {

    protected ArmorSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Armor";
    }

    @Override
    public Long getSongId() {
        return 3000L;
    }

    @Override
    public Integer getSongLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Weaves a magical armor of sound around the target. Usage: sing armor [target]";
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

        int skillRank = skillService.getSkillRank(mobile, getSongSkillName());
        int tickCount = Math.min(5, 1 + (skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));

        String effectName;

        if (skillRank <= 51) {
            effectName = "Armor +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Armor +15";
        } else if (skillRank <= 75) {
            effectName = "Armor +20";
        } else {
            effectName = "Armor +25";
        }

        Effect armorEffect = this.effectService.getEffectByName(effectName).block();

        if (armorEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour protective tempo falls flat.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), armorEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou weave a protective song around %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s weaves a shield of sound around you!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\nA faint shimmering resonance surrounds %s.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
