package com.aimud.aimud.service;

import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.RoomRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Flux<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Cacheable(value = "rooms", key = "#id")
    public Mono<Room> getRoom(Long id) {
        return roomRepository.findById(id);
    }

    @CachePut(value = "rooms", key = "#room.id", condition = "#room.id != null")
    public Mono<Room> saveRoom(Room room) {
        return roomRepository.save(room);
    }

    @CacheEvict(value = "rooms", key = "#id")
    public Mono<Void> deleteRoom(Long id) {
        return roomRepository.deleteById(id);
    }
}
