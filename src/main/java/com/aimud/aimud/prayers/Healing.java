package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;

@DivinePrayer(name = "healing")
public class Healing extends Prayer {

    protected Healing(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                           CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Healing";
    }

    @Override
    public Long getPrayerId() {
        return 2011L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 25;
    }

    @Override
    public String getDescription() {
        return "Heals a standard amount of hit points. Usage: pray healing [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return super.getManaCost(mobile) * 10;
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target == null) {
            target = mobile;
        }

        int healAmount = getDamage(mobile) * 25; 

        int newHp = Math.min(target.getMaxHp(), target.getCurrentHp() + healAmount);
        int actualHeal = newHp - target.getCurrentHp();
        
        target.setCurrentHp(newHp);
        characterService.save(target).subscribe();

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, 
                String.format("\n\nYou lay your hands on %s and heal %d points of damage.", 
                mobile == target ? "yourself" : target.getName(), actualHeal));
        }

        if (target.getUserId() != null && mobile != target) {
            communicationService.sendTextMessage(target,
                String.format("\n\n%s lays their hands on you and heals %d points of damage.", 
                mobile.getName(), actualHeal));
        }

        return true;
    }
}
