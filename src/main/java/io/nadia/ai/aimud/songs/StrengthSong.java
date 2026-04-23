package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;

/**
 * Implementation of the strength bard song.
 */
@BardSong(name = "strength")
public class StrengthSong extends Song {

    /**
     * Constructs the strength dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected StrengthSong(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() {
        return "Song of Strength";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() {
        return 3010L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() {
        return 20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Inspires the target with physical might. Usage: sing strength [target]";
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
        if (target != null && !this.mobileService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

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


