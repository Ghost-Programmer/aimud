package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.RoomService;
import io.nadia.ai.aimud.types.RoomType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@Slf4j
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Mono<Map<String, Object>> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RoomType type,
            @RequestParam(required = false) Long minId,
            @RequestParam(required = false) Long maxId) {
        log.info("REST Request to get rooms: page={}, size={}, name={}, type={}", page, size, name, type);
        return roomService.getAllRooms()
                .filter(room -> {
                    boolean matches = true;
                    if (name != null && !name.isEmpty()) {
                        matches = room.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    if (matches && type != null) {
                        matches = room.getRoomType() == type;
                    }
                    if (matches && minId != null) {
                        matches = room.getId() >= minId;
                    }
                    if (matches && maxId != null) {
                        matches = room.getId() <= maxId;
                    }
                    return matches;
                })
                .collectList()
                .map(rooms -> {
                    rooms.sort(Comparator.comparing(room -> room.getName().toLowerCase()));
                    int totalRooms = rooms.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, totalRooms);

                    List<Room> pagedRooms = (fromIndex < totalRooms)
                            ? rooms.subList(fromIndex, toIndex)
                            : List.of();

                    Map<String, Object> response = new HashMap<>();
                    response.put("rooms", pagedRooms);
                    response.put("total", totalRooms);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Room>> getRoom(@PathVariable Long id) {
        log.info("REST Request to get room: {}", id);
        return roomService.getRoom(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Room>> createRoom(@RequestBody Room room) {
        log.info("REST Request to create room: {}", room.getName());
        return roomService.saveRoom(room)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Room>> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        log.info("REST Request to update room: {}", id);
        return roomService.getRoom(id)
                .flatMap(existingRoom -> {
                    existingRoom.setName(room.getName());
                    existingRoom.setDescription(room.getDescription());
                    existingRoom.setRoomType(room.getRoomType());

                    existingRoom.setNorthId(room.getNorthId());
                    existingRoom.setSouthId(room.getSouthId());
                    existingRoom.setEastId(room.getEastId());
                    existingRoom.setWestId(room.getWestId());
                    existingRoom.setUpId(room.getUpId());
                    existingRoom.setDownId(room.getDownId());

                    existingRoom.setNorthDoor(room.isNorthDoor());
                    existingRoom.setSouthDoor(room.isSouthDoor());
                    existingRoom.setEastDoor(room.isEastDoor());
                    existingRoom.setWestDoor(room.isWestDoor());
                    existingRoom.setUpDoor(room.isUpDoor());
                    existingRoom.setDownDoor(room.isDownDoor());

                    existingRoom.setNorthDoorOpen(room.isNorthDoorOpen());
                    existingRoom.setSouthDoorOpen(room.isSouthDoorOpen());
                    existingRoom.setEastDoorOpen(room.isEastDoorOpen());
                    existingRoom.setWestDoorOpen(room.isWestDoorOpen());
                    existingRoom.setUpDoorOpen(room.isUpDoorOpen());
                    existingRoom.setDownDoorOpen(room.isDownDoorOpen());
                    existingRoom.setItems(room.getItems());

                    return roomService.saveRoom(existingRoom);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRoom(@PathVariable Long id) {
        log.info("REST Request to delete room: {}", id);
        return roomService.deleteRoom(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
