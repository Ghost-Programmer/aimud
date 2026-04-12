package com.aimud.aimud.songs.dot.aoe;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.songs.Song;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@BardSong(name = "macabreoverture")
public class MacabreOverture extends Song {

    public MacabreOverture(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() { return "Macabre Overture"; }

    @Override
    public Long getSongId() { return 3515L; }

    @Override
    public Integer getSongLevel() { return 90; }

    @Override
    public Integer getManaCost(Mobile mobile) { return super.getManaCost(mobile) * 2; }

    @Override
    public String getDescription() {
        return "A level 90 AoE Damage-Over-Time song. Usage: sing 'macabreoverture' <target>";
    }

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
        int numDice = Math.max(1, Math.min(15, (rank / 7) + 1));
        
        // Locked duration parsing rule requested explicitly by architect 
        int duration = Math.min(5, 1 + (this.skillService.getSkillRank(mobile, SkillsType.SING_SONG) / 20));
        
        int diceSize = 6 + (rank / 10);

        String effectName = "Sonic Damage " + numDice + "d" + diceSize;
        Effect effect = this.effectService.getEffectByName(effectName).block();

        if (effect != null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou aggressively sweep Macabre Overture around " + primaryTarget.getName() + "!");
            this.communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " tortures the room performing an agonizing rendition of Macabre Overture!");

            boolean applied = false;
            for (Mobile tgt : targets) {
                boolean appliedHere = this.applyEffect(tgt, mobile, this.getSongSkillName(), effect, duration);
                if (appliedHere) applied = true;
                if (tgt.getUserId() != null) this.communicationService.sendTextMessage(tgt, "\n\n" + mobile.getName() + " damages you across ticks with Macabre Overture!");
            }
            return applied;
        } else {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYour song fizzles out of tune.");
            return false;
        }
    }
}
