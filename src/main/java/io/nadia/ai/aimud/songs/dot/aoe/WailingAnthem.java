package io.nadia.ai.aimud.songs.dot.aoe;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Implementation of the wailinganthem bard song.
 */
@Component
@BardSong(name = "wailinganthem")
public class WailingAnthem extends Song {

    /**
     * Constructs the wailinganthem dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param MobileService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public WailingAnthem(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() { return "Wailing Anthem"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() { return 3513L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() { return 30; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getManaCost(Mobile mobile) { return super.getManaCost(mobile) * 2; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 30 AoE Damage-Over-Time song. Usage: sing 'wailinganthem' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sing(Mobile mobile, Song song, Mobile primaryTarget) {
        if (primaryTarget != null && !this.mobileService.canTarget(primaryTarget)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + primaryTarget.getName() + ".");
            return false;
        }

        if (primaryTarget == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou must specify a target.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, primaryTarget);
        if (targets.isEmpty()) return false;

        int rank = this.skillService.getSkillRank(mobile, this.getSongSkillName());
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        
        // Locked duration parsing rule requested explicitly by architect 
        int duration = Math.min(5, 1 + (this.skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));
        
        int diceSize = 6 + (rank / 10);

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou aggressively sweep Wailing Anthem around " + primaryTarget.getName() + "!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " tortures the room performing an agonizing rendition of Wailing Anthem!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getSongSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " damages you across ticks with Wailing Anthem!");
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour song fizzles out of tune.");
            return false;
        }
    }
}


