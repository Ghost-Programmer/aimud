package com.aimud.aimud.commands;

import com.aimud.aimud.model.Mobile;
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
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing move {} command for Mobile: {} in room id {}.", getDirectionName(), Mobile.getName(), Mobile.getCurrentRoomId());

        return this.roomService.getRoom(Mobile.getCurrentRoomId()).flatMap(room -> {
            Long nextRoomId = getNextRoomId(room);
            if (nextRoomId != null) {
                return this.characterService.enterRoom(Mobile, nextRoomId);
            } else {
                communicationService.sendTextMessage(Mobile, "\n\nYou can't go " + getDirectionName() + " from here.");
                return Mono.empty();
            }
        }).then();
    }

    @Override
    public String getDescription() {
        return "Move " + getDirectionName() + ".";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: " + getDirectionName() + "\n\nMoves your Mobile in the " + getDirectionName() + " direction, if an exit exists.";
    }
}

