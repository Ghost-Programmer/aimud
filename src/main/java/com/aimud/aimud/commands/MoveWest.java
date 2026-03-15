package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;

@MudCommand(name = "w")
public class MoveWest extends MoveCommand {

    public MoveWest(CharacterService characterService, CommunicationService communicationService, RoomService roomService) {
        super(characterService, communicationService, roomService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getWestId();
    }

    @Override
    protected String getDirectionName() {
        return "west";
    }
}