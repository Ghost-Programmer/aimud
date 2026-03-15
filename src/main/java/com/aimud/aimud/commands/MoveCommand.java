package com.aimud.aimud.commands;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class MoveCommand implements Command {
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;
    protected final RoomService roomService;

    protected abstract Long getNextRoomId(Room currentRoom);
    protected abstract String getDirectionName();

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing move {} command for character: {} in room id {}.", getDirectionName(), character.getName(), character.getCurrentRoomId());

        return this.roomService.getRoom(character.getCurrentRoomId()).flatMap(room -> {
            Long nextRoomId = getNextRoomId(room);
            if (nextRoomId != null) {
                return this.characterService.enterRoom(character, nextRoomId);
            } else {
                communicationService.sendTextMessage(character, "\n\nYou can't go " + getDirectionName() + " from here.");
                return Mono.empty();
            }
        }).then();
    }
}