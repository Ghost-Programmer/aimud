package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing attack command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nAttack who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(Mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Check for a PC target first
                    List<Mobile> charactersInRoom = characterService.findAllByRoomId(room.getId());
                    Mobile pcTarget = charactersInRoom.stream()
                            .filter(c -> !c.getId().equals(Mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        if (characterService.setTarget(Mobile, pcTarget)) {
                            communicationService.sendTextMessage(Mobile, "\n\nYou charge towards " + pcTarget.getName() + " and attack!");
                            communicationService.roomMessage(Mobile, "\n" + Mobile.getName() + " charges towards " + pcTarget.getName() + " and attacks!");
                        } else {
                            communicationService.sendTextMessage(Mobile, "\n\nYou cannot attack " + pcTarget.getName() + ".");
                        }
                        return Mono.empty();
                    }

                    // Check for an NPC target
                    List<Mobile> mobilesInRoom = mobileService.getMobilesInRoom(room.getId());
                    if (mobilesInRoom.isEmpty()) {
                        log.info("No mobiles found in room: {}", room.getName());
                        communicationService.sendTextMessage(Mobile, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    return mobilesInRoom.stream()
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .map(npcTarget -> {
                                if (characterService.setTarget(Mobile, npcTarget)) {
                                    communicationService.sendTextMessage(Mobile, "\n\nYou charge towards " + npcTarget.getName() + " and attack!");
                                    communicationService.roomMessage(Mobile, "\n" + Mobile.getName() + " charges towards " + npcTarget.getName() + " and attacks!");
                                } else {
                                    communicationService.sendTextMessage(Mobile, "\n\nYou cannot attack " + npcTarget.getName() + ".");
                                }
                                return Mono.empty();
                            }).orElse(Mono.defer(() -> {
                                log.info("No mobiles found in room: {} by name: {}", room.getName(), targetName);
                                communicationService.sendTextMessage(Mobile, "\n\nThey aren't here.");
                                return Mono.empty();
                            }));
                }).then();
    }

    @Override
    public String getDescription() {
        return "Initiates combat with a target.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: attack <target>\n\nStarts fighting the specified target in the room.";
    }
}

