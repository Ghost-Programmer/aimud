package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
/**
 * TakeCommand standard implementation layer.
 * Take an item from the room.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "take")
public class TakeCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing take command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nTake what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        return roomService.getRoom(Mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Check transient items first (corpses, etc.)
                    Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                            .filter(i -> i.getName().toLowerCase().contains(itemName))
                            .findFirst();

                    if (transientMatch.isPresent()) {
                        if (transientMatch.get().isNoPickup()) {
                            communicationService.sendTextMessage(Mobile, "\n\nYou cannot pick that up.");
                        }
                        // Even if noPickup is false for a transient item, picking up
                        // transient items is not yet implemented ??? treat as not allowed.
                        return Mono.empty();
                    }

                    List<Long> itemIds = room.getItemIds();
                    if (itemIds.isEmpty()) {
                        communicationService.sendTextMessage(Mobile, "\n\nYou don't see that here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(itemIds)
                            .flatMap(itemService::getItem)
                            .filter(item -> item.getName().toLowerCase().contains(itemName))
                            .next()
                            .flatMap(itemToTake -> {
                                if (itemToTake.isNoPickup()) {
                                    communicationService.sendTextMessage(Mobile, "\n\nYou cannot pick that up.");
                                    return Mono.empty();
                                }
                                return characterService.takeItem(Mobile, itemToTake.getId()).then();
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(Mobile, "\n\nYou don't see that here.");
                                return Mono.empty();
                            }));
                }).then();
    }

    @Override
    public String getDescription() {
        return "Take an item from the room.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: take <item>\n\nPicks up an item from the ground in your current room and adds it to your inventory.";
    }
}

