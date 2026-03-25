package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.ItemService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.RoomService;
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
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing look command for character: {}", character.getName());
        return roomService.getRoom(character.getCurrentRoomId())
                .flatMap(room -> {
                    communicationService.sendTextMessage(character, "\n\n" + room.getName() + "\n" + room.getDescription());
                    
                    characterService.findAllByRoomId(room.getId()).stream()
                            .filter(c -> !c.getId().equals(character.getId()))
                            .forEach(c -> communicationService.sendTextMessage(character, "\nYou see " + c.getName() + " here."));

                    mobileService.getMobilesInRoom(room.getId()).forEach(m -> {
                        communicationService.sendTextMessage(character, "\nYou see " + m.getName() + " here.");
                    });

                    room.getItemIds().forEach(itemId -> {
                        itemService.getItem(itemId)
                                .doOnNext(item -> communicationService.sendTextMessage(character, "\nYou see " + item.getName() + " laying here."))
                                .subscribe();
                    });

                    roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                        communicationService.sendTextMessage(character, "\nYou see " + item.getName() + " laying here."));

                    List<String> exits = new ArrayList<>();
                    if (room.getNorthId() != null) exits.add("North");
                    if (room.getEastId() != null) exits.add("East");
                    if (room.getSouthId() != null) exits.add("South");
                    if (room.getWestId() != null) exits.add("West");
                    if (room.getUpId() != null) exits.add("Up");
                    if (room.getDownId() != null) exits.add("Down");
                    communicationService.sendTextMessage(character, "\n\nExits: " + String.join(", ", exits));

                    return Mono.empty();
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
