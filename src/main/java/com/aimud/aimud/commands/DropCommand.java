package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
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
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing drop command for character: {}", character.getName());
        
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(character, "\n\nDrop what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        
        Optional<Item> itemToDrop = character.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToDrop.isEmpty()) {
            communicationService.sendTextMessage(character, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        return characterService.dropItem(character, itemToDrop.get().getId())
                .then();
    }
}
