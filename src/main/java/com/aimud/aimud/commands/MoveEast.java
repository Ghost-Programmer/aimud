package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;

@MudCommand(name = "e")
public class MoveEast extends MoveCommand {

    public MoveEast(CharacterService characterService, CommunicationService communicationService, RoomService roomService) {
        super(characterService, communicationService, roomService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getEastId();
    }

    @Override
    protected String getDirectionName() {
        return "east";
    }
}

