package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
/**
 * EquipCommand standard implementation layer.
 * Equip an item from your inventory.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "equip")
public class EquipCommand implements Command {
    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing equip command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nEquip what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        // Find the item in Mobile's inventory by name (or ID)
        // Since players use names, we'll try to find by name first
        Optional<Item> itemToEquip = Mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToEquip.isEmpty()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        return characterService.equipItem(Mobile, itemToEquip.get().getId())
                .then();
    }

    @Override
    public String getDescription() {
        return "Equip an item from your inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: equip <item>\n\nEquips the specified item from your inventory. If you already have an item equipped in that slot, it will be replaced.";
    }
}

