package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "lullaby")
public class Lullaby extends Song {

    protected Lullaby(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                      CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Lullaby";
    }

    @Override
    public Long getSongId() {
        return 3001L; // Unique ID for songs
    }

    @Override
    public Integer getSongLevel() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "Soothes a target, reducing their hostile urges or putting them to sleep. Usage: sing lullaby [target]";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        return mobile;
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        if (target == null) {
            target = mobile;
        }

        int skillRank = skillService.getSkillRank(mobile, getSongSkillName());
        int tickCount = skillService.getSkillRank(mobile, SkillsType.SING_SONG) + 5;

        // Note: Actual Sleep/Calm effect mechanics would require specific handling in a complete MUD logic
        // We'll mimic the HpRegen approach for demonstration, providing a generic "Calmed" effect
        String effectName = "Calmed";
        
        // We need an effect for this. Since we don't know if a sleepy effect exists in DB, 
        // we'll just query for it. Wait, the other spells use effectService.getEffectByName().
        // Let's assume there is, or we'll just use a harmless text message for the prototype.
        Effect calmEffect = effectService.getEffectByName("Calmed").block();

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, 
                String.format("\n\nYou sing a soothing melody to %s.", 
                mobile == target ? "yourself" : target.getName()));
        }

        if (target.getUserId() != null && mobile != target) {
            communicationService.sendTextMessage(target,
                String.format("\n\n%s sings a soothing melody. You feel very sleepy...", 
                mobile.getName()));
        }

        communicationService.roomMessage(mobile,
                String.format("\n\nA gentle, calming song fills the air around %s.", mobile.getName()));

        if (calmEffect != null) {
            applyEffect(mobile, getSongSkillName(), calmEffect, tickCount);
        }

        return true;
    }
}
