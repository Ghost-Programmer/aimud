package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the hpregen bard song.
 */
@BardSong(name = "hpregen")
public class HpRegenSong extends Song {

    /**
     * Constructs the hpregen dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected HpRegenSong(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() {
        return "Song of Healing";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() {
        return 3004L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() {
        return 5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Accelerates health regeneration through an uplifting melody. Usage: sing hpregen [target]";
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

