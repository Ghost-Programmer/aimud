package com.aimud.aimud.service;

import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final CommunicationService communicationService;

    public RoomService(RoomRepository roomRepository, CommunicationService communicationService) {
        this.roomRepository = roomRepository;
        this.communicationService = communicationService;
    }

    public Flux<Room> getAllRooms() {
        log.info("Fetching all rooms");
        return roomRepository.findAll();
    }

    public Mono<Room> getRoom(Long id) {
        log.info("Fetching room with id: {}", id);
        return roomRepository.findById(id);
    }

    public Mono<Room> saveRoom(Room room) {
        log.info("Saving room: {} (id: {})", room.getName(), room.getId());
        return roomRepository.save(room);
    }

    public Mono<Void> deleteRoom(Long id) {
        log.info("Deleting room with id: {}", id);
        return roomRepository.deleteById(id);
    }


}
