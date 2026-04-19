package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.repository.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final MobileService mobileService;
    private final ConfigService configService;

    // In-memory storage for transient room items (e.g., corpses) — never persisted to DB
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Item>> transientRoomItems = new ConcurrentHashMap<>();

    /**
     * Constructs a new RoomService.
     *
     * @param roomRepository       the room repository
     * @param mobileService        the mobile service
     * @param configService        the config service
     */
    public RoomService(RoomRepository roomRepository, @Lazy MobileService mobileService, @Lazy ConfigService configService) {
        this.roomRepository = roomRepository;
        this.mobileService = mobileService;
        this.configService = configService;
    }

    /**
     * Retrieves all rooms from the database, grouped by caching.
     *
     * @return a {@link Flux} emitting all rooms
     */
    @Cacheable(value = "rooms")
    public Flux<Room> getAllRooms() {
        log.info("Fetching all rooms");
        return roomRepository.findAll().cache();
    }

    /**
     * Retrieves a specific room by its ID, and spawns its specific mobiles.
     *
     * @param id the ID of the room
     * @return a {@link Mono} containing the room
     */
    @Cacheable(value = "room", key = "#id")
    public Mono<Room> getRoom(Long id) {
        log.info("Fetching room with id: {}", id);
        return roomRepository.findById(id)
                .doOnNext(room -> {
                    if (mobileService != null) {
                        mobileService.spawnMobilesForRoom(room);
                    }
                })
                .cache();
    }

    /**
     * Saves a room entity and flushes the related caches.
     *
     * @param room the room to save
     * @return a {@link Mono} containing the saved room
     */
    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Room> saveRoom(Room room) {
        log.info("Saving room: {} (id: {})", room.getName(), room.getId());
        return roomRepository.save(room);
    }

    /**
     * Deletes a room by its ID and flushes the related caches.
     *
     * @param id the ID of the room to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"rooms", "room"}, allEntries = true)
    public Mono<Void> deleteRoom(Long id) {
        log.info("Deleting room with id: {}", id);
        return roomRepository.deleteById(id);
    }

    /**
     * Formats and appends an item ID to a room's persisted item list.
     *
     * @param roomId the ID of the room
     * @param itemId the ID of the item
     * @return a {@link Mono} containing the saved room
     */
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

    /**
     * Removes an item ID from a room's persisted item list.
     *
     * @param roomId the ID of the room
     * @param itemId the ID of the item to remove
     * @return a {@link Mono} containing the saved room
     */
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

    /**
     * Formats and appends a mobile ID to a room's persisted mobile list.
     *
     * @param roomId   the ID of the room
     * @param mobileId the ID of the mobile
     * @return a {@link Mono} containing the saved room
     */
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

    /**
     * Removes a mobile ID from a room's persisted mobile list.
     *
     * @param roomId   the ID of the room
     * @param mobileId the ID of the mobile to remove
     * @return a {@link Mono} containing the saved room
     */
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

    /**
     * Adds an item to a room's transient in-memory state (e.g. a corpse) which will not be persisted.
     *
     * @param roomId the ID of the room
     * @param item   the item to add
     */
    public void addTransientItemToRoom(Long roomId, Item item) {
        transientRoomItems.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(item);
        log.info("Added transient item '{}' to room {}", item.getName(), roomId);
    }

    /**
     * Retrieves all transient in-memory items located in a specific room.
     *
     * @param roomId the ID of the room
     * @return a list of transient items
     */
    public List<Item> getTransientItemsInRoom(Long roomId) {
        return transientRoomItems.getOrDefault(roomId, new CopyOnWriteArrayList<>());
    }

    /**
     * Removes an item from a room's transient in-memory state.
     *
     * @param roomId the ID of the room
     * @param item   the item to remove
     */
    public void removeTransientItemFromRoom(Long roomId, Item item) {
        CopyOnWriteArrayList<Item> items = transientRoomItems.get(roomId);
        if (items != null) {
            items.remove(item);
            log.info("Removed transient item '{}' from room {}", item.getName(), roomId);
        }
    }

    /**
     * Calculates ambient light in the room by evaluating current server time.
     * Updates the room's transient currentLightValue and returns it.
     */
    public Mono<Integer> calculateCurrentLightValue(Room room) {
        return this.configService.getServerSettings()
                .map(settings -> {
                    Integer dayLight = room.getDayLightValue() != null ? room.getDayLightValue() : 0;
                    Integer nightLight = room.getNightLightValue() != null ? room.getNightLightValue() : 0;
                    Integer light = settings.isNight() ? nightLight : dayLight;

                    if (room.getEffects() != null) {
                        for (CharacterEffect ce : room.getEffects()) {
                            if (ce.getEffect() != null) {
                                if (io.nadia.ai.aimud.types.EffectType.DARKVISION.equals(ce.getEffect().getEffectType())) {
                                    light += ce.getEffect().getModifier1();
                                } else if (io.nadia.ai.aimud.types.EffectType.DARKNESS.equals(ce.getEffect().getEffectType())) {
                                    light -= ce.getEffect().getModifier1();
                                }
                            }
                        }
                    }

                    room.setCurrentLightValue(light);
                    return light;
                });
    }
}
