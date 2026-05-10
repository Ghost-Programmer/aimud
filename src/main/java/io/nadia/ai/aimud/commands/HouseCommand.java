package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "house")
public class HouseCommand implements Command {

    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing house command for Mobile: {}", mobile.getName());
        String[] firstSplit = commandLine.trim().split("\\s+", 2);

        if (firstSplit.length < 2) {
            sendSyntax(mobile);
            return Mono.empty();
        }

        String action = firstSplit[0].toLowerCase();
        String argument = firstSplit[1].trim();
        
        if (!List.of("claim", "add", "name", "description").contains(action)) {
            sendSyntax(mobile);
            return Mono.empty();
        }

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    if (action.equals("claim")) {
                        return handleClaim(mobile, room, argument.toLowerCase());
                    } else if (action.equals("add")) {
                        return handleAdd(mobile, room, argument.toLowerCase());
                    } else if (action.equals("name")) {
                        return handleName(mobile, room, argument);
                    } else if (action.equals("description")) {
                        return handleDescription(mobile, room, argument);
                    }
                    return Mono.empty();
                })
                .then();
    }

    private void sendSyntax(Mobile mobile) {
        communicationService.sendTextMessage(mobile, "\n\nSyntax:\n  house claim <direction>\n  house add <direction>\n  house name \"<name>\"\n  house description \"<description>\"");
    }

    private Mono<Void> handleName(Mobile mobile, Room room, String name) {
        if (room.getRoomOwner() == null || !room.getRoomOwner().equals(mobile.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not own this room.");
            return Mono.empty();
        }
        
        if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 1) {
            name = name.substring(1, name.length() - 1);
        }
        
        room.setName(name);
        return roomService.saveRoom(room)
                .doOnNext(r -> communicationService.sendTextMessage(mobile, "\n\nYou have renamed the room to: " + r.getName()))
                .then();
    }

    private Mono<Void> handleDescription(Mobile mobile, Room room, String description) {
        if (room.getRoomOwner() == null || !room.getRoomOwner().equals(mobile.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not own this room.");
            return Mono.empty();
        }
        
        if (description.startsWith("\"") && description.endsWith("\"") && description.length() > 1) {
            description = description.substring(1, description.length() - 1);
        }
        
        room.setDescription(description);
        return roomService.saveRoom(room)
                .doOnNext(r -> communicationService.sendTextMessage(mobile, "\n\nYou have updated the room's description."))
                .then();
    }

    private Mono<Void> handleClaim(Mobile mobile, Room room, String direction) {
        if (!List.of("north", "south", "east", "west", "up", "down").contains(direction)) {
            communicationService.sendTextMessage(mobile, "\n\nInvalid direction. Valid directions are: north, south, east, west, up, down.");
            return Mono.empty();
        }

        if (!room.isRoomHouse()) {
            communicationService.sendTextMessage(mobile, "\n\nYou cannot build a house here.");
            return Mono.empty();
        }

        Item writ = mobile.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.DOCUMENT && i.getName().equalsIgnoreCase("Housing Writ"))
                .findFirst()
                .orElse(null);

        if (writ == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not possess a Housing Writ.");
            return Mono.empty();
        }

        return buildRoom(mobile, room, direction, writ, false);
    }

    private Mono<Void> handleAdd(Mobile mobile, Room room, String direction) {
        if (!List.of("north", "south", "east", "west", "up", "down").contains(direction)) {
            communicationService.sendTextMessage(mobile, "\n\nInvalid direction. Valid directions are: north, south, east, west, up, down.");
            return Mono.empty();
        }

        if (room.getRoomOwner() == null || !room.getRoomOwner().equals(mobile.getId())) {
            communicationService.sendTextMessage(mobile, "\n\nYou can only expand a house that you own.");
            return Mono.empty();
        }

        Item permit = mobile.getInventory().stream()
                .filter(i -> i.getItemType() == ItemType.DOCUMENT && i.getName().equalsIgnoreCase("House Expansion Permit"))
                .findFirst()
                .orElse(null);

        if (permit == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou do not possess a House Expansion Permit.");
            return Mono.empty();
        }

        return buildRoom(mobile, room, direction, permit, true);
    }

    private Mono<Void> buildRoom(Mobile mobile, Room room, String direction, Item documentToConsume, boolean isExpansion) {
        boolean isConnected;
        String reverseDirection;
        switch (direction) {
            case "north": isConnected = room.getNorthId() != null; reverseDirection = "south"; break;
            case "south": isConnected = room.getSouthId() != null; reverseDirection = "north"; break;
            case "east":  isConnected = room.getEastId() != null; reverseDirection = "west"; break;
            case "west":  isConnected = room.getWestId() != null; reverseDirection = "east"; break;
            case "up":    isConnected = room.getUpId() != null; reverseDirection = "down"; break;
            case "down":  isConnected = room.getDownId() != null; reverseDirection = "up"; break;
            default: isConnected = true; reverseDirection = ""; break;
        }

        if (isConnected) {
            communicationService.sendTextMessage(mobile, "\n\nThere is already a connection in that direction.");
            return Mono.empty();
        }

        return databaseClient.sql("SELECT COALESCE(MAX(id), 19999) + 1 FROM rooms WHERE id >= 20000 AND id < 30000")
                .map((row, rowMetadata) -> row.get(0, Long.class))
                .one()
                .flatMap(newRoomId -> {
                    if (newRoomId >= 30000) {
                        communicationService.sendTextMessage(mobile, "\n\nNo available house plots remain.");
                        return Mono.empty();
                    }

                    String houseName = mobile.getName() + "'s House";
                    String houseDesc = "This is the house " + mobile.getName() + " built.";

                    String reverseCol = reverseDirection + "_id";
                    String sql = "INSERT INTO rooms (id, name, description, room_type, " + reverseCol + ", room_persist, room_house, room_owner) " +
                                 "VALUES (:id, :name, :description, 'INDOORS', :reverseId, true, false, :ownerId)";

                    return databaseClient.sql(sql)
                            .bind("id", newRoomId)
                            .bind("name", houseName)
                            .bind("description", houseDesc)
                            .bind("reverseId", room.getId())
                            .bind("ownerId", mobile.getId())
                            .fetch()
                            .rowsUpdated()
                            .flatMap(updatedRows -> {
                                if (updatedRows == 0) {
                                    communicationService.sendTextMessage(mobile, "\n\nFailed to create the house.");
                                    return Mono.empty();
                                }

                                switch (direction) {
                                    case "north": room.setNorthId(newRoomId); break;
                                    case "south": room.setSouthId(newRoomId); break;
                                    case "east":  room.setEastId(newRoomId); break;
                                    case "west":  room.setWestId(newRoomId); break;
                                    case "up":    room.setUpId(newRoomId); break;
                                    case "down":  room.setDownId(newRoomId); break;
                                }

                                return roomService.saveRoom(room)
                                        .flatMap(savedRoom -> {
                                            List<Item> currentInventory = new ArrayList<>(mobile.getInventory());
                                            currentInventory.remove(documentToConsume);
                                            mobile.setInventory(currentInventory);

                                            return mobileService.save(mobile)
                                                    .flatMap(savedChar -> mobileService.updateInventory(savedChar, currentInventory))
                                                    .doOnNext(savedChar -> {
                                                        if (isExpansion) {
                                                            communicationService.sendTextMessage(mobile, "\n\nYou use your permit and expand your house to the " + direction + "!");
                                                            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " uses a permit to expand their house to the " + direction + "!");
                                                        } else {
                                                            communicationService.sendTextMessage(mobile, "\n\nYou claim a plot of land and build your house to the " + direction + "!");
                                                            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " claims a plot of land and builds a house to the " + direction + "!");
                                                        }
                                                    })
                                                    .then();
                                        });
                            });
                });
    }

    @Override
    public String getDescription() {
        return "Interact with the housing system.";
    }

    @Override
    public String getDetailedDescription() {
        return "Housing Commands:\n" +
               "  house claim <direction>      - Uses a Housing Writ on a valid plot of land to claim it and build a house.\n" +
               "  house add <direction>        - Uses a House Expansion Permit inside your home to add new rooms.\n" +
               "  house name \"<name>\"          - Renames the current room (requires ownership).\n" +
               "  house description \"<desc>\"   - Updates the current room's description (requires ownership).";
    }
}
