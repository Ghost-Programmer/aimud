package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.ItemService;
import com.aimud.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "take")
public class TakeCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final RoomService roomService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing take command for character: {}", character.getName());
        
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(character, "\n\nTake what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        return roomService.getRoom(character.getCurrentRoomId())
                .flatMap(room -> {
                    List<Long> itemIds = room.getItemIds();
                    if (itemIds.isEmpty()) {
                        communicationService.sendTextMessage(character, "\n\nYou don't see that here.");
                        return Mono.empty();
                    }

                    // Look up all items in the room to see if any match the name
                    return Flux.fromIterable(itemIds)
                            .flatMap(itemService::getItem)
                            .filter(item -> item.getName().toLowerCase().contains(itemName))
                            .next() // Get the first matching item
                            .flatMap(itemToTake -> characterService.takeItem(character, itemToTake.getId()))
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(character, "\n\nYou don't see that here.");
                                return Mono.empty();
                            }));
                }).then();
    }

    @Override
    public String getDescription() {
        return "Take an item from the room.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: take <item>\n\nPicks up an item from the ground in your current room and adds it to your inventory.";
    }
}
