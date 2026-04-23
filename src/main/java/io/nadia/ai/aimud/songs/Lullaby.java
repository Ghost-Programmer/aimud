package io.nadia.ai.aimud.songs;

import io.nadia.ai.aimud.annontation.BardSong;
import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SkillService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the lullaby bard song.
 */
@BardSong(name = "lullaby")
public class Lullaby extends Song {

    public Lullaby(SkillService skillService, MobileService mobileService,
            CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Lullaby";
    }

    @Override
    public Long getSongId() {
        return 3014L;
    }

    @Override
    public Integer getSongLevel() {
        return 30;
    }

    @Override
    public String getDescription() {
        return "Sings a soothing melody that puts all mobiles with weaker charisma to sleep. Usage: sing lullaby";
    }

    @Override
    public Mobile getDefaultTarget(Mobile mobile) {
        // Defaults to self so SingCommand doesn't block it for lacking a target.
        return mobile;
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        List<Mobile> allInRoom = new ArrayList<>(mobileService.findAllByRoomId(mobile.getCurrentRoomId()));
        allInRoom.addAll(mobileService.findAllByRoomId(mobile.getCurrentRoomId()));

        boolean affectedAnyone = false;

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYou sing a soothing, gentle lullaby...");
        }
        communicationService.roomMessage(mobile,
                "\n" + mobile.getName() + " begins to sing a soothing, gentle lullaby...");

        Effect sleepEffect = this.effectService.getEffectByName("Lullaby Sleep").block();
        if (sleepEffect == null) {
            sleepEffect = new Effect();
            sleepEffect.setName("Lullaby Sleep");
            sleepEffect.setEffectType(io.nadia.ai.aimud.types.EffectType.SLEEPING);
            sleepEffect = this.effectService.saveEffect(sleepEffect).block();
        }

        int tickCount = Math.min(5,
                1 + (skillService.getSkillRank(mobile, io.nadia.ai.aimud.types.SkillsType.SING_SONG) / 20));

        for (Mobile m : allInRoom) {
            if (m.getId().equals(mobile.getId())) {
                continue;
            }

            // Put to sleep if their charisma is lower than the bard's
            if (m.getCharisma() < mobile.getCharisma()) {
                if (this.applyEffect(m, this.getSongSkillName(), sleepEffect, tickCount)) {
                    // Clear their target if they fall asleep
                    mobileService.setTarget(m, null);

                    affectedAnyone = true;

                    if (m.getUserId() != null) {
                        communicationService.sendTextMessage(m,
                                "\n\nYou feel your eyelids grow heavy and you drift off to sleep.");
                    }
                    communicationService.roomMessage(m, "\n" + m.getName() + " falls fast asleep.");
                }
            }
        }

        if (!affectedAnyone && mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYour lullaby doesn't seem to affect anyone here.");
        }

        return true;
    }
}

