package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "get")
public class GetCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing get command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 4);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nGet what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        // Handle "get <item> from <container>"
        if (parts.length >= 4 && parts[2].equalsIgnoreCase("from")) {
            String containerName = parts[3].toLowerCase();
            
            Optional<Item> containerOpt = Mobile.getInventory().stream()
                    .filter(i -> i.getName().toLowerCase().contains(containerName))
                    .findFirst();

            if (containerOpt.isEmpty()) {
                communicationService.sendTextMessage(Mobile, "\n\nYou don't have that container in your inventory.");
                return Mono.empty();
            }

            Item container = containerOpt.get();

            if (container.getItemType() != ItemType.CONTAINER) {
                communicationService.sendTextMessage(Mobile, "\n\n" + container.getName() + " is not a container.");
                return Mono.empty();
            }

            if (container.getInventory() == null || container.getInventory().isEmpty()) {
                communicationService.sendTextMessage(Mobile, "\n\n" + container.getName() + " is empty.");
                return Mono.empty();
            }

            Optional<Item> itemToGetOpt = container.getInventory().stream()
                    .filter(i -> i.getName().toLowerCase().contains(itemName))
                    .findFirst();

            if (itemToGetOpt.isEmpty()) {
                communicationService.sendTextMessage(Mobile, "\n\nYou don't see that in " + container.getName() + ".");
                return Mono.empty();
            }

            Item itemToGet = itemToGetOpt.get();
            
            // Move item from container to root inventory
            container.getInventory().remove(itemToGet);
            Mobile.getInventory().add(itemToGet);

            communicationService.sendTextMessage(Mobile, "\n\nYou get " + itemToGet.getName() + " from " + container.getName() + ".");
            return characterService.updateInventory(Mobile, Mobile.getInventory()).then();
        }

        // Handle standard "get <item>" (same as take)
        return roomService.getRoom(Mobile.getCurrentRoomId())
                .flatMap(room -> {
                    Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                            .filter(i -> i.getName().toLowerCase().contains(itemName))
                            .findFirst();

                    if (transientMatch.isPresent()) {
                        if (transientMatch.get().isNoPickup()) {
                            communicationService.sendTextMessage(Mobile, "\n\nYou cannot pick that up.");
                        }
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
        return "Get an item from the room or from a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: get <item>\n        get <item> from <container>\n\nPicks up an item from the ground, or takes an item out of a container in your inventory.";
    }
}
