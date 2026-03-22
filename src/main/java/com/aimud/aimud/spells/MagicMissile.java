package com.aimud.aimud.spells;

import com.aimud.aimud.annontation.MagicSpell;

@MagicSpell(name = "missile")
public class MagicMissile implements Spell {
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
}
