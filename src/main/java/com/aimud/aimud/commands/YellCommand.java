package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import com.aimud.aimud.service.RoomService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@MudCommand(name = "yell")
public class YellCommand implements Command {

    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterService characterService;
    private final MobileService mobileService;

    public YellCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.roomService = context.getBean(RoomService.class);
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nYell what?");
            return Mono.empty();
        }

        String text = parts[1];
        communicationService.sendTextMessage(mobile, "\nYou yell, '" + text + "'");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " yells, '" + text + "'");

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Long> connected = new ArrayList<>();
                    if (room.getNorthId() != null) connected.add(room.getNorthId());
                    if (room.getSouthId() != null) connected.add(room.getSouthId());
                    if (room.getEastId() != null) connected.add(room.getEastId());
                    if (room.getWestId() != null) connected.add(room.getWestId());
                    if (room.getUpId() != null) connected.add(room.getUpId());
                    if (room.getDownId() != null) connected.add(room.getDownId());

                    for (Long roomId : connected) {
                        characterService.findAllByRoomId(roomId).forEach(c -> {
                            communicationService.sendTextMessage(c, "\nSomeone yells from nearby, '" + text + "'");
                        });
                        mobileService.getMobilesInRoom(roomId).forEach(m -> {
                            communicationService.sendTextMessage(m, "\nSomeone yells from nearby, '" + text + "'");
                        });
                    }
                    return Mono.empty();
                });
    }

    @Override
    public String getDescription() {
        return "Yell something that can be heard in connected rooms.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: yell <message>\n\nYells a message that can be heard by everyone in your current room and all immediately adjacent rooms.";
    }
}
