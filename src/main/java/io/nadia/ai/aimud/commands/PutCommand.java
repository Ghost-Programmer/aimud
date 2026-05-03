package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@MudCommand(name = "put")
public class PutCommand implements Command {
    private final MobileService mobileService;
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing put command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 4);
        if (parts.length < 4 || !parts[2].equalsIgnoreCase("in")) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: put <item> in <container>");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        String containerName = parts[3].toLowerCase();

        Optional<Item> itemToPutOpt = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToPutOpt.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        Item itemToPut = itemToPutOpt.get();

        // 1. Check inventory for container
        Optional<Item> containerOpt = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(containerName))
                .findFirst();

        if (containerOpt.isPresent()) {
            return processPut(mobile, itemToPut, containerOpt.get(), true);
        }

        // 2. If not in inventory, check room
        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Check transient items first (like corpses)
                    Optional<Item> transientMatch = roomService.getTransientItemsInRoom(room.getId()).stream()
                            .filter(i -> i.getName().toLowerCase().contains(containerName))
                            .findFirst();

                    if (transientMatch.isPresent()) {
                        return processPut(mobile, itemToPut, transientMatch.get(), false);
                    }

                    // Then persistent items
                    if (room.getItemIds().isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nYou don't see that container here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(room.getItemIds())
                            .flatMap(itemService::getItem)
                            .filter(item -> item.getName().toLowerCase().contains(containerName))
                            .next()
                            .flatMap(containerItem -> processPut(mobile, itemToPut, containerItem, false))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(mobile, "\n\nYou don't see that container here.");
                                return Mono.empty();
                            }));
                }).then();
    }

    private Mono<Void> processPut(Mobile mobile, Item itemToPut, Item container, boolean inInventory) {
        if (container.getItemType() != ItemType.CONTAINER) {
            communicationService.sendTextMessage(mobile, "\n\n" + container.getName() + " is not a container.");
            return Mono.empty();
        }

        if (itemToPut.getId().equals(container.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou cannot put a container inside itself.");
            return Mono.empty();
        }

        if (itemToPut.getItemType() == ItemType.CONTAINER) {
            communicationService.sendTextMessage(mobile, "\n\nYou cannot put a container inside another container.");
            return Mono.empty();
        }

        int maxItems = container.getProperty2();

        if (container.getInventory() == null) {
            container.setInventory(new ArrayList<>());
        }

        if (maxItems > 0 && container.getInventory().size() >= maxItems) {
            communicationService.sendTextMessage(mobile, "\n\n" + container.getName() + " is full.");
            return Mono.empty();
        }

        mobile.getInventory().remove(itemToPut);
        container.getInventory().add(itemToPut);

        communicationService.sendTextMessage(mobile, "\n\nYou put " + itemToPut.getName() + " in " + container.getName() + ".");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " puts " + itemToPut.getName() + " in " + container.getName() + ".");

        if (inInventory) {
            // Container is in mobile inventory, so updating inventory saves both
            return mobileService.updateInventory(mobile, mobile.getInventory()).then();
        } else {
            // Container is in room, save mobile inventory and the container item
            return Mono.when(
                    mobileService.updateInventory(mobile, mobile.getInventory()),
                    itemService.saveItem(container)
            ).then();
        }
    }

    @Override
    public String getDescription() {
        return "Put an item into a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: put <item> in <container>\n\nMoves an item from your main inventory into a container you are carrying or one on the ground.";
    }
}
