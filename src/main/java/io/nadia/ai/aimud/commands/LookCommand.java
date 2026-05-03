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
    private final ConfigService configService;

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
                        
                        // Look at characters
                        List<Mobile> mobilesInRoom = mobileService.findAllByRoomId(room.getId());
                        for (Mobile m : mobilesInRoom) {
                            if (m.getName().toLowerCase().contains(targetName) && !m.isHidden() && !m.isInvisible()) {
                                Mono<String> raceNameMono = m.getRaceId() != null 
                                        ? configService.getAllRaces().filter(r -> r.getId().equals(m.getRaceId())).next().map(io.nadia.ai.aimud.model.Race::getName).defaultIfEmpty("Unknown Race")
                                        : Mono.just("Unknown Race");

                                Mono<String> classNameMono = m.getClassId() != null
                                        ? configService.getAllCharacterClasses().filter(c -> c.getId().equals(m.getClassId())).next().map(io.nadia.ai.aimud.model.CharacterClass::getName).defaultIfEmpty("Unknown Class")
                                        : Mono.just("Unknown Class");

                                return Mono.zip(raceNameMono, classNameMono).flatMap(tuple -> {
                                    String raceName = tuple.getT1();
                                    String className = tuple.getT2();

                                    StringBuilder desc = new StringBuilder("\n\nYou look at " + m.getName() + ".\n");
                                    desc.append(m.getName()).append(" is a ").append(raceName).append(" ").append(className).append(".\n");

                                    double hpPercent = m.getMaxHp() > 0 ? (double) m.getCurrentHp() / (double) m.getMaxHp() : 1.0;
                                    if (hpPercent >= 1.0) desc.append(m.getName() + " is in excellent condition.\n");
                                    else if (hpPercent >= 0.75) desc.append(m.getName() + " has a few scratches.\n");
                                    else if (hpPercent >= 0.5) desc.append(m.getName() + " has some small wounds and bruises.\n");
                                    else if (hpPercent >= 0.25) desc.append(m.getName() + " is covered in blood.\n");
                                    else desc.append(m.getName() + " is barely clinging to life.\n");

                                    desc.append("\nEquipment:\n");
                                    boolean hasEq = false;
                                    if (m.getHead() != null) { desc.append("Head: ").append(m.getHead().getName()).append("\n"); hasEq = true; }
                                    if (m.getChest() != null) { desc.append("Chest: ").append(m.getChest().getName()).append("\n"); hasEq = true; }
                                    if (m.getLegs() != null) { desc.append("Legs: ").append(m.getLegs().getName()).append("\n"); hasEq = true; }
                                    if (m.getFeet() != null) { desc.append("Feet: ").append(m.getFeet().getName()).append("\n"); hasEq = true; }
                                    if (m.getArms() != null) { desc.append("Arms: ").append(m.getArms().getName()).append("\n"); hasEq = true; }
                                    if (m.getHands() != null) { desc.append("Hands: ").append(m.getHands().getName()).append("\n"); hasEq = true; }
                                    if (m.getPrimary() != null) { desc.append("Primary: ").append(m.getPrimary().getName()).append("\n"); hasEq = true; }
                                    if (m.getOffhand() != null) { desc.append("Offhand: ").append(m.getOffhand().getName()).append("\n"); hasEq = true; }
                                    if (!hasEq) desc.append("Nothing of interest.\n");

                                    communicationService.sendTextMessage(mobile, desc.toString());
                                    return Mono.empty();
                                });
                            }
                        }

                        // We could look at items here later, but default to nothing found for now
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


