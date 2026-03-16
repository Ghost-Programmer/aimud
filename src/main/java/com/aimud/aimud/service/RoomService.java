package com.aimud.aimud.service;

import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final CommunicationService communicationService;

    public RoomService(RoomRepository roomRepository, CommunicationService communicationService) {
        this.roomRepository = roomRepository;
        this.communicationService = communicationService;
    }

    @Cacheable(value = "rooms")
    public Flux<Room> getAllRooms() {
        log.info("Fetching all rooms");
        return roomRepository.findAll().cache();
    }

    @Cacheable(value = "room", key = "#id")
    public Mono<Room> getRoom(Long id) {
        log.info("Fetching room with id: {}", id);
        return roomRepository.findById(id).cache();
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> saveRoom(Room room) {
        log.info("Saving room: {} (id: {})", room.getName(), room.getId());
        return roomRepository.save(room);
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Void> deleteRoom(Long id) {
        log.info("Deleting room with id: {}", id);
        return roomRepository.deleteById(id);
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> addItemToRoom(Long roomId, Long itemId) {
        log.info("Adding item {} to room {}", itemId, roomId);
        return this.getRoom(roomId)
                .flatMap(room -> {
                    String currentItems = room.getItems();
                    if (currentItems == null || currentItems.isEmpty()) {
                        room.setItems(String.valueOf(itemId));
                    } else {
                        room.setItems(currentItems + "," + itemId);
                    }
                    return this.saveRoom(room);
                });
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> removeItemFromRoom(Long roomId, Long itemId) {
        log.info("Removing item {} from room {}", itemId, roomId);
        return this.getRoom(roomId)
                .flatMap(room -> {
                    List<Long> itemIds = room.getItemIds();
                    if (itemIds.remove(itemId)) {
                        String newItems = itemIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
                        room.setItems(newItems.isEmpty() ? null : newItems);
                        return this.saveRoom(room);
                    }
                    return Mono.just(room);
                });
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> addMobileToRoom(Long roomId, Long mobileId) {
        log.info("Adding mobile {} to room {}", mobileId, roomId);
        return this.getRoom(roomId)
                .flatMap(room -> {
                    String currentMobiles = room.getMobiles();
                    if (currentMobiles == null || currentMobiles.isEmpty()) {
                        room.setMobiles(String.valueOf(mobileId));
                    } else {
                        room.setMobiles(currentMobiles + "," + mobileId);
                    }
                    return this.saveRoom(room);
                });
    }

    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> removeMobileFromRoom(Long roomId, Long mobileId) {
        log.info("Removing mobile {} from room {}", mobileId, roomId);
        return this.getRoom(roomId)
                .flatMap(room -> {
                    List<Long> mobileIds = room.getMobileIds();
                    if (mobileIds.remove(mobileId)) {
                        String newMobiles = mobileIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));
                        room.setMobiles(newMobiles.isEmpty() ? null : newMobiles);
                        return this.saveRoom(room);
                    }
                    return Mono.just(room);
                });
    }

}
