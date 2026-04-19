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

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "look")
public class LookCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterService characterService;
    private final ItemService itemService;
    private final MobileService mobileService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing look command for Mobile: {}", Mobile.getName());
        String[] parts = commandLine.trim().split("\\s+", 2);
        boolean isTargetedLook = parts.length > 1;

        return roomService.getRoom(Mobile.getCurrentRoomId())
                .flatMap(room -> {
                    if (isTargetedLook) {
                        String targetName = parts[1].toLowerCase();
                        
                        // Look inside corpses
                        for (Item item : roomService.getTransientItemsInRoom(room.getId())) {
                            if (item.getItemType() == ItemType.CORPSE && item.getName().toLowerCase().contains(targetName)) {
                                communicationService.sendTextMessage(Mobile, "\n\nUpon " + item.getName() + " you see:");
                                if (item.getInventory() == null || item.getInventory().isEmpty()) {
                                    communicationService.sendTextMessage(Mobile, "Nothing of value.");
                                } else {
                                    for (Item lootItem : item.getInventory()) {
                                        communicationService.sendTextMessage(Mobile, " - " + lootItem.getName());
                                    }
                                }
                                return Mono.empty();
                            }
                        }
                        
                        // We could look at characters or items here later, but default to nothing found for now
                        communicationService.sendTextMessage(Mobile, "\n\nYou don't see that here.");
                        return Mono.empty();
                    }

                    // Otherwise, regular room look
                    return roomService.calculateCurrentLightValue(room).flatMap(light -> {
                        if (light <= 0) {
                            communicationService.sendTextMessage(Mobile, "\n\nIt is pitch black. You cannot see anything.");
                            return Mono.empty();
                        }

                        if (light >= 5) {
                            communicationService.sendTextMessage(Mobile, "\n\n" + room.getName() + "\n" + room.getDescription());
                        }

                        if (light == 1) {
                            long chars = characterService.findAllByRoomId(room.getId()).stream()
                                    .filter(c -> !c.getId().equals(Mobile.getId()) && !c.isHidden() && !c.isInvisible()).count();
                            long mobs = mobileService.getMobilesInRoom(room.getId()).stream()
                                    .filter(m -> !m.isHidden() && !m.isInvisible()).count();
                            boolean hasItems = !room.getItemIds().isEmpty() || !roomService.getTransientItemsInRoom(room.getId()).isEmpty();
                            
                            if (chars > 0 || mobs > 0 || hasItems) {
                                communicationService.sendTextMessage(Mobile, "\n\nYou sense something present in the darkness.");
                            } else {
                                communicationService.sendTextMessage(Mobile, "\n\nIt is too dark to make out any details.");
                            }
                        } else if (light > 1) {
                            characterService.findAllByRoomId(room.getId()).stream()
                                    .filter(c -> !c.getId().equals(Mobile.getId()) && !c.isHidden() && !c.isInvisible())
                                    .forEach(c -> {
                                        if (light >= 7) {
                                            communicationService.sendTextMessage(Mobile, "\nYou see " + c.getName() + " here.");
                                        } else {
                                            communicationService.sendTextMessage(Mobile, "\nYou see a shadowy creature here.");
                                        }
                                    });

                            mobileService.getMobilesInRoom(room.getId()).stream()
                                    .filter(m -> !m.isHidden() && !m.isInvisible())
                                    .forEach(m -> {
                                        if (light >= 7) {
                                            communicationService.sendTextMessage(Mobile, "\nYou see " + m.getName() + " here.");
                                            if (m.getStoreId() != null) {
                                                communicationService.sendTextMessage(Mobile, m.getName() + " appears to be running a store.");
                                            }
                                        } else {
                                            communicationService.sendTextMessage(Mobile, "\nYou see a shadowy creature here.");
                                        }
                                    });

                            if (light >= 7) {
                                room.getItemIds().forEach(itemId -> {
                                    itemService.getItem(itemId)
                                            .doOnNext(item -> communicationService.sendTextMessage(Mobile, "\nYou see " + item.getName() + " laying here."))
                                            .subscribe();
                                });
                                roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                        communicationService.sendTextMessage(Mobile, "\nYou see " + item.getName() + " laying here."));
                            } else {
                                room.getItemIds().forEach(itemId -> {
                                    communicationService.sendTextMessage(Mobile, "\nYou see some sort of item laying here.");
                                });
                                roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                    communicationService.sendTextMessage(Mobile, "\nYou see some sort of item laying here."));
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
                            communicationService.sendTextMessage(Mobile, "\n\nExits: " + String.join(", ", exits));
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

