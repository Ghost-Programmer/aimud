package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.annontation.DivinePrayer;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.service.*;

@DivinePrayer(name = "major healing")
public class MajorHealing extends Prayer {

    protected MajorHealing(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                           CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getPrayerName() {
        return "Major Healing";
    }

    @Override
    public Long getPrayerId() {
        return 2009L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 50;
    }

    @Override
    public String getDescription() {
        return "Heals a major amount of hit points. Usage: pray major healing [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return super.getManaCost(mobile) * 20;
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

        int healAmount = getDamage(mobile) * 50; 

        int newHp = Math.min(target.getMaxHp(), target.getCurrentHp() + healAmount);
        int actualHeal = newHp - target.getCurrentHp();
        
        target.setCurrentHp(newHp);
        characterService.save(target).subscribe();

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, 
                String.format("\n\nYou lay your hands on %s and heal %d points of damage with major force.", 
                mobile == target ? "yourself" : target.getName(), actualHeal));
        }

        if (target.getUserId() != null && mobile != target) {
            communicationService.sendTextMessage(target,
                String.format("\n\n%s lays their hands on you and heals %d points of damage with major force.", 
                mobile.getName(), actualHeal));
        }

        return true;
    }
}
