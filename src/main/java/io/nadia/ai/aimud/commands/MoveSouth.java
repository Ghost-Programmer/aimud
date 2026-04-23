package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
/**
 * MoveSouth standard implementation layer.
 * Primary processing handler mapping structural integrations natively.
 */

@MudCommand(name = "s")
public class MoveSouth extends MoveCommand {

    public MoveSouth(MobileService mobileService, CommunicationService communicationService, RoomService roomService) {
        super(communicationService, roomService, mobileService);
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


