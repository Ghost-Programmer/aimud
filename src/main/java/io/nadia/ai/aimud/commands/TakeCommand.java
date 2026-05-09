package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
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
    private final MobileService mobileService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing take command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nTake what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        String containerName = null;
        if (parts.length >= 4 && parts[2].equalsIgnoreCase("from")) {
            containerName = parts[3].toLowerCase();
        }

        if (containerName != null) {
            return handleTakeFromContainer(mobile, itemName, containerName);
        } else {
            return handleTakeFromRoom(mobile, itemName);
        }
    }

    private Mono<Void> handleTakeFromContainer(Mobile mobile, String itemName, String containerName) {
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // find container in room or mobile inventory
                    return Flux.fromIterable(room.getItemIds())
                            .flatMap(itemService::getItem)
                            .filter(i -> i.getName().toLowerCase().contains(containerName))
                            .next()
                            .switchIfEmpty(Mono.defer(() -> {
                                return Flux.fromIterable(mobile.getInventory())
                                        .filter(i -> i.getName().toLowerCase().contains(containerName))
                                        .next();
                            }))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nYou don't see that container here.");
                                return Mono.empty();
                            }))
                            .flatMap(container -> {
                                if (container.getItemType() != io.nadia.ai.aimud.types.ItemType.CONTAINER) {
                                    communicationService.sendTextMessage(mobile, "\n\nThat is not a container.");
                                    return Mono.empty();
                                }
                                if (container.getInventory() == null || container.getInventory().isEmpty()) {
                                    communicationService.sendTextMessage(mobile, "\n\nIt is empty.");
                                    return Mono.empty();
                                }
                                
                                Item itemToTake = container.getInventory().stream()
                                        .filter(i -> i.getName().toLowerCase().contains(itemName))
                                        .findFirst()
                                        .orElse(null);
                                        
                                if (itemToTake == null) {
                                    communicationService.sendTextMessage(mobile, "\n\nYou don't see that in there.");
                                    return Mono.empty();
                                }
                                if (itemToTake.isNoPickup()) {
                                    communicationService.sendTextMessage(mobile, "\n\nYou cannot pick that up.");
                                    return Mono.empty();
                                }
                                
                                // remove from container
                                container.getInventory().remove(itemToTake);
                                
                                if (itemToTake.getItemType() == io.nadia.ai.aimud.types.ItemType.MONEY) {
                                    int goldAmount = itemToTake.getProperty1();
                                    mobile.setGold(mobile.getGold() + goldAmount);
                                    
                                    return Mono.defer(() -> mobileService.save(mobile))
                                            .doOnNext(savedChar -> {
                                                communicationService.sendTextMessage(savedChar, "\n\nYou take " + goldAmount + " gold from " + container.getName() + ".");
                                                communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " takes some gold from " + container.getName() + ".");
                                                communicationService.sendCharacterUpdate(savedChar);
                                            })
                                            .then();
                                }
                                
                                // save container, then give item to player
                                return Mono.defer(() -> {
                                            java.util.List<Item> inv = new java.util.ArrayList<>(mobile.getInventory());
                                            inv.add(itemToTake);
                                            return mobileService.updateInventory(mobile, inv);
                                        })
                                        .doOnNext(savedChar -> {
                                            communicationService.sendTextMessage(savedChar, "\n\nYou take " + itemToTake.getName() + " from " + container.getName() + ".");
                                            communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " takes " + itemToTake.getName() + " from " + container.getName() + ".");
                                            communicationService.sendCharacterUpdate(savedChar);
                                        })
                                        .then();
                            });
                }).then();
    }

    private Mono<Void> handleTakeFromRoom(Mobile mobile, String itemName) {
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Check transient items first (corpses, etc.)
                    Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                            .filter(i -> i.getName().toLowerCase().contains(itemName))
                            .findFirst();

                    if (transientMatch.isPresent()) {
                        Item itemToTake = transientMatch.get();
                        if (itemToTake.isNoPickup()) {
                            communicationService.sendTextMessage(mobile, "\n\nYou cannot pick that up.");
                            return Mono.<Void>empty();
                        }
                        return mobileService.takeTransientItem(mobile, itemToTake).then();
                    }

                    List<Long> itemIds = room.getItemIds();
                    if (itemIds.isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nYou don't see that here.");
                        return Mono.<Void>empty();
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

    @Override
    public String getDescription() {
        return "Take an item from the room or a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: take <item> [from <container>]\n\nPicks up an item from the ground in your current room and adds it to your inventory. You can also use 'take <item> from <container>' to remove an item from a container that is either in the room or in your inventory.";
    }
}


