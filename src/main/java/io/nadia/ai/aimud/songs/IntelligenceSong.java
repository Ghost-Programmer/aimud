package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

@BardSong(name = "intelligence")
public class IntelligenceSong extends Song {

    protected IntelligenceSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Song of Intelligence";
    }

    @Override
    public Long getSongId() {
        return 3005L;
    }

    @Override
    public Integer getSongLevel() {
        return 12;
    }

    @Override
    public String getDescription() {
        return "Sharpens the target's intellect. Usage: sing intelligence [target]";
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
            effectName = "Intelligence +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Intelligence +15";
        } else if (skillRank <= 75) {
            effectName = "Intelligence +20";
        } else {
            effectName = "Intelligence +25";
        }

        Effect intEffect = this.effectService.getEffectByName(effectName).block();

        if (intEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour song loses its tune.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), intEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing a song of intellect to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s sharpens your mind with a song!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s appears remarkably focused and alert.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}
