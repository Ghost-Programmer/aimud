package com.aimud.aimud.spells;

import com.aimud.aimud.Dice;
import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@MagicSpell(name = "chainlightning")
public class ChainLightning extends Spell {

    public ChainLightning(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Chain Lightning";
    }

    @Override
    public Long getSpellId() {
        return 1001L;
    }

    @Override
    public Integer getSpellLevel() {
        return 60;
    }

    @Override
    public String getDescription() {
        return "Summons a devastating chain of electrical energy that arcs between multiple targets. Usage: cast chainlightning <target>";
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target == null) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou need a target to initiate the chain lightning on.");
            return false;
        }

        List<Mobile> targets = this.getAoeTargets(mobile, target);
        if (targets.isEmpty()) {
            this.communicationService.sendTextMessage(mobile, "\n\nThere is no one to strike with lightning.");
            return false;
        }

        int rank = this.skillService.getSkillRank(mobile, this.getSpellSkillName());
        if (rank < 1) rank = 1; else if (rank > 100) rank = 100;

        // Scales from 10d10 at rank 1 up to 50d10 at rank 100
        int numDice = 10 + Math.round((rank - 1) * 40.0f / 99.0f);
        int diceSize = 10;

        if (mobile.getUserId() != null) {
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou unleash a storm of chain lightning starting at %s!", target.getName()));
        }
        this.communicationService.roomMessage(mobile, String.format("\n\n%s unleashes chain lightning at %s, electricity arcing to everyone nearby!", mobile.getName(), target.getName()));

        for (Mobile tgt : targets) {
            int damage = new Dice(numDice, diceSize).getTotal();

            boolean resist = false;
            // minimal magic saving throw mechanic
            if (tgt.getMagicResist() > new Dice(1, 100).getTotal()) {
                damage = damage / 2;
                resist = true;
            }

            if (tgt.getUserId() != null) {
                if (resist) {
                    this.communicationService.sendTextMessage(tgt, String.format("\n\nYou resist %s's chain lightning, taking half damage!", mobile.getName()));
                } else {
                    this.communicationService.sendTextMessage(tgt, String.format("\n\nYou are electrocuted by %s's chain lightning for %d damage!", mobile.getName(), damage));
                }
            }

            tgt.setCurrentHp(tgt.getCurrentHp() - damage);

            int hateAmount = damage;
            if (tgt.isHateWizard()) hateAmount *= 5;
            tgt.addHate(mobile.getId(), hateAmount);
            if (mobile.getTarget() == null) {
                mobile.setTarget(tgt);
            }

            if (tgt.getCurrentHp() <= 0) {
                tgt.setCurrentHp(0);
                String deathMsg = "\n" + tgt.getName() + " is DEAD!!";
                if (mobile.getUserId() != null && mobile.getId() != tgt.getId()) {
                    this.communicationService.sendTextMessage(mobile, deathMsg);
                }
                if (tgt.getUserId() != null) {
                    this.communicationService.sendTextMessage(tgt, "\n\nYou have died...");
                    this.communicationService.sendTextMessage(tgt, deathMsg);
                }
                this.communicationService.roomMessage(tgt, deathMsg);

                tgt.setTarget(null);
                if (mobile.getTarget() == tgt) {
                    mobile.setTarget(null);
                }

                this.characterService.findAllByRoomId(tgt.getCurrentRoomId())
                        .forEach(m -> m.removeHate(tgt.getId()));
            } else {
                if (tgt.getTarget() == null) {
                    tgt.setTarget(mobile);
                }
            }

            if (tgt.getUserId() != null) {
                 this.characterService.save(tgt).subscribe();
            } else {
                 this.mopbileService.saveMobile(tgt).subscribe();
            }
        }

        return true;
    }
}
