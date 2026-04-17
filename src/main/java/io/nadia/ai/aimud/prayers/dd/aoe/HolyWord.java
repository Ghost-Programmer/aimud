package io.nadia.ai.aimud.prayers.dd.aoe;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Implementation of the holyword prayer.
 */
@Component
@DivinePrayer(name = "holyword")
public class HolyWord extends Prayer {

    /**
     * Constructs the holyword dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    public HolyWord(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrayerName() { return "Holy Word"; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getPrayerId() { return 2504L; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getPrayerLevel() { return 15; }

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
        return "A level 15 AoE direct damage prayer targeting hostiles. Usage: pray 'holyword' <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, "\n\nYou need a target to center your wrath.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, target);
        if (targets.isEmpty()) return false;

        int rank = this.skillService.getSkillRank(mobile, this.getPrayerSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        int numDice = 2 + Math.round((rank - 1) * 38.0f / 99.0f);
        int diceSize = 8;

        if (mobile.getUserId() != null) this.communicationService.sendTextMessage(mobile, String.format("\n\nYou invoke Holy Word upon %s and surrounding enemies!", target.getName()));
        this.communicationService.roomMessage(mobile, String.format("\n\n%s invokes fierce Holy Word spreading out from %s!", mobile.getName(), target.getName()));

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
                this.communicationService.sendTextMessage(tgt, String.format("\n\nYou are struck by %s's Holy Word for %d holy damage!", mobile.getName(), damage));
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

