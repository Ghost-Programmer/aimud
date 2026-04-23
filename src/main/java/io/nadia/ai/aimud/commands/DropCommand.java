package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
/**
 * DropCommand standard implementation layer.
 * Drop an item from your inventory.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "drop")
public class DropCommand implements Command {
    private final MobileService mobileService;
    private final CommunicationService communicationService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing drop command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nDrop what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        Optional<Item> itemToDrop = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToDrop.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        Item item = itemToDrop.get();
        if (item.getItemType() == io.nadia.ai.aimud.types.ItemType.CONTAINER && item.getInventory() != null && !item.getInventory().isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou must empty the container before dropping it.");
            return Mono.empty();
        }

        return mobileService.dropItem(mobile, itemToDrop.get().getId())
                .then();
    }

    @Override
    public String getDescription() {
        return "Drop an item from your inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: drop <item>\n\nDrops the specified item from your inventory into the room you are currently in.";
    }
}


