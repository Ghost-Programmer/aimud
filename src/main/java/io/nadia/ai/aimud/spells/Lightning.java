package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;
import org.springframework.stereotype.Component;

@Component
@MagicSpell(name = "lightning")
public class Lightning extends Spell {

    public Lightning(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Lightning";
    }

    @Override
    public Long getSpellId() {
        return 1007L;
    }

    @Override
    public Integer getSpellLevel() {
        return 40;
    }

    @Override
    public String getDescription() {
        return "Summons a powerful bolt of electrical energy to blast a single target. Usage: cast lightning <target>";
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou need a target to blast with lightning.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        // Scales from 10d10 at rank 1 up to 50d10 at rank 100
        int numDice = 10 + Math.round((rank - 1) * 40.0f / 99.0f);
        int diceSize = 10;

        int damage = new Dice(numDice, diceSize).getTotal();

        boolean resist = false;

        if (target.getMagicResist() > new Dice(1, 100).getTotal()) {
            damage = damage / 2;
            resist = true;
        }

        if (mobile.getUserId() != null) {
            if (resist) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\n%s partly resists your lightning bolt!", target.getName()));
            }
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou blast %s with a bolt of lightning for %d damage!", target.getName(), damage));
        }

        if (target.getUserId() != null) {
            if (resist) {
                this.communicationService.sendTextMessage(target, String.format("\n\nYou partly resist %s's lightning bolt!", mobile.getName()));
            }
            this.communicationService.sendTextMessage(target, String.format("\n\n%s blasts you with a bolt of lightning for %d damage!", mobile.getName(), damage));
        }
        
        if (resist) {
            this.communicationService.roomMessage(mobile, String.format("\n\n%s's lightning bolt is partly resisted as it strikes %s!", mobile.getName(), target.getName()));
        } else {
            this.communicationService.roomMessage(mobile, String.format("\n\n%s blasts %s with a bolt of lightning!", mobile.getName(), target.getName()));
        }

        target.setCurrentHp(target.getCurrentHp() - damage);
        
        int hateAmount = damage;
        if (target.isHateWizard()) hateAmount *= 5;
        target.addHate(mobile.getId(), hateAmount);
        if (mobile.getTarget() == null) {
            if (!this.characterService.setTarget(mobile, target)) return false;
        }

        if (target.getCurrentHp() <= 0) {
            target.setCurrentHp(0);
            String deathMsg = "\n" + target.getName() + " is DEAD!!";
            if (mobile.getUserId() != null && mobile.getId() != target.getId()) {
                this.communicationService.sendTextMessage(mobile, deathMsg);
            }
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\nYou have died...");
                this.communicationService.sendTextMessage(target, deathMsg);
            }
            this.communicationService.roomMessage(target, deathMsg);

            this.characterService.setTarget(target, null);
            if (mobile.getTarget() == target) {
                this.characterService.setTarget(mobile, null);
            }

            // Clear hate
            this.characterService.findAllByRoomId(target.getCurrentRoomId())
                    .forEach(m -> m.removeHate(target.getId()));
        } else {
            if (target.getTarget() == null) {
                if (!this.characterService.setTarget(target, mobile)) return false;
            }
        }

        if (target.getUserId() != null) {
            this.characterService.save(target).subscribe();
        } else {
            this.mopbileService.saveMobile(target).subscribe();
        }

        return !resist;
    }
}
