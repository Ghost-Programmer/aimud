package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "drop")
public class DropCommand implements Command {
    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing drop command for Mobile: {}", Mobile.getName());
        
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nDrop what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        
        Optional<Item> itemToDrop = Mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToDrop.isEmpty()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        return characterService.dropItem(Mobile, itemToDrop.get().getId())
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

