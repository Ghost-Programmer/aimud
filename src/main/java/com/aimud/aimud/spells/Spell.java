package com.aimud.aimud.spells;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.types.SkillsType;

public abstract class Spell {

    private final SkillService skillService;

    protected Spell(SkillService skillService) {
        this.skillService = skillService;
    }

    abstract public String getSpellName();
    abstract public Long getSpellId();
    abstract public Integer getSpellLevel();
    abstract public String getDescription();
    abstract public Mobile getTarget(Mobile mobile, String[] parts);
    abstract public boolean cast(Mobile mobile, Spell spell, Mobile target);


    public String getSpellSkillName() {
        return "Spell: " + this.getSpellName();
    }

    public Integer getManaCost(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC);
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());

        return 9 + spellSkill + (castSkill - getSpellLevel()) * 2;
    }
}

