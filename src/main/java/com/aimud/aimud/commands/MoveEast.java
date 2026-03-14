package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "e")
public class MoveEast implements Command{
    private final CharacterService characterService;
    private final CommunicationService communicationService;
    private final RoomService roomService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing move east command for character: {}", character.getName());

        return this.roomService.getRoom(character.getCurrentRoomId()).flatMap(room -> {
            if (room.getEastId() != null) {
                return this.characterService.enterRoom(character, room.getEastId());
            } else {
                communicationService.sendTextMessage(character, "\n\nYou can't go east from here.");
            }
            return Mono.empty();
        }).then();
    }
}