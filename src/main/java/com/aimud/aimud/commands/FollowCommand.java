package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.CharacterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "follow")
@Component
public class FollowCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nFollow whom?");
            return Mono.empty();
        }
        
        String targetName = parts[1];
        
        if (targetName.equalsIgnoreCase("self")) {
            mobile.setFollowingId(null);
            communicationService.sendTextMessage(mobile, "\n\nYou stop following anyone.");
            return characterService.save(mobile).then();
        }
        
        Long roomId = mobile.getCurrentRoomId();

        // Check active characters in the room first
        Optional<Mobile> targetMobile = characterService.findAllByRoomId(roomId).stream()
                .filter(m -> m.getName().equalsIgnoreCase(targetName) && !m.getId().equals(mobile.getId()))
                .findFirst();

        if (targetMobile.isEmpty()) {
            // Check NPCs in the room
            targetMobile = mobileService.getMobilesInRoom(roomId).stream()
                    .filter(m -> m.getName().toLowerCase().contains(targetName.toLowerCase()))
                    .findFirst();
        }

        if (targetMobile.isPresent()) {
            Mobile target = targetMobile.get();
            mobile.setFollowingId(target.getId());
            communicationService.sendTextMessage(mobile, "\n\nYou start following " + target.getName() + ".");
            communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " starts following you.");
            return characterService.save(mobile).then();
        } else {
            communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
            return Mono.empty();
        }
    }

    @Override
    public String getDescription() {
        return "Follow another character or mobile.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: follow <name> | follow self\n\nAllows you to follow another character or mobile. If they move, you will follow them if possible. Use 'follow self' to stop following.";
    }
}
