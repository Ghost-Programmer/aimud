package io.nadia.ai.aimud.songs.dd.aoe;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Implementation of the cacophony bard song.
 */
@Component
@BardSong(name = "cacophony")
public class Cacophony extends Song {

    /**
     * Constructs the cacophony dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public Cacophony(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() { return "Cacophony"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() { return 3504L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() { return 15; }

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
        return "A level 15 AoE direct damage song. Usage: sing 'cacophony' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sing(Mobile mobile, Song song, Mobile primaryTarget) {
        if (primaryTarget != null && !this.characterService.canTarget(primaryTarget)) {
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
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        int numDice = 2 + Math.round((rank - 1) * 38.0f / 99.0f);
        int diceSize = 8;

        if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, String.format("\n\nYou tear the air apart around %s with Cacophony!", primaryTarget.getName()));
        this.communicationService.roomMessage(mobile, String.format("\n\n%s shakes the entire room violently with Cacophony!", mobile.getName()));

        for (Mobile tgt : targets) {
            int damage = new Dice(numDice, diceSize).getTotal();
            boolean resist = false;
            if (tgt.getMagicResist() > new Dice(1, 100).getTotal()) {
                damage /= 2;
                resist = true;
            }

            tgt.setCurrentHp(tgt.getCurrentHp() - damage);
            tgt.addHate(mobile.getId(), damage);
            if (mobile.getTarget() == null) {
                if (!this.characterService.setTarget(mobile, tgt)) continue;
            }

            if (tgt.getUserId() != null) {
                this.communicationService.sendTextMessage(tgt, String.format("\n\nYou are struck by %s's explosive Cacophony for %d sonic damage!", mobile.getName(), damage));
            }

            if (tgt.getCurrentHp() <= 0) {
                tgt.setCurrentHp(0);
                String deathMsg = "\n" + tgt.getName() + " is DEAD!!";
                if (mobile.getUserId() != null && mobile.getId() != tgt.getId()) this.communicationService.sendTextMessage(mobile, deathMsg);
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\nYou have died...");
                    this.communicationService.sendTextMessage(tgt, deathMsg);
                }
                this.communicationService.roomMessage(tgt, deathMsg);
                this.characterService.setTarget(tgt, null);
                if (mobile.getTarget() == tgt) this.characterService.setTarget(mobile, null);
                this.characterService.findAllByRoomId(tgt.getCurrentRoomId()).forEach(m -> m.removeHate(tgt.getId()));
            } else {
                if (tgt.getTarget() == null) if (!this.characterService.setTarget(tgt, mobile)) continue;
            }

            if (tgt.getUserId() != null) this.characterService.save(tgt).subscribe();
            else this.mopbileService.saveMobile(tgt).subscribe();
        }
        return true;
    }
}

