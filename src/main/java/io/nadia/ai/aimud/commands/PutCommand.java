package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@MudCommand(name = "put")
@RequiredArgsConstructor
public class PutCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length < 4 || !parts[2].equalsIgnoreCase("in")) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: put <item> in <container>");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        String containerName = parts[3].toLowerCase();

        Item itemToPut = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst()
                .orElse(null);

        if (itemToPut == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // find container in room or mobile inventory
                    return Flux.fromIterable(room.getItemIds())
                            .flatMap(itemService::getItem)
                            .filter(i -> i.getName().toLowerCase().contains(containerName))
                            .next()
                            .switchIfEmpty(Mono.defer(() -> {
                                return Flux.fromIterable(mobile.getInventory())
                                        .filter(i -> i.getName().toLowerCase().contains(containerName) && !i.getId().equals(itemToPut.getId()))
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

                                // Remove item from character inventory
                                java.util.List<Item> inv = new java.util.ArrayList<>(mobile.getInventory());
                                inv.remove(itemToPut);
                                
                                // Add item to container
                                if (container.getInventory() == null) {
                                    container.setInventory(new java.util.ArrayList<>());
                                }
                                container.getInventory().add(itemToPut);
                                itemService.addItemToContainer(container, itemToPut.getId());

                                // Save container, then update player inventory
                                return itemService.saveItem(container)
                                        .then(Mono.defer(() -> mobileService.updateInventory(mobile, inv)))
                                        .doOnNext(savedChar -> {
                                            communicationService.sendTextMessage(savedChar, "\n\nYou put " + itemToPut.getName() + " in " + container.getName() + ".");
                                            communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " puts " + itemToPut.getName() + " in " + container.getName() + ".");
                                            communicationService.sendCharacterUpdate(savedChar);
                                        })
                                        .then();
                            });
                }).then();
    }

    @Override
    public String getDescription() {
        return "Put an item into a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: put <item> in <container>\n\nPuts an item from your inventory into a container. The container can be in your inventory or in the room.";
    }
}
