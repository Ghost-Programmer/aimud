package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.service.StatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@MudCommand(name = "load", role = "MUD_ADMIN")
public class LoadCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final ItemService itemService;
    private final StatService statService;

    public LoadCommand(ApplicationContext context) {
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.roomService = context.getBean(RoomService.class);
        this.itemService = context.getBean(ItemService.class);
        this.statService = context.getBean(StatService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 3);
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: load <npc|item|room> <id>");
            return Mono.empty();
        }

        String type = parts[1].toLowerCase();
        Long id;
        try {
            id = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            communicationService.sendTextMessage(mobile, "\n\nInvalid ID. Must be a number.");
            return Mono.empty();
        }

        Long adminRoomId = mobile.getCurrentRoomId();
        if (adminRoomId == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou are not in a room.");
            return Mono.empty();
        }

        return switch (type) {
            case "item" -> loadItem(mobile, adminRoomId, id);
            case "room" -> loadRoom(mobile, id);
            case "npc" -> loadNpc(mobile, adminRoomId, id);
            default -> {
                communicationService.sendTextMessage(mobile,
                        "\n\nInvalid load target. Use: load <npc|item|room> <id>.");
                yield Mono.empty();
            }
        };
    }

    private Mono<Void> loadItem(Mobile admin, Long roomId, Long itemId) {
        return itemService.getItem(itemId)
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(admin, "\n\nItem ID " + itemId + " not found.");
                    return Mono.empty();
                }))
                .flatMap(item -> roomService.addItemToRoom(roomId, itemId)
                        .doOnSuccess(r -> communicationService.sendTextMessage(admin,
                                "\n\nYou have loaded " + item.getName() + " into the room."))
                        .then());
    }

    private Mono<Void> loadRoom(Mobile admin, Long roomId) {
        return roomService.getRoom(roomId)
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(admin, "\n\nRoom ID " + roomId + " not found.");
                    return Mono.empty();
                }))
                .flatMap(room -> {
                    communicationService.sendTextMessage(admin, "\n\nYou vanish in a puff of smoke.");
                    return mobileService.enterRoom(admin, room.getId());
                });
    }

    private Mono<Void> loadNpc(Mobile admin, Long adminRoomId, Long npcId) {
        Optional<Mobile> activeNpcOpt = mobileService.getAvailableMobiles().stream()
                .filter(m -> m.getId().equals(npcId))
                .findFirst();

        if (activeNpcOpt.isPresent()) {
            Mobile existingNpc = activeNpcOpt.get();
            communicationService.sendTextMessage(admin,
                    "\n\nMoving active NPC " + existingNpc.getName() + " to your room.");
            return mobileService.enterRoom(existingNpc, adminRoomId);
        }

        return mobileService.getMobile(npcId)
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(admin, "\n\nNPC ID " + npcId + " not found.");
                    return Mono.empty();
                }))
                .flatMap(statService::updateCurrentStats)
                .flatMap(npc -> {
                    npc.setCurrentRoomId(adminRoomId);
                    return mobileService.saveMobile(npc)
                            .then(mobileService.selectCharacter(npcId))
                            .doOnSuccess(v -> communicationService.sendTextMessage(admin,
                                    "\n\nLoaded NPC " + npc.getName() + " from database to your room."));
                });
    }

    @Override
    public String getDescription() {
        return "Load an item, NPC, or teleport to a room.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: load <npc|item|room> <id>\n\n" +
                "load npc <id>  - Moves an existing NPC or loads it from the database to your current room.\n" +
                "load item <id> - Creates a new instance of an item and adds it to your current room.\n" +
                "load room <id> - Fetches the room and teleports you to it.";
    }
}
