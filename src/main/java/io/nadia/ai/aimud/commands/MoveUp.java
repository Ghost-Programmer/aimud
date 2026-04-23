package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
/**
 * MoveUp standard implementation layer.
 * Primary processing handler mapping structural integrations natively.
 */

@MudCommand(name = "u")
public class MoveUp extends MoveCommand {

    public MoveUp(MobileService mobileService, CommunicationService communicationService, RoomService roomService) {
        super(communicationService, roomService, mobileService);
    }

    @Override
    protected Long getNextRoomId(Room currentRoom) {
        return currentRoom.getUpId();
    }

    @Override
    protected String getDirectionName() {
        return "up";
    }
}


