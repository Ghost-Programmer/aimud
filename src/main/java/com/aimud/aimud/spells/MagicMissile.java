package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.SkillService;

@MagicSpell(name = "missile")
public class MagicMissile extends Spell {

    protected MagicMissile(SkillService skillService) {
        super(skillService);
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
        return "Fires a bolt of magic at your target dealing <skill> d 6 damage + bonus damage. Damage increases at higher levels. Usage: cast missle <target>";
    }

    @Override
    public Mobile getTarget(Mobile mobile, String[] parts) {
        return null;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {

        return false;
    }
}
