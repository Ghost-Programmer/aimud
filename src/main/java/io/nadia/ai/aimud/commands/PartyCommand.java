package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PartyCommand standard implementation layer.
 * Manage your party.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "party")
public class PartyCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;

    @Override
    /**
     * 
     * Execute sequence logic maps.
     * 
     * @param Mobile      local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile,
                    "\n\nParty commands: \n party invite <name> \n party accept \n party decline \n party leave \n party remove <name> \n party list");
            return Mono.empty();
        }

        String action = parts[1].toLowerCase();

        switch (action) {
            case "invite":
                handleInvite(mobile, parts);
                break;
            case "accept":
            case "join":
                handleAccept(mobile);
                break;
            case "decline":
                handleDecline(mobile);
                break;
            case "leave":
                handleLeave(mobile);
                break;
            case "remove":
                handleRemove(mobile, parts);
                break;
            case "list":
                handleList(mobile);
                break;
            default:
                communicationService.sendTextMessage(mobile,
                        "\n\nUnknown party command. Valid options: invite, accept, decline, leave, remove, list.");
        }

        return Mono.empty();
    }

    private List<Mobile> getAllActiveMobiles() {
        List<Mobile> allMobiles = new ArrayList<>(mobileService.getAvailableMobiles());
        allMobiles.addAll(mobileService.getAvailableMobiles());
        return allMobiles;
    }

    private void handleInvite(Mobile mobile, String[] parts) {
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile,
                    "\n\nYou must specify who you want to invite: party invite <name>");
            return;
        }

        String targetName = parts[2].toLowerCase();

        Mobile target = mobileService.findAllByRoomId(mobile.getCurrentRoomId()).stream()
                .filter(c -> c.getName().toLowerCase().startsWith(targetName))
                .findFirst()
                .orElse(null);

        if (target == null) {
            target = mobileService.findAllByRoomId(mobile.getCurrentRoomId()).stream()
                    .filter(m -> m.getName().toLowerCase().startsWith(targetName))
                    .findFirst()
                    .orElse(null);
        }

        if (target == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't see them here.");
            return;
        }

        if (target.getId().equals(mobile.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou can't invite yourself.");
            return;
        }

        if (target.getPartyLeaderId() != null) {
            communicationService.sendTextMessage(mobile, "\n\n" + target.getName() + " is already in a party.");
            return;
        }

        if (mobile.getPartyLeaderId() == null) {
            mobile.setPartyLeaderId(mobile.getId());
        }

        if (!mobile.getId().equals(mobile.getPartyLeaderId())) {
            communicationService.sendTextMessage(mobile, "\n\nOnly the party leader can invite others.");
            return;
        }

        target.setPendingPartyInviteId(mobile.getId());

        communicationService.sendTextMessage(mobile, "\n\nYou have invited " + target.getName() + " to the party.");
        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target, "\n\n" + mobile.getName()
                    + " has invited you to join their party. Type 'party accept' or 'party decline'.");
        }
    }

    private void handleAccept(Mobile mobile) {
        if (mobile.getPendingPartyInviteId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou have no pending party invites.");
            return;
        }

        if (mobile.getPartyLeaderId() != null) {
            communicationService.sendTextMessage(mobile, "\n\nYou are already in a party. Type 'party leave' first.");
            return;
        }

        Long leaderId = mobile.getPendingPartyInviteId();
        mobile.setPartyLeaderId(leaderId);
        mobile.setPendingPartyInviteId(null);

        communicationService.sendTextMessage(mobile, "\n\nYou join the party.");

        for (Mobile m : getAllActiveMobiles()) {
            if (leaderId.equals(m.getPartyLeaderId()) && m.getUserId() != null && !m.getId().equals(mobile.getId())) {
                communicationService.sendTextMessage(m, "\n\n" + mobile.getName() + " has joined the party.");
            }
        }
    }

    private void handleDecline(Mobile mobile) {
        if (mobile.getPendingPartyInviteId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou have no pending party invites.");
            return;
        }

        Long leaderId = mobile.getPendingPartyInviteId();
        mobile.setPendingPartyInviteId(null);
        communicationService.sendTextMessage(mobile, "\n\nYou decline the party invite.");

        for (Mobile m : getAllActiveMobiles()) {
            if (m.getId().equals(leaderId) && m.getUserId() != null) {
                communicationService.sendTextMessage(m, "\n\n" + mobile.getName() + " declined your party invite.");
            }
        }
    }

    private void handleLeave(Mobile mobile) {
        if (mobile.getPartyLeaderId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou are not in a party.");
            return;
        }

        Long leaderId = mobile.getPartyLeaderId();

        if (mobile.getId().equals(leaderId)) {
            communicationService.sendTextMessage(mobile, "\n\nYou disband the party.");

            for (Mobile m : getAllActiveMobiles()) {
                if (leaderId.equals(m.getPartyLeaderId())) {
                    m.setPartyLeaderId(null);
                    if (!m.getId().equals(mobile.getId()) && m.getUserId() != null) {
                        communicationService.sendTextMessage(m,
                                "\n\n" + mobile.getName() + " has disbanded the party.");
                    }
                }
            }
        } else {
            mobile.setPartyLeaderId(null);
            communicationService.sendTextMessage(mobile, "\n\nYou leave the party.");

            for (Mobile m : getAllActiveMobiles()) {
                if (leaderId.equals(m.getPartyLeaderId()) && m.getUserId() != null) {
                    communicationService.sendTextMessage(m, "\n\n" + mobile.getName() + " has left the party.");
                }
            }
        }
    }

    private void handleRemove(Mobile mobile, String[] parts) {
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile,
                    "\n\nYou must specify who you want to remove: party remove <name>");
            return;
        }

        if (mobile.getPartyLeaderId() == null || !mobile.getId().equals(mobile.getPartyLeaderId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou must be the party leader to remove someone.");
            return;
        }

        String targetName = parts[2].toLowerCase();
        Mobile target = null;

        for (Mobile m : getAllActiveMobiles()) {
            if (mobile.getId().equals(m.getPartyLeaderId()) && m.getName().toLowerCase().startsWith(targetName)) {
                target = m;
                break;
            }
        }

        if (target == null) {
            communicationService.sendTextMessage(mobile, "\n\nThey are not in your party.");
            return;
        }

        if (target.getId().equals(mobile.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou can't remove yourself. Use 'party leave'.");
            return;
        }

        target.setPartyLeaderId(null);

        communicationService.sendTextMessage(mobile, "\n\nYou have removed " + target.getName() + " from the party.");
        if (target.getUserId() != null) {
            communicationService.sendTextMessage(target,
                    "\n\n" + mobile.getName() + " has removed you from the party.");
        }

        for (Mobile m : getAllActiveMobiles()) {
            if (mobile.getId().equals(m.getPartyLeaderId()) && m.getUserId() != null) {
                communicationService.sendTextMessage(m,
                        "\n\n" + mobile.getName() + " removed " + target.getName() + " from the party.");
            }
        }
    }

    private void handleList(Mobile mobile) {
        if (mobile.getPartyLeaderId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou are not in a party.");
            return;
        }

        Long leaderId = mobile.getPartyLeaderId();

        List<Mobile> partyMembers = getAllActiveMobiles().stream()
                .filter(m -> leaderId.equals(m.getPartyLeaderId()))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n=== Party Members ===\n");

        for (Mobile m : partyMembers) {
            sb.append(m.getName());
            if (m.getId().equals(leaderId)) {
                sb.append(" (Leader)");
            }
            sb.append("\n");
        }

        communicationService.sendTextMessage(mobile, sb.toString());
    }

    @Override
    public String getDescription() {
        return "Manage your party.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: party invite <name> | party accept | party decline | party remove <name> | party leave | party list\n\nAllows you to form a party, invite characters, or check who is grouped with you.";
    }
}
