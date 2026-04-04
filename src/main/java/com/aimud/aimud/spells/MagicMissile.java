package com.aimud.aimud.spells;

import com.aimud.aimud.Dice;
import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;

@MagicSpell(name = "missile")
public class MagicMissile extends Spell {


    protected MagicMissile(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Magic Missile";
    }

    @Override
    public Long getSpellId() {
        return 1000L;
    }

    @Override
    public Integer getSpellLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Fires a bolt of magic at your target dealing <skill> d 6 damage + bonus damage. Damage increases at higher levels. Usage: cast missile <target>";
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {

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

        return !resist;
    }
}

