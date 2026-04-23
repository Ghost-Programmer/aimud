package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
/**
 * AttackCommand standard implementation layer.
 * Initiates combat with a target.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "attack")
public class AttackCommand implements Command {
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
        log.info("Executing attack command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nAttack who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Check for a PC target first
                    List<Mobile> charactersInRoom = mobileService.findAllByRoomId(room.getId());
                    Mobile pcTarget = charactersInRoom.stream()
                            .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (pcTarget != null) {
                        if (mobileService.setTarget(mobile, pcTarget)) {
                            communicationService.sendTextMessage(mobile, "\n\nYou charge towards " + pcTarget.getName() + " and attack!");
                            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " charges towards " + pcTarget.getName() + " and attacks!");
                        } else {
                            communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + pcTarget.getName() + ".");
                        }
                        return Mono.empty();
                    }

                    // Check for an NPC target
                    List<Mobile> mobilesInRoom = mobileService.findAllByRoomId(room.getId());
                    if (mobilesInRoom.isEmpty()) {
                        log.info("No mobiles found in room: {}", room.getName());
                        communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    return mobilesInRoom.stream()
                            .filter(m -> m.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .map(npcTarget -> {
                                if (mobileService.setTarget(mobile, npcTarget)) {
                                    communicationService.sendTextMessage(mobile, "\n\nYou charge towards " + npcTarget.getName() + " and attack!");
                                    communicationService.roomMessage(mobile, "\n" + mobile.getName() + " charges towards " + npcTarget.getName() + " and attacks!");
                                } else {
                                    communicationService.sendTextMessage(mobile, "\n\nYou cannot attack " + npcTarget.getName() + ".");
                                }
                                return Mono.empty();
                            }).orElse(Mono.defer(() -> {
                                log.info("No mobiles found in room: {} by name: {}", room.getName(), targetName);
                                communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
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


