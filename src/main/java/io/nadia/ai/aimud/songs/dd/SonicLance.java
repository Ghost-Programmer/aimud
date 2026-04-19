package io.nadia.ai.aimud.songs.dd;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;

/**
 * Implementation of the soniclance bard song.
 */
@Component
@BardSong(name = "soniclance")
public class SonicLance extends Song {

    /**
     * Constructs the soniclance dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public SonicLance(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSongName() { return "Sonic Lance"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSongId() { return 3503L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSongLevel() { return 85; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "A level 85 direct damage song. Usage: sing 'soniclance' <target>";
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
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        int numDice = 2 + Math.round((rank - 1) * 38.0f / 99.0f);
        int diceSize = 8;
        int damage = new Dice(numDice, diceSize).getTotal();

        boolean resist = false;
        if (target.getMagicResist() > new Dice(1, 100).getTotal()) {
            damage /= 2;
            resist = true;
        }

        if (mobile.getUserId() != null) {
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou strike %s with the reverberations of Sonic Lance for %d sonic damage!", target.getName(), damage));
        }
        if (target.getUserId() != null) {
            this.communicationService.sendTextMessage(target, String.format("\n\n%s's Sonic Lance tears through you for %d sonic damage!", mobile.getName(), damage));
        }
        this.communicationService.roomMessage(mobile, String.format("\n\n%s shreds %s with Sonic Lance!", mobile.getName(), target.getName()));

        target.setCurrentHp(target.getCurrentHp() - damage);
        int hateAmount = damage;
        target.addHate(mobile.getId(), hateAmount);
        if (mobile.getTarget() == null) {
            if (!this.characterService.setTarget(mobile, target)) return false;
        }

        if (target.getCurrentHp() <= 0) {
            target.setCurrentHp(0);
            String deathMsg = "\n" + target.getName() + " is DEAD!!";
            if (mobile.getUserId() != null && mobile.getId() != target.getId()) this.communicationService.sendTextMessage(mobile, deathMsg);
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\nYou have died...");
                this.communicationService.sendTextMessage(target, deathMsg);
            }
            this.communicationService.roomMessage(target, deathMsg);
            this.characterService.setTarget(target, null);
            if (mobile.getTarget() == target) this.characterService.setTarget(mobile, null);
            this.characterService.findAllByRoomId(target.getCurrentRoomId()).forEach(m -> m.removeHate(target.getId()));
        } else {
            if (target.getTarget() == null) if (!this.characterService.setTarget(target, mobile)) return false;
        }

        if (target.getUserId() != null) this.characterService.save(target).subscribe();
        else this.mobileService.saveMobile(target).subscribe();

        return !resist;
    }
}

