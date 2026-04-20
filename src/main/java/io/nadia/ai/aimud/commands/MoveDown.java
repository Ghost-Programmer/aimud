package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
/**
 * MoveDown standard implementation layer.
 * Primary processing handler mapping structural integrations natively.
 */

@MudCommand(name = "d")
public class MoveDown extends MoveCommand {

    public MoveDown(CharacterService characterService, CommunicationService communicationService, RoomService roomService) {
        super(characterService, communicationService, roomService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getDownId();
    }

    @Override
    protected String getDirectionName() {
        return "down";
    }
}

