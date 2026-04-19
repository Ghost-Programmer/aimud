package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the stamina bard song.
 */
@BardSong(name = "stamina")
public class StaminaSong extends Song {

    /**
     * Constructs the stamina dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected StaminaSong(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() {
        return "Song of Stamina";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() {
        return 3009L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() {
        return 16;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Inspires the target with enduring stamina. Usage: sing stamina [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
            effectName = "Constitution +" + ((skillRank / 5) + 1);
        } else if (skillRank <= 61) {
            effectName = "Constitution +15";
        } else if (skillRank <= 75) {
            effectName = "Constitution +20";
        } else {
            effectName = "Constitution +25";
        }

        Effect stamEffect = this.effectService.getEffectByName(effectName).block();

        if (stamEffect == null) {
            if (mobile.getUserId() != null) {
                communicationService.sendTextMessage(mobile, "\n\nYour song fades away, inspiring no one.");
            }
            return false;
        }

        if (this.applyEffect(target, this.getSongSkillName(), stamEffect, tickCount)) {

            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\nYou sing a steady song of stamina to %s.",
                        mobile == target ? "yourself" : target.getName()));
            }

            if (target.getUserId() != null && mobile != target) {
                this.communicationService.sendTextMessage(target, String.format("\n\n%s fills you with steady energy through their song!", mobile.getName()));
            }

            this.communicationService.roomMessage(mobile, String.format("\n\n%s breathes deeply, filled with enduring stamina.",
                    mobile == target ? mobile.getName() : target.getName()));

        }
        return true;
    }
}

