package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "attack")
public class AttackCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterService characterService;
    private final MobileService mobileService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing attack command for character: {}", character.getName());
        
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(character, "\n\nAttack who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(character.getCurrentRoomId())
                .flatMap(room -> {
                    // Check for a PC target first
                    List<Character> charactersInRoom = characterService.findAllByRoomId(room.getId());
                    Character pcTarget = charactersInRoom.stream()
                            .filter(c -> !c.getId().equals(character.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        character.setTarget(pcTarget);
                        communicationService.sendTextMessage(character, "\n\nYou charge towards " + pcTarget.getName() + " and attack!");
                        communicationService.roomMessage(character, "\n" + character.getName() + " charges towards " + pcTarget.getName() + " and attacks!");
                        return Mono.empty();
                    }

                    // Check for an NPC target
                    List<Long> mobileIds = room.getMobileIds();
                    if (mobileIds.isEmpty()) {
                        communicationService.sendTextMessage(character, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(mobileIds)
                            .flatMap(mobileService::getMobile)
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .next()
                            .flatMap(npcTarget -> {
                                character.setTarget(npcTarget);
                                communicationService.sendTextMessage(character, "\n\nYou charge towards " + npcTarget.getName() + " and attack!");
                                communicationService.roomMessage(character, "\n" + character.getName() + " charges towards " + npcTarget.getName() + " and attacks!");
                                return Mono.empty();
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                communicationService.sendTextMessage(character, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                }).then();
    }
}
