package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
/**
 * FollowCommand standard implementation layer.
 * Follow another character or mobile.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "follow")
@Component
public class FollowCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
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
            return mobileService.save(mobile).then();
        }
        
        Long roomId = mobile.getCurrentRoomId();

        // Check active characters in the room first
        Optional<Mobile> targetMobile = mobileService.findAllByRoomId(roomId).stream()
                .filter(m -> m.getName().equalsIgnoreCase(targetName) && !m.getId().equals(mobile.getId()) && !m.isHidden() && !m.isInvisible())
                .findFirst();

        if (targetMobile.isEmpty()) {
            // Check NPCs in the room
            targetMobile = mobileService.findAllByRoomId(roomId).stream()
                    .filter(m -> m.getName().toLowerCase().contains(targetName.toLowerCase()) && !m.isHidden() && !m.isInvisible())
                    .findFirst();
        }

        if (targetMobile.isPresent()) {
            Mobile target = targetMobile.get();
            mobile.setFollowingId(target.getId());
            communicationService.sendTextMessage(mobile, "\n\nYou start following " + target.getName() + ".");
            communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " starts following you.");
            return mobileService.save(mobile).then();
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

