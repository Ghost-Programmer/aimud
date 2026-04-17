package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.Dice;
import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

/**
 * Implementation of the missile spell.
 */
@MagicSpell(name = "missile")
public class MagicMissile extends Spell {


    /**
     * Constructs the missile dependencies.
     *
     * @param skillService         system for evaluating actor skill ranks
     * @param mobileService        registry of available AI targets
     * @param characterService     registry of active player characters
     * @param communicationService emitter for localized chat events
     * @param effectService        engine handling transient buffs and debuffs
     */
    protected MagicMissile(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpellName() {
        return "Magic Missile";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getSpellId() {
        return 1008L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getSpellLevel() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Fires a bolt of magic at your target dealing <skill> d 6 damage + bonus damage. Damage increases at higher levels. Usage: cast missile <target>";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }


        int damage = this.getDamage(mobile);

        boolean resist = false;

        if (target.getMagicResist() > new Dice(1, 100).getTotal()) {
            damage = damage / 2;
            resist = true;
        }

        if (mobile.getUserId() != null) {

            if (resist) {
                this.communicationService.sendTextMessage(mobile, String.format("\n\n%s resists your magic missile for half damage!", target.getName()));
            }
            this.communicationService.sendTextMessage(mobile, String.format("\n\nYou fire a magic missile at %s for %d damage!", target.getName(), damage));
        }

        if (mobile.getUserId() != null) {
            Mobile Mobile = target;
            if (resist) {
                this.communicationService.sendTextMessage(Mobile, String.format("\n\nYou resist %s's magic missile  for half damage!", mobile.getName()));
            }
            this.communicationService.sendTextMessage(Mobile, String.format("\n\n%s fires a magic missile at you for %d damage!", mobile.getName(), damage));
        }
        if (resist) {
            this.communicationService.roomMessage(mobile, String.format("\n\n%s's magic missile is resisted for half damage as it hits %s!", mobile.getName(), target.getName()));
        }
        this.communicationService.roomMessage(mobile, String.format("\n\n%s fires a magic missile at %s for %d damage!", mobile.getName(), target.getName(), damage));

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
            if (mobile.getUserId() != null) {
                this.communicationService.sendTextMessage(mobile, deathMsg);
            }
            if (target.getUserId() != null) {
                this.communicationService.sendTextMessage(target, "\n\nYou have died...");
                this.communicationService.sendTextMessage(target, deathMsg);
            }
            this.communicationService.roomMessage(target, deathMsg);

            this.characterService.setTarget(target, null);
            this.characterService.setTarget(mobile, null);

            // Clear hate
            this.characterService.findAllByRoomId(target.getCurrentRoomId())
                    .forEach(m -> m.removeHate(target.getId()));
        }
        return !resist;
    }
}


