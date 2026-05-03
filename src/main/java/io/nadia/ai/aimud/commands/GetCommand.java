package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@MudCommand(name = "get")
public class GetCommand implements Command {
    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing get command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 4);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nGet what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        // Handle "get <item> from <container>"
        if (parts.length >= 4 && parts[2].equalsIgnoreCase("from")) {
            String containerName = parts[3].toLowerCase();
            
            Optional<Item> containerOpt = mobile.getInventory().stream()
                    .filter(i -> i.getName().toLowerCase().contains(containerName))
                    .findFirst();

            if (containerOpt.isPresent()) {
                return processGetFromContainer(mobile, itemName, containerOpt.get(), true);
            }

            // Not in inventory, check room
            return roomService.getRoom(mobile.getCurrentRoomId())
                    .flatMap(room -> {
                        Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                                .filter(i -> i.getName().toLowerCase().contains(containerName))
                                .findFirst();

                        if (transientMatch.isPresent()) {
                            return processGetFromContainer(mobile, itemName, transientMatch.get(), false);
                        }

                        if (room.getItemIds().isEmpty()) {
                            communicationService.sendTextMessage(mobile, "\n\nYou don't see that container here.");
                            return Mono.empty();
                        }

                        return Flux.fromIterable(room.getItemIds())
                                .flatMap(itemService::getItem)
                                .filter(item -> item.getName().toLowerCase().contains(containerName))
                                .next()
                                .flatMap(containerItem -> processGetFromContainer(mobile, itemName, containerItem, false))
                                .switchIfEmpty(Mono.defer(() -> {
                                    communicationService.sendTextMessage(mobile, "\n\nYou don't see that container here.");
                                    return Mono.empty();
                                }));
                    }).then();
        }

        // Handle standard "get <item>" (same as take)
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                            .filter(i -> i.getName().toLowerCase().contains(itemName))
                            .findFirst();

                    if (transientMatch.isPresent()) {
                        if (transientMatch.get().isNoPickup()) {
                            communicationService.sendTextMessage(mobile, "\n\nYou cannot pick that up.");
                        }
                        return Mono.empty();
                    }

                    List<Long> itemIds = room.getItemIds();
                    if (itemIds.isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nYou don't see that here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(itemIds)
                            .flatMap(itemService::getItem)
                            .filter(item -> item.getName().toLowerCase().contains(itemName))
                            .next()
                            .flatMap(itemToTake -> {
                                if (itemToTake.isNoPickup()) {
                                    communicationService.sendTextMessage(mobile, "\n\nYou cannot pick that up.");
                                    return Mono.empty();
                                }
                                return mobileService.takeItem(mobile, itemToTake.getId()).then();
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nYou don't see that here.");
                                return Mono.empty();
                            }));
                }).then();
    }

    private Mono<Void> processGetFromContainer(Mobile mobile, String itemName, Item container, boolean inInventory) {
        if (container.getItemType() != ItemType.CONTAINER) {
            communicationService.sendTextMessage(mobile, "\n\n" + container.getName() + " is not a container.");
            return Mono.empty();
        }

        if (container.getInventory() == null || container.getInventory().isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\n" + container.getName() + " is empty.");
            return Mono.empty();
        }

        Optional<Item> itemToGetOpt = container.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToGetOpt.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't see that in " + container.getName() + ".");
            return Mono.empty();
        }

        Item itemToGet = itemToGetOpt.get();
        
        container.getInventory().remove(itemToGet);
        mobile.getInventory().add(itemToGet);

        communicationService.sendTextMessage(mobile, "\n\nYou get " + itemToGet.getName() + " from " + container.getName() + ".");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " gets " + itemToGet.getName() + " from " + container.getName() + ".");

        if (inInventory) {
            return mobileService.updateInventory(mobile, mobile.getInventory()).then();
        } else {
            return Mono.when(
                    mobileService.updateInventory(mobile, mobile.getInventory()),
                    itemService.saveItem(container)
            ).then();
        }
    }

    @Override
    public String getDescription() {
        return "Get an item from the room or from a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: get <item>\n        get <item> from <container>\n\nPicks up an item from the ground, or takes an item out of a container in your inventory or on the ground.";
    }
}
