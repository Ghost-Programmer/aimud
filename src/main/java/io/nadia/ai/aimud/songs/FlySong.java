package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the fly bard song.
 */
@BardSong(name = "fly")
public class FlySong extends Song {

    /**
     * Constructs the fly dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected FlySong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() {
        return "Song of Flight";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() {
        return 3003L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() {
        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Grants the target the ability to fly through an uplifting melody. Usage: sing fly [target]";
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

