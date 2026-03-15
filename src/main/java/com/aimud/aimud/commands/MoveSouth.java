package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.RoomService;

@MudCommand(name = "s")
public class MoveSouth extends MoveCommand {

    public MoveSouth(CharacterService characterService, CommunicationService communicationService, RoomService roomService) {
        super(characterService, communicationService, roomService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getSouthId();
    }

    @Override
    protected String getDirectionName() {
        return "south";
    }
}