package com.aimud.aimud.songs;

import com.aimud.aimud.annontation.BardSong;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.*;
import com.aimud.aimud.types.SkillsType;

@BardSong(name = "magic_drain")
public class ManaTransferSong extends Song {

    protected ManaTransferSong(SkillService skillService, MobileService mopbileService,
            CharacterService characterService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mopbileService, characterService, communicationService, effectService);
    }

    @Override
    public String getSongName() {
        return "Magic Drain Song";
    }

    @Override
    public Long getSongId() {
        return 3008L;
    }

    @Override
    public Integer getSongLevel() {
        return 15;
    }

    @Override
    public String getDescription() {
        return "Sacrifices the bard's own mana to invigorate all other party members immediately. Usage: sing magic_drain";
    }

    @Override
    public Integer getManaCost(Mobile mobile) {
        return 0;
    }

    @Override
    public boolean sing(Mobile mobile, Song song, Mobile target) {
        if (target != null && !this.characterService.canTarget(target)) {
            this.communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + target.getName() + ".");
            return false;
        }

        int skillRank = skillService.getSkillRank(mobile, getSongSkillName());
        if (skillRank <= 0) {
            skillRank = 1;
        }

        Long leaderId = mobile.getPartyLeaderId();
        int partySize = 1;

        if (leaderId != null) {
            long count = characterService.findAllByRoomId(mobile.getCurrentRoomId()).stream()
                    .filter(m -> leaderId.equals(m.getPartyLeaderId())).count();
            count += mopbileService.getMobilesInRoom(mobile.getCurrentRoomId()).stream()
                    .filter(m -> leaderId.equals(m.getPartyLeaderId())).count();
            partySize = (int) count;
        }

        int otherMembers = partySize - 1;

        // Formula: skillRank + 5, up to a maximum of 100 mana
        int totalDesiredDrain = Math.min(100, skillRank + 5);

        if (mobile.getId().equals(target.getId())) {
            // Apply the actual drain to the Bard
            int current = mobile.getCurrentMana();
            int drain = Math.min(current, totalDesiredDrain);

            mobile.setCurrentMana(Math.max(0, current - totalDesiredDrain));

            if (mobile.getUserId() != null) {
                if (otherMembers <= 0) {
                    communicationService.sendTextMessage(mobile, String.format(
                            "\n\nYou violently sacrifice %d mana, but there is no one to share it with.", drain));
                } else {
                    communicationService.sendTextMessage(mobile, String
                            .format("\n\nYou playfully weave %d mana into the air to invigorate your party.", drain));
                }
            }
        } else if (otherMembers > 0) {
            // Apply the granted mana to the Ally
            int splitAmount = totalDesiredDrain / otherMembers;
            int max = target.getMaxMana();
            int current = target.getCurrentMana();

            int newMana = Math.min(max, current + splitAmount);
            target.setCurrentMana(newMana);

            if (target.getUserId() != null) {
                communicationService.sendTextMessage(target,
                        String.format("\n\n%s's magic drain unconditionally restores %d mana to you!", mobile.getName(),
                                splitAmount));
            }
        }

        return true;
    }
}
