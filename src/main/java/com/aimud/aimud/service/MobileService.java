package com.aimud.aimud.service;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.MobileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MobileService {

    private final MobileRepository mobileRepository;
    private final StatService statService;

    // In-memory storage for active spawned mobiles
    // Keys are UUIDs to allow multiple instances of the same mobile template, 
    // but we map them to their template ID or assign them a unique runtime ID.
    // For now, we'll key them by their DB ID, assuming 1 DB entry = 1 instance.
    private final ConcurrentHashMap<Long, Mobile> activeMobiles = new ConcurrentHashMap<>();

    public MobileService(MobileRepository mobileRepository, StatService statService) {
        this.mobileRepository = mobileRepository;
        this.statService = statService;
    }

    @Cacheable(value = "mobiles")
    public Flux<Mobile> getAllMobiles() {
        log.info("Fetching all mobiles");
        return mobileRepository.findAll().cache();
    }

    @Cacheable(value = "mobile", key = "#id")
    public Mono<Mobile> getMobile(Long id) {
        log.info("Fetching mobile with id: {}", id);
        return mobileRepository.findById(id).cache();
    }

    @CacheEvict(value = {"mobiles", "mobile","mobiles"}, allEntries = true)
    public Mono<Mobile> saveMobile(Mobile mobile) {
        log.info("Saving mobile: {} (id: {})", mobile.getName(), mobile.getId());
        return mobileRepository.save(mobile)
                .doOnNext(saved -> {
                    // Update in-memory if it's currently active
                    if (saved.getId() != null && activeMobiles.containsKey(saved.getId())) {
                        activeMobiles.put(saved.getId(), saved);
                    }
                });
    }

    @CacheEvict(value = {"mobiles", "mobile","mobiles" }, allEntries = true)
    public Mono<Void> deleteMobile(Long id) {
        log.info("Deleting mobile with id: {}", id);
        return mobileRepository.deleteById(id)
                .doOnSuccess(v -> activeMobiles.remove(id));
    }

    @Cacheable(value = "mobiles")
    public Flux<Mobile> getMobilesByRoom(Long roomId) {
        return mobileRepository.findByCurrentRoomId(roomId);
    }

    /**
     * Scans the room for assigned mobile IDs, and loads them into memory if not already present.
     */
    public void spawnMobilesForRoom(Room room) {
        log.info("Spawning mobiles for room: {} (id: {})", room.getName(), room.getId());
        this.getMobilesByRoom(room.getId()).subscribe(mobile -> {
            if (!activeMobiles.containsKey(mobile.getId())) {
                log.info("Spawning mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
                // Ensure the mobile knows which room it is in
                mobile.setCurrentRoomId(room.getId());
                activeMobiles.put(room.getId(), mobile);
                log.info("Spawned mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
            } else {
                log.info("Mobile already spawned in room: {} (id: {})", room.getName(), room.getId());
            }
        });
    }

    /**
     * Gets all active mobiles currently located in a specific room.
     */
    public List<Mobile> getMobilesInRoom(Long roomId) {
        return activeMobiles.values().stream()
                .filter(m -> roomId.equals(m.getCurrentRoomId()))
                .collect(Collectors.toList());
    }

    public List<Mobile> getActiveMobiles() {
        return new ArrayList<>(activeMobiles.values());
    }
}
