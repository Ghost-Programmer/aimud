package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.model.MobileSkill;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.repository.MobileActionRepository;
import io.nadia.ai.aimud.repository.MobileRepository;
import io.nadia.ai.aimud.repository.MobileSkillRepository;
import io.nadia.ai.aimud.types.WearLocation;
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
    private final MobileActionRepository mobileActionRepository;

    // In-memory storage for active spawned mobiles
    // Keys are UUIDs to allow multiple instances of the same mobile template, 
    // but we map them to their template ID or assign them a unique runtime ID.
    // For now, we'll key them by their DB ID, assuming 1 DB entry = 1 instance.
    private final ConcurrentHashMap<Long, Mobile> activeMobiles = new ConcurrentHashMap<>();

    /**
     * Constructs a new MobileService.
     *
     * @param mobileRepository       the mobile repository
     * @param statService            the stat service
     * @param mobileSkillRepository  the mobile skill repository
     * @param itemService            the item service
     * @param databaseClient         the R2DBC database client
     * @param mobileActionRepository the mobile action repository
     */
    public MobileService(MobileRepository mobileRepository, StatService statService,
                         MobileSkillRepository mobileSkillRepository, ItemService itemService,
                         DatabaseClient databaseClient, MobileActionRepository mobileActionRepository) {
        this.mobileRepository = mobileRepository;
        this.statService = statService;
        this.mobileSkillRepository = mobileSkillRepository;
        this.itemService = itemService;
        this.databaseClient = databaseClient;
        this.mobileActionRepository = mobileActionRepository;
    }

    /**
     * Retrieves all non-player character mobiles from the database.
     *
     * @return a {@link Flux} emitting all NPC mobiles
     */
    @Cacheable(value = "mobiles")
    public Flux<Mobile> getAllMobiles() {
        log.info("Fetching all mobiles");
        return mobileRepository.findByUserIdIsNull().cache();
    }

    /**
     * Retrieves a mobile by its ID from the database or cache.
     *
     * @param id the ID of the mobile
     * @return a {@link Mono} containing the mobile
     */
    @Cacheable(value = "mobile", key = "#id")
    public Mono<Mobile> getMobile(Long id) {
        log.info("Fetching mobile with id: {}", id);
        return mobileRepository.findById(id).cache();
    }

    /**
     * Saves a mobile entity to the database and updates it in memory if active.
     *
     * @param mobile the mobile to save
     * @return a {@link Mono} containing the saved mobile
     */
    @CacheEvict(value = {"mobiles", "mobile", "mobiles"}, allEntries = true)
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

    /**
     * Deletes a mobile entity from the database and removes it from active memory.
     *
     * @param id the ID of the mobile to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"mobiles", "mobile", "mobiles"}, allEntries = true)
    public Mono<Void> deleteMobile(Long id) {
        log.info("Deleting mobile with id: {}", id);
        return mobileRepository.deleteById(id)
                .doOnSuccess(v -> activeMobiles.remove(id));
    }

    /**
     * Retrieves all mobiles assigned to a specific room from the database.
     *
     * @param roomId the ID of the room
     * @return a {@link Flux} emitting mobiles located in the room
     */
    @Cacheable(value = "mobiles")
    public Flux<Mobile> getMobilesByRoom(Long roomId) {
        return mobileRepository.findByCurrentRoomIdAndUserIdIsNull(roomId);
    }

    /**
     * Scans the room for assigned mobile IDs, dropping them into memory and 
     * caching them with all stats and actions if not already present.
     *
     * @param room the room to spawn mobiles for
     */
    public void spawnMobilesForRoom(Room room) {
        log.info("Spawning mobiles for room: {} (id: {})", room.getName(), room.getId());
        this.getMobilesByRoom(room.getId()).subscribe(mobile -> {
            if (!activeMobiles.containsKey(mobile.getId())) {
                log.info("Spawning mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
                // Ensure the mobile knows which room it is in
                mobile.setCurrentRoomId(room.getId());
                mobile.setUserId(null);
                statService.updateMobileStats(mobile);
                
                // Fetch AI actions into transient list
                getMobileActions(mobile.getId()).collectList().subscribe(actions -> {
                    mobile.setActions(actions);
                    activeMobiles.put(mobile.getId(), mobile);
                    log.info("Spawned mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
                });
            } else {
                log.info("Mobile already spawned in room: {} (id: {})", room.getName(), room.getId());
            }
        });
    }

    /**
     * Gets all active (spawned, in-memory) mobiles currently located in a specific room.
     *
     * @param roomId the ID of the room
     * @return a list of active mobiles in the room
     */
    public List<Mobile> getMobilesInRoom(Long roomId) {
        return activeMobiles.values().stream()
                .filter(m -> roomId.equals(m.getCurrentRoomId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all currently active and tracked mobiles in memory.
     *
     * @return a list of all active mobiles
     */
    public List<Mobile> getActiveMobiles() {
        return new ArrayList<>(activeMobiles.values());
    }

    /**
     * Removes an active mobile from in-memory tracking.
     *
     * @param mobileId the ID of the mobile to remove
     */
    public void removeActiveMobile(Long mobileId) {
        activeMobiles.remove(mobileId);
        log.info("Removed mobile {} from active list", mobileId);
    }

    // --- SKILL MANAGEMENT ---

    /**
     * Retrieves all skills associated with a specific mobile.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting the mobile's skills
     */
    public Flux<MobileSkill> getMobileSkills(Long mobileId) {
        log.info("Fetching skills for mobile: {}", mobileId);
        return mobileSkillRepository.findByMobileId(mobileId);
    }

    /**
     * Assigns or updates a skill to a mobile with a specific rank.
     *
     * @param mobileId  the ID of the mobile
     * @param skillName the name of the skill
     * @param rank      the rank of the skill
     * @return a {@link Mono} containing the updated or created mobile skill
     */
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

    // --- ACTION MANAGEMENT ---

    /**
     * Retrieves the AI actions configured for a mobile.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting the mobile's available actions
     */
    public Flux<MobileAction> getMobileActions(Long mobileId) {
        log.info("Fetching actions for mobile: {}", mobileId);
        return mobileActionRepository.findByMobileId(mobileId);
    }

    /**
     * Saves a complete list of AI actions for a mobile, replacing existing ones.
     *
     * @param mobileId the ID of the mobile
     * @param actions  the list of actions to save
     * @return a {@link Flux} emitting the saved actions
     */
    public Flux<MobileAction> saveMobileActions(Long mobileId, List<MobileAction> actions) {
        log.info("Saving {} actions for mobile {}", actions.size(), mobileId);
        return databaseClient.sql("DELETE FROM mobile_actions WHERE mobile_id = :mobileId")
                .bind("mobileId", mobileId)
                .fetch().rowsUpdated()
                .thenMany(Flux.fromIterable(actions)
                        .flatMap(action -> {
                            action.setMobileId(mobileId);
                            action.setId(null); // Ensure fresh insert
                            return mobileActionRepository.save(action);
                        }));
    }

    // --- INVENTORY MANAGEMENT ---

    /**
     * Retrieves the inventory items for a mobile, populating their total counts.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting inventory items
     */
    public Flux<Item> getMobileInventory(Long mobileId) {
        log.info("Fetching inventory for mobile: {}", mobileId);
        return databaseClient.sql("SELECT item_id, item_count FROM mobile_inventory WHERE mobile_id = :mobileId")
                .bind("mobileId", mobileId)
                .map((row, metadata) -> new Object[]{row.get("item_id", Long.class), row.get("item_count", Integer.class)})
                .all()
                .flatMap(arr -> {
                    Long itemId = (Long) arr[0];
                    Integer count = (Integer) arr[1];
                    return itemService.getItem(itemId)
                            .map(item -> {
                                item.setCount(count != null ? count : 1);
                                return item;
                            });
                });
    }

    /**
     * Adds an item to a mobile's inventory. If stackable, augments the count.
     *
     * @param mobileId the ID of the mobile
     * @param itemId   the ID of the item
     * @return a {@link Mono} returning the mobile context
     */
    public Mono<Mobile> addItemToInventory(Long mobileId, Long itemId) {
        log.info("Adding item {} to inventory of mobile {}", itemId, mobileId);
        return getMobile(mobileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + mobileId)))
                .flatMap(mobile -> itemService.getItem(itemId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Item not found: " + itemId)))
                        .flatMap(item -> {
                            String sql;
                            if (item.isStackable()) {
                                sql = "INSERT INTO mobile_inventory (mobile_id, item_id, item_count) VALUES (:mobileId, :itemId, 1) " +
                                      "ON CONFLICT (mobile_id, item_id) DO UPDATE SET item_count = mobile_inventory.item_count + 1";
                            } else {
                                sql = "INSERT INTO mobile_inventory (mobile_id, item_id) VALUES (:mobileId, :itemId) ON CONFLICT DO NOTHING";
                            }
                            return databaseClient.sql(sql)
                                .bind("mobileId", mobileId)
                                .bind("itemId", itemId)
                                .fetch().rowsUpdated()
                                .thenReturn(mobile);
                        }));
    }

    // --- WEAR LOCATION MANAGEMENT ---

    /**
     * Equips an item to a specific wear location on a mobile.
     *
     * @param mobileId the ID of the mobile
     * @param itemId   the ID of the item to equip
     * @param location the location to equip the item to
     * @return a {@link Mono} returning the saved mobile context
     */
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

    /**
     * Retrieves all items currently worn or equipped by a mobile.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting the equipped items
     */
    public Flux<Item> getMobileWornItems(Long mobileId) {
        log.info("Fetching worn items for mobile: {}", mobileId);
        return getMobile(mobileId)
                .flatMapMany(mobile -> {
                    List<Long> ids = new ArrayList<>();
                    if (mobile.getHeadId() != null) ids.add(mobile.getHeadId());
                    if (mobile.getChestId() != null) ids.add(mobile.getChestId());
                    if (mobile.getLegsId() != null) ids.add(mobile.getLegsId());
                    if (mobile.getFeetId() != null) ids.add(mobile.getFeetId());
                    if (mobile.getArmsId() != null) ids.add(mobile.getArmsId());
                    if (mobile.getHandsId() != null) ids.add(mobile.getHandsId());
                    if (mobile.getRightFingerId() != null) ids.add(mobile.getRightFingerId());
                    if (mobile.getLeftFingerId() != null) ids.add(mobile.getLeftFingerId());
                    if (mobile.getRightWristId() != null) ids.add(mobile.getRightWristId());
                    if (mobile.getLeftWristId() != null) ids.add(mobile.getLeftWristId());
                    if (mobile.getNeckId() != null) ids.add(mobile.getNeckId());
                    if (mobile.getLeftEarId() != null) ids.add(mobile.getLeftEarId());
                    if (mobile.getRightEarId() != null) ids.add(mobile.getRightEarId());
                    if (mobile.getFaceId() != null) ids.add(mobile.getFaceId());
                    if (mobile.getWaistId() != null) ids.add(mobile.getWaistId());
                    if (mobile.getPrimaryId() != null) ids.add(mobile.getPrimaryId());
                    if (mobile.getOffhandId() != null) ids.add(mobile.getOffhandId());
                    return Flux.fromIterable(ids);
                })
                .flatMap(itemService::getItem);
    }

    // --- ROOM ASSIGNMENT ---

    /**
     * Sets the room a mobile is currently located in.
     *
     * @param mobileId the ID of the mobile
     * @param roomId   the ID of the room
     * @return a {@link Mono} returning the saved mobile context
     */
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

    /**
     * Applies an item to the designated wear location property of a mobile.
     *
     * @param mobile   the mobile context
     * @param item     the item being equipped
     * @param location the location the item will occupy
     */
    private void applyWearLocation(Mobile mobile, Item item, WearLocation location) {
        switch (location) {
            case HEAD -> mobile.setHead(item);
            case CHEST -> mobile.setChest(item);
            case LEGS -> mobile.setLegs(item);
            case FEET -> mobile.setFeet(item);
            case ARMS -> mobile.setArms(item);
            case HANDS -> mobile.setHands(item);
            case NECK -> mobile.setNeck(item);
            case FACE -> mobile.setFace(item);
            case WAIST -> mobile.setWaist(item);
            case PRIMARY -> mobile.setPrimary(item);
            case OFFHAND -> mobile.setOffhand(item);
            case FINGER -> {
                if (mobile.getRightFingerId() == null) mobile.setRightFinger(item);
                else mobile.setLeftFinger(item);
            }
            case WRIST -> {
                if (mobile.getRightWristId() == null) mobile.setRightWrist(item);
                else mobile.setLeftWrist(item);
            }
            case EAR -> {
                if (mobile.getLeftEarId() == null) mobile.setLeftEar(item);
                else mobile.setRightEar(item);
            }
            default -> log.warn("Unhandled wear location {} for mobile {}", location, mobile.getId());
        }
    }
}
