package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
/**
 * LootCommand standard implementation layer.
 * Loot items from a corpse.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "loot")
public class LootCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing loot command for Mobile: {}", mobile.getName());
        String[] parts = commandLine.trim().split("\\s+", 2);

        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nLoot what?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    Item corpseToLoot = null;

                    for (Item item : roomService.getTransientItemsInRoom(room.getId())) {
                        if (item.getItemType() == ItemType.CORPSE && item.getName().toLowerCase().contains(targetName)) {
                            corpseToLoot = item;
                            break;
                        }
                    }

                    if (corpseToLoot == null) {
                        communicationService.sendTextMessage(mobile, "\n\nYou don't see any corpse by that name here.");
                        return Mono.empty();
                    }

                    if (corpseToLoot.getInventory() == null || corpseToLoot.getInventory().isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nThere is nothing of value left on " + corpseToLoot.getName() + ".");
                        return Mono.empty();
                    }

                    if (mobile.getInventory() == null) {
                        mobile.setInventory(new ArrayList<>());
                    }

                    communicationService.sendTextMessage(mobile, "\n\nYou loot " + corpseToLoot.getName() + ":");
                    for (Item lootItem : corpseToLoot.getInventory()) {
                        mobile.getInventory().add(lootItem);
                        communicationService.sendTextMessage(mobile, " - " + lootItem.getName());
                    }

                    communicationService.roomMessage(mobile, "\n" + mobile.getName() + " loots " + corpseToLoot.getName() + ".");

                    corpseToLoot.getInventory().clear();
                    mobileService.save(mobile).subscribe();

                    return Mono.empty();
                });
    }

    @Override
    public String getDescription() {
        return "Loot items from a corpse.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: loot <name>\n\nLoots all items from a specified corpse in the room and places them in your inventory.";
    }
}

