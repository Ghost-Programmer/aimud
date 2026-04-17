package io.nadia.ai.aimud.songs.dot;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.stereotype.Component;

/**
 * Implementation of the lullabyofagony bard song.
 */
@Component
@BardSong(name = "lullabyofagony")
public class LullabyOfAgony extends Song {

    /**
     * Constructs the lullabyofagony dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public LullabyOfAgony(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() { return "Lullaby of Agony"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() { return 3511L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() { return 80; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 80 Damage-Over-Time song. Usage: sing 'lullabyofagony' <target>";
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

        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSongSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        
        // Locked duration parsing rule requested explicitly by architect
        int duration = Math.min(5, 1 + (this.skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));
        
        int diceSize = 6 + (rank / 10);

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            this.applyEffect(target, mobile, this.getSongSkillName(), effect, duration);
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou subject " + target.getName() + " to the agonizing duration of Lullaby of Agony.");
            if (target.getUserId() != null) this.communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " afflicts you with Lullaby of Agony!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " subjects " + target.getName() + " to Lullaby of Agony.");
            return true;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour song fizzles out of tune.");
            return false;
        }
    }
}

