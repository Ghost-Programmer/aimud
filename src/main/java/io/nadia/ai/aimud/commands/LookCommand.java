package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
/**
 * LookCommand standard implementation layer.
 * Look around your current room.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "look")
public class LookCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final ItemService itemService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing look command for Mobile: {}", mobile.getName());
        String[] parts = commandLine.trim().split("\\s+", 2);
        boolean isTargetedLook = parts.length > 1;

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    if (isTargetedLook) {
                        String targetName = parts[1].toLowerCase();
                        
                        // Look inside corpses
                        for (Item item : roomService.getTransientItemsInRoom(room.getId())) {
                            if (item.getItemType() == ItemType.CORPSE && item.getName().toLowerCase().contains(targetName)) {
                                communicationService.sendTextMessage(mobile, "\n\nUpon " + item.getName() + " you see:");
                                if (item.getInventory() == null || item.getInventory().isEmpty()) {
                                    communicationService.sendTextMessage(mobile, "Nothing of value.");
                                } else {
                                    for (Item lootItem : item.getInventory()) {
                                        communicationService.sendTextMessage(mobile, " - " + lootItem.getName());
                                    }
                                }
                                return Mono.empty();
                            }
                        }
                        
                        // We could look at characters or items here later, but default to nothing found for now
                        communicationService.sendTextMessage(mobile, "\n\nYou don't see that here.");
                        return Mono.empty();
                    }

                    // Otherwise, regular room look
                    return roomService.calculateCurrentLightValue(room).flatMap(baseLight -> {
                        int light = mobileService.getEffectiveLight(mobile, baseLight);
                        if (light <= 0) {
                            communicationService.sendTextMessage(mobile, "\n\nIt is pitch black. You cannot see anything.");
                            return Mono.empty();
                        }

                        if (light >= 5) {
                            communicationService.sendTextMessage(mobile, "\n\n" + room.getName() + "\n" + room.getDescription());
                        }

                        if (light == 1) {
                            long othersCount = mobileService.findAllByRoomId(room.getId()).stream()
                                    .filter(m -> !m.getId().equals(mobile.getId()) && !m.isHidden() && !m.isInvisible()).count();
                            boolean hasItems = !room.getItemIds().isEmpty() || !roomService.getTransientItemsInRoom(room.getId()).isEmpty();
                            
                            if (othersCount > 0 || hasItems) {
                                communicationService.sendTextMessage(mobile, "\n\nYou sense something present in the darkness.");
                            } else {
                                communicationService.sendTextMessage(mobile, "\n\nIt is too dark to make out any details.");
                            }
                        } else if (light > 1) {
                            mobileService.findAllByRoomId(room.getId()).stream()
                                    .filter(m -> !m.getId().equals(mobile.getId()) && !m.isHidden() && !m.isInvisible())
                                    .forEach(m -> {
                                        if (light >= 7) {
                                            communicationService.sendTextMessage(mobile, "\nYou see " + m.getName() + " here.");
                                            if (m.getStoreId() != null) {
                                                communicationService.sendTextMessage(mobile, m.getName() + " appears to be running a store.");
                                            }
                                        } else {
                                            communicationService.sendTextMessage(mobile, "\nYou see a shadowy creature here.");
                                        }
                                    });

                            if (light >= 7) {
                                room.getItemIds().forEach(itemId -> {
                                    itemService.getItem(itemId)
                                            .doOnNext(item -> communicationService.sendTextMessage(mobile, "\nYou see " + item.getName() + " laying here."))
                                            .subscribe();
                                });
                                roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                        communicationService.sendTextMessage(mobile, "\nYou see " + item.getName() + " laying here."));
                            } else {
                                room.getItemIds().forEach(itemId -> {
                                    communicationService.sendTextMessage(mobile, "\nYou see some sort of item laying here.");
                                });
                                roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                    communicationService.sendTextMessage(mobile, "\nYou see some sort of item laying here."));
                            }
                        }

                        if (light >= 4) {
                            List<String> exits = new ArrayList<>();
                            if (room.getNorthId() != null) exits.add("North");
                            if (room.getEastId() != null) exits.add("East");
                            if (room.getSouthId() != null) exits.add("South");
                            if (room.getWestId() != null) exits.add("West");
                            if (room.getUpId() != null) exits.add("Up");
                            if (room.getDownId() != null) exits.add("Down");
                            communicationService.sendTextMessage(mobile, "\n\nExits: " + String.join(", ", exits));
                        }

                        return Mono.empty();
                    });
                });
    }

    @Override
    public String getDescription() {
        return "Look around your current room.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: look\n\nShows you the description of your current location, including other characters, monsters, items, and available exits.";
    }
}


