package com.aimud.aimud.prayers;

import com.aimud.aimud.annontation.DivinePrayer;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;

@DivinePrayer(name = "minor healing")
public class MinorHealing extends Prayer {

    protected MinorHealing(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                           CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Minor Healing";
    }

    @Override
    public Long getPrayerId() {
        return 2001L; // Unique ID for prayers
    }

    @Override
    public Integer getPrayerLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Instantly heals a small amount of hit points. Usage: pray minor healing [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        if (target == null) {
            target = mobile;
        }

        int healAmount = getDamage(mobile); // Reuse getDamage for "power" calculation
        healAmount = Math.max(5, healAmount); // Minimum heal of 5

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
