package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * GotoCommand for MUD admins.
 * Instantly teleport to a specific room ID or another player.
 */
@Slf4j
@MudCommand(name = "goto", role = "MUD_ADMIN")
public class GotoCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final RoomService roomService;

    public GotoCommand(ApplicationContext context) {
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.roomService = context.getBean(RoomService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: goto <roomID|playerName>");
            return Mono.empty();
        }

        String target = parts[1];

        // Try to parse target as Room ID
        try {
            Long roomId = Long.parseLong(target);
            return teleportToRoom(mobile, roomId)
                    .switchIfEmpty(Mono.defer(() -> {
                        // Room didn't exist, maybe there's a player with a numeric name?
                        return teleportToPlayer(mobile, target);
                    }));
        } catch (NumberFormatException e) {
            // Target is not a number, search for player
            return teleportToPlayer(mobile, target);
        }
    }

    private Mono<Void> teleportToRoom(Mobile mobile, Long roomId) {
        return roomService.getRoom(roomId)
                .flatMap(room -> {
                    communicationService.sendTextMessage(mobile, "\n\nYou vanish in a puff of smoke.");
                    return mobileService.enterRoom(mobile, room.getId());
                });
    }

    private Mono<Void> teleportToPlayer(Mobile mobile, String playerName) {
        Optional<Mobile> targetPlayer = mobileService.getAvailableMobiles().stream()
                .filter(c -> c.getName().equalsIgnoreCase(playerName))
                .findFirst();

        if (targetPlayer.isPresent()) {
            Long targetRoomId = targetPlayer.get().getCurrentRoomId();
            if (targetRoomId != null) {
                communicationService.sendTextMessage(mobile, "\n\nYou vanish in a puff of smoke.");
                return mobileService.enterRoom(mobile, targetRoomId);
            }
        }

        communicationService.sendTextMessage(mobile, "\n\nTarget not found.");
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Teleport instantly to a location or player.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: goto <roomID|playerName>\n\nTeleports you instantly to the specified room ID or to the room where the specified player is currently located.";
    }
}
