package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@MudCommand(name = "reload", role = "MUD_ADMIN")
public class ReloadCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final StoreService storeService;

    public ReloadCommand(ApplicationContext context) {
        this.mobileService = context.getBean(MobileService.class);
        this.communicationService = context.getBean(CommunicationService.class);
        this.roomService = context.getBean(RoomService.class);
        this.storeService = context.getBean(StoreService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        String target = parts.length > 1 ? parts[1].toLowerCase() : "";

        return switch (target) {
            case "rooms" -> roomService.reloadAllRooms()
                    .doOnSuccess(v -> communicationService.sendTextMessage(mobile, "\n\nAll rooms have been reloaded from the database."))
                    .then();
            case "npcs" -> mobileService.reloadAllNPCs()
                    .doOnSuccess(v -> communicationService.sendTextMessage(mobile, "\n\nAll NPCs have been reloaded from the database."))
                    .then();
            case "stores" -> storeService.reloadAllStores()
                    .doOnSuccess(v -> communicationService.sendTextMessage(mobile, "\n\nAll stores have been reloaded from the database."))
                    .then();
            case "" -> {
                Long roomId = mobile.getCurrentRoomId();
                if (roomId == null) {
                    communicationService.sendTextMessage(mobile, "\n\nYou are not in a room.");
                    yield Mono.empty();
                }

                List<Long> storeIds = mobileService.findAllByRoomId(roomId).stream()
                        .filter(m -> m.getUserId() == null && m.getStoreId() != null)
                        .map(Mobile::getStoreId)
                        .distinct()
                        .collect(Collectors.toList());

                Mono<Void> reloadStoresMono = Flux.fromIterable(storeIds)
                        .flatMap(storeService::reloadStore)
                        .then();

                yield roomService.reloadRoom(roomId)
                        .then(mobileService.reloadRoomNPCs(roomId))
                        .then(reloadStoresMono)
                        .doOnSuccess(v -> communicationService.sendTextMessage(mobile, "\n\nCurrent room, its NPCs, and their associated stores have been reloaded."))
                        .then();
            }
            default -> {
                communicationService.sendTextMessage(mobile, "\n\nInvalid reload target. Use: reload [rooms|npcs|stores|].");
                yield Mono.empty();
            }
        };
    }

    @Override
    public String getDescription() {
        return "Reloads rooms, npcs, stores, or the current room from the database.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: reload [rooms|npcs|stores]\n\n" +
               "reload rooms  - Loads all rooms from the database.\n" +
               "reload npcs   - Reloads all NPCs from the database and removes all of the ones in memory.\n" +
               "reload stores - Reloads all stores from the database.\n" +
               "reload        - Reloads the current room, any NPCs for that room, and any stores associated with NPCs in that room.";
    }
}
