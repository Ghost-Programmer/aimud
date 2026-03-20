package com.aimud.aimud.service;

import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.MobileSkill;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.repository.MobileRepository;
import com.aimud.aimud.repository.MobileSkillRepository;
import com.aimud.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MobileService {

    private final MobileRepository mobileRepository;
    private final StatService statService;
    private final MobileSkillRepository mobileSkillRepository;
    private final ItemService itemService;
    private final DatabaseClient databaseClient;

    // In-memory storage for active spawned mobiles
    // Keys are UUIDs to allow multiple instances of the same mobile template, 
    // but we map them to their template ID or assign them a unique runtime ID.
    // For now, we'll key them by their DB ID, assuming 1 DB entry = 1 instance.
    private final ConcurrentHashMap<Long, Mobile> activeMobiles = new ConcurrentHashMap<>();

    public MobileService(MobileRepository mobileRepository, StatService statService,
                         MobileSkillRepository mobileSkillRepository, ItemService itemService,
                         DatabaseClient databaseClient) {
        this.mobileRepository = mobileRepository;
        this.statService = statService;
        this.mobileSkillRepository = mobileSkillRepository;
        this.itemService = itemService;
        this.databaseClient = databaseClient;
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
                statService.updateMobileStats(mobile);
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

    // --- SKILL MANAGEMENT ---

    public Flux<MobileSkill> getMobileSkills(Long mobileId) {
        log.info("Fetching skills for mobile: {}", mobileId);
        return mobileSkillRepository.findByMobileId(mobileId);
    }

    public Mono<MobileSkill> assignSkill(Long mobileId, String skillName, int rank) {
        log.info("Assigning skill '{}' rank {} to mobile {}", skillName, rank, mobileId);
        return databaseClient.sql(
                "INSERT INTO mobile_skills (mobile_id, name, rank) VALUES (:mobileId, :name, :rank) " +
                "ON CONFLICT (mobile_id, name) DO UPDATE SET rank = EXCLUDED.rank")
                .bind("mobileId", mobileId)
                .bind("name", skillName)
                .bind("rank", rank)
                .fetch().rowsUpdated()
                .then(mobileSkillRepository.findByMobileIdAndName(mobileId, skillName));
    }

    // --- INVENTORY MANAGEMENT ---

    public Flux<Item> getMobileInventory(Long mobileId) {
        log.info("Fetching inventory for mobile: {}", mobileId);
        return databaseClient.sql("SELECT item_id FROM mobile_inventory WHERE mobile_id = :mobileId")
                .bind("mobileId", mobileId)
                .map(row -> row.get("item_id", Long.class))
                .all()
                .flatMap(itemService::getItem);
    }

    public Mono<Mobile> addItemToInventory(Long mobileId, Long itemId) {
        log.info("Adding item {} to inventory of mobile {}", itemId, mobileId);
        return getMobile(mobileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + mobileId)))
                .flatMap(mobile -> itemService.getItem(itemId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Item not found: " + itemId)))
                        .then(databaseClient.sql(
                                "INSERT INTO mobile_inventory (mobile_id, item_id) VALUES (:mobileId, :itemId) ON CONFLICT DO NOTHING")
                                .bind("mobileId", mobileId)
                                .bind("itemId", itemId)
                                .fetch().rowsUpdated())
                        .thenReturn(mobile));
    }

    // --- WEAR LOCATION MANAGEMENT ---

    @CacheEvict(value = {"mobiles", "mobile"}, allEntries = true)
    public Mono<Mobile> assignItemToWearLocation(Long mobileId, Long itemId, WearLocation location) {
        log.info("Assigning item {} to wear location {} on mobile {}", itemId, location, mobileId);
        return getMobile(mobileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + mobileId)))
                .flatMap(mobile -> itemService.getItem(itemId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Item not found: " + itemId)))
                        .flatMap(item -> {
                            applyWearLocation(mobile, item, location);
                            return saveMobile(mobile);
                        }));
    }

    public Flux<Item> getMobileWornItems(Long mobileId) {
        log.info("Fetching worn items for mobile: {}", mobileId);
        return getMobile(mobileId)
                .flatMapMany(mobile -> {
                    List<Long> ids = new ArrayList<>();
                    if (mobile.getHeadId() != null)         ids.add(mobile.getHeadId());
                    if (mobile.getChestId() != null)        ids.add(mobile.getChestId());
                    if (mobile.getLegsId() != null)         ids.add(mobile.getLegsId());
                    if (mobile.getFeetId() != null)         ids.add(mobile.getFeetId());
                    if (mobile.getArmsId() != null)         ids.add(mobile.getArmsId());
                    if (mobile.getHandsId() != null)        ids.add(mobile.getHandsId());
                    if (mobile.getRightFingerId() != null)  ids.add(mobile.getRightFingerId());
                    if (mobile.getLeftFingerId() != null)   ids.add(mobile.getLeftFingerId());
                    if (mobile.getRightWristId() != null)   ids.add(mobile.getRightWristId());
                    if (mobile.getLeftWristId() != null)    ids.add(mobile.getLeftWristId());
                    if (mobile.getNeckId() != null)         ids.add(mobile.getNeckId());
                    if (mobile.getLeftEarId() != null)      ids.add(mobile.getLeftEarId());
                    if (mobile.getRightEarId() != null)     ids.add(mobile.getRightEarId());
                    if (mobile.getFaceId() != null)         ids.add(mobile.getFaceId());
                    if (mobile.getWaistId() != null)        ids.add(mobile.getWaistId());
                    if (mobile.getPrimaryId() != null)      ids.add(mobile.getPrimaryId());
                    if (mobile.getOffhandId() != null)      ids.add(mobile.getOffhandId());
                    return Flux.fromIterable(ids);
                })
                .flatMap(itemService::getItem);
    }

    // --- ROOM ASSIGNMENT ---

    @CacheEvict(value = {"mobiles", "mobile"}, allEntries = true)
    public Mono<Mobile> setMobileRoom(Long mobileId, Long roomId) {
        log.info("Setting room {} for mobile {}", roomId, mobileId);
        return getMobile(mobileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + mobileId)))
                .flatMap(mobile -> {
                    mobile.setCurrentRoomId(roomId);
                    return saveMobile(mobile);
                });
    }

    private void applyWearLocation(Mobile mobile, Item item, WearLocation location) {
        switch (location) {
            case HEAD    -> mobile.setHead(item);
            case CHEST   -> mobile.setChest(item);
            case LEGS    -> mobile.setLegs(item);
            case FEET    -> mobile.setFeet(item);
            case ARMS    -> mobile.setArms(item);
            case HANDS   -> mobile.setHands(item);
            case NECK    -> mobile.setNeck(item);
            case FACE    -> mobile.setFace(item);
            case WAIST   -> mobile.setWaist(item);
            case PRIMARY -> mobile.setPrimary(item);
            case OFFHAND -> mobile.setOffhand(item);
            case FINGER  -> {
                if (mobile.getRightFingerId() == null) mobile.setRightFinger(item);
                else mobile.setLeftFinger(item);
            }
            case WRIST   -> {
                if (mobile.getRightWristId() == null) mobile.setRightWrist(item);
                else mobile.setLeftWrist(item);
            }
            case EAR     -> {
                if (mobile.getLeftEarId() == null) mobile.setLeftEar(item);
                else mobile.setRightEar(item);
            }
            default -> log.warn("Unhandled wear location {} for mobile {}", location, mobile.getId());
        }
    }
}
