package com.aimud.aimud.service;

import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final Map<Long, Room> roomCache = new ConcurrentHashMap<>();

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostConstruct
    public void loadRooms() {
        roomRepository.findAll()
                .doOnNext(room -> roomCache.put(room.getId(), room))
                .subscribe();
    }

    public Flux<Room> getAllRooms() {
        return Flux.fromIterable(roomCache.values());
    }

    public Room getRoom(Long id) {
        return roomCache.get(id);
    }
}
