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
 * TransferCommand for MUD admins.
 * Forcefully move a player to a specific room ID.
 */
@Slf4j
@MudCommand(name = "transfer", role = "MUD_ADMIN")
public class TransferCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final RoomService roomService;

    public TransferCommand(ApplicationContext context) {
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.roomService = context.getBean(RoomService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 3);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: transfer <playerName> [roomID]");
            return Mono.empty();
        }

        String playerName = parts[1];
        String targetStr = parts.length > 2 ? parts[2] : null;

        Optional<Mobile> targetPlayerOpt = mobileService.getAvailableMobiles().stream()
                .filter(c -> c.getName().equalsIgnoreCase(playerName))
                .findFirst();

        if (targetPlayerOpt.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nPlayer '" + playerName + "' not found or not online.");
            return Mono.empty();
        }

        Mobile targetPlayer = targetPlayerOpt.get();
        Long roomId;

        if (targetStr != null) {
            try {
                roomId = Long.parseLong(targetStr);
            } catch (NumberFormatException e) {
                communicationService.sendTextMessage(mobile, "\n\nInvalid room ID: " + targetStr);
                return Mono.empty();
            }
        } else {
            roomId = mobile.getCurrentRoomId();
        }

        return roomService.getRoom(roomId)
                .flatMap(room -> {
                    communicationService.sendTextMessage(mobile,
                            "\n\nYou transfer " + targetPlayer.getName() + " to room " + room.getId() + ".");
                    communicationService.sendTextMessage(targetPlayer,
                            "\n\nYou have been forcefully transferred by an administrator.");
                    return mobileService.enterRoom(targetPlayer, room.getId());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(mobile, "\n\nRoom ID " + roomId + " does not exist.");
                    return Mono.empty();
                }));
    }

    @Override
    public String getDescription() {
        return "Forcefully move a player to a new location.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: transfer <playerName> [roomID]\n\nTeleports the specified player instantly to the specified room ID. If no room ID is provided, teleports them to your current room. This is an administrative command.";
    }
}
