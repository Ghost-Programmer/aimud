package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;

@MudCommand(name = "n")
public class MoveNorth extends MoveCommand {

    public MoveNorth(CharacterService characterService, CommunicationService communicationService, RoomService roomService) {
        super(characterService, communicationService, roomService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getNorthId();
    }

    @Override
    protected String getDirectionName() {
        return "north";
    }
}

