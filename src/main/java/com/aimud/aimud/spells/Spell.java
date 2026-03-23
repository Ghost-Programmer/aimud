package com.aimud.aimud.spells;

import com.aimud.aimud.Dice;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

public abstract class Spell {

    protected final SkillService skillService;
    protected final MobileService mopbileService;
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;

    protected Spell(SkillService skillService, MobileService mopbileService, CharacterService characterService, CommunicationService communicationService) {
        this.skillService = skillService;
        this.mopbileService = mopbileService;
        this.characterService = characterService;
        this.communicationService = communicationService;
    }


    abstract public String getSpellName();
    abstract public Long getSpellId();
    abstract public Integer getSpellLevel();
    abstract public String getDescription();
    abstract public boolean cast(Mobile mobile, Spell spell, Mobile target);


    public String getSpellSkillName() {
        return "Spell: " + this.getSpellName();
    }

    public Integer getManaCost(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC);
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());

        return 9 + spellSkill + (castSkill - getSpellLevel());
    }

    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile.getTarget();
    }

    public Mobile getTarget(Mobile mobile, String[] parts) {
        if (parts.length == 2) {
            return this.getDefaultTarget(mobile);
        }
        if (parts.length > 3) {

            String name =  parts[2].toLowerCase();

            Mobile target = mopbileService.getMobilesInRoom(mobile.getCurrentRoomId()).stream()
                    .filter(m -> m.getName().toLowerCase().contains(name))
                    .findFirst()
                    .orElse(null);

            if(target != null) {
                return target;
            }

            return characterService.findAllByRoomId(mobile.getCurrentRoomId()).stream()
                    .filter(c -> c.getName().toLowerCase().contains(name))
                    .findFirst()
                    .orElse(null);

        }
        return null;
    }

    int getDamage(Mobile mobile) {
        int castSkill = skillService.getSkillRank(mobile, SkillsType.CAST_MAGIC);
        int spellSkill = skillService.getSkillRank(mobile, getSpellSkillName());

        int dice = (9 + spellSkill + (castSkill - getSpellLevel())) / 6;

       return new Dice(dice, 6).getTotal() + (9 + spellSkill + (castSkill - getSpellLevel())) % 6;
    }
}

