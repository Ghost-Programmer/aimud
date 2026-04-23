package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.annontation.MagicSpell;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SkillService;

/**
 * Implementation of the Recall spell.
 * Pulls a group member to the caster's room.
 */
@MagicSpell(name = "recall")
public class Recall extends Spell {

    public Recall(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService) {
        super(skillService, mobileService, communicationService, effectService);
    }

    @Override
    public String getSpellName() {
        return "Recall";
    }

    @Override
    public Long getSpellId() {
        return 1032L;
    }

    @Override
    public Integer getSpellLevel() {
        return 30; // Requires CAST_MAGIC level 30
    }

    @Override
    public String getDescription() {
        return "Opens a portal and pulls a group member to your location. Usage: cast recall <player>";
    }

    @Override
    public Mobile getTarget(Mobile mobile, String[] parts) {
        if (parts.length >= 3) {
            String name = parts[2].toLowerCase();
            // Search all available characters in the game for the target, not just the current room
            return mobileService.getAvailableCharacters().stream()
                    .filter(c -> c.getName().toLowerCase().contains(name))
                    .filter(c -> isSameGroup(mobile, c))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private boolean isSameGroup(Mobile caster, Mobile target) {
        if (caster.getId().equals(target.getId())) return true;
        
        Long casterLeader = caster.getPartyLeaderId();
        Long targetLeader = target.getPartyLeaderId();

        // They are in the same group if they share a leader
        if (casterLeader != null && casterLeader.equals(targetLeader)) return true;
        // Or if the caster is the leader and the target follows them
        if (casterLeader == null && targetLeader != null && targetLeader.equals(caster.getId())) return true;
        // Or if the target is the leader and the caster follows them
        if (targetLeader == null && casterLeader != null && casterLeader.equals(target.getId())) return true;
        
        return false;
    }

    @Override
    public boolean cast(Mobile mobile, Spell spell, Mobile target) {
        if (target == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou must specify a valid group member to recall. Target was not found in your group.");
            return false;
        }

        if (target.getCurrentRoomId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou cannot locate them.");
            return false;
        }

        if (target.getCurrentRoomId().equals(mobile.getCurrentRoomId())) {
            communicationService.sendTextMessage(mobile, "\n\nThey are already here!");
            return false;
        }

        if (mobile.getUserId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYou open a glowing portal and pull " + target.getName() + " through it!");
        }
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " opens a glowing portal and pulls " + target.getName() + " through it!");

        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n\nA glowing portal opens before you and pulls you in!");
        }
        
        // Notify the target's old room
        mobileService.findAllByRoomId(target.getCurrentRoomId()).stream()
                .filter(m -> !m.getId().equals(target.getId()))
                .forEach(m -> communicationService.sendTextMessage(m, "\n\nA glowing portal opens and pulls " + target.getName() + " inside!"));

        // Teleport the target
        mobileService.enterRoom(target, mobile.getCurrentRoomId()).subscribe();

        return true;
    }
}

