package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.MobileSkill;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.model.SkillRegistry;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.RoomType;
import com.aimud.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service providing MCP Tools for AI to manage Rooms, Items, and Effects in AIMUD.
 */
@Service
@Slf4j
public class McpToolService {

    private static final Duration TOOL_TIMEOUT = Duration.ofSeconds(30);

    private final RoomService roomService;
    private final ItemService itemService;
    private final EffectService effectService;
    private final MobileService mobileService;
    private final ConfigService configService;

    public McpToolService(RoomService roomService, ItemService itemService, EffectService effectService,
                          MobileService mobileService, ConfigService configService) {
        this.roomService = roomService;
        this.itemService = itemService;
        this.effectService = effectService;
        this.mobileService = mobileService;
        this.configService = configService;
    }

    // --- ROOM TOOLS ---

    @Tool(description = "Create a new room in the world")
    public Room createRoom(
            @ToolParam(description = "The name of the room") String name,
            @ToolParam(description = "The description of the room") String description,
            @ToolParam(description = "The type of the room (e.g. CITY, FIELD, FOREST, etc.)") String roomType) {
        log.info("MCP Tool: Creating room: {}", name);
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        if (roomType != null) {
            try {
                room.setRoomType(RoomType.valueOf(roomType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid roomType: {}", roomType);
            }
        }
        return await(roomService.saveRoom(room), "create room");
    }

    @Tool(description = "Update an existing room")
    public Room updateRoom(
            @ToolParam(description = "The ID of the room to update") Long id,
            @ToolParam(description = "The name of the room") String name,
            @ToolParam(description = "The description of the room") String description,
            @ToolParam(description = "The type of the room") String roomType) {
        log.info("MCP Tool: Updating room: {} (id: {})", name, id);
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        room.setDescription(description);
        if (roomType != null) {
            try {
                room.setRoomType(RoomType.valueOf(roomType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid roomType: {}", roomType);
            }
        }
        return await(roomService.saveRoom(room), "update room");
    }

    @Tool(description = "Set a directional door and destination room for an existing room")
    public Room setRoomDoor(
            @ToolParam(description = "The ID of the source room") Long roomId,
            @ToolParam(description = "The direction for the door (NORTH, SOUTH, EAST, WEST, UP, DOWN or N/S/E/W/U/D)") String direction,
            @ToolParam(description = "The ID of the destination room") Long destinationRoomId,
            @ToolParam(description = "Whether the door starts open") boolean doorOpen) {
        String normalizedDirection = normalizeDoorDirection(direction);
        log.info("MCP Tool: Setting {} door from room {} to room {} (open={})",
                normalizedDirection, roomId, destinationRoomId, doorOpen);

        return roomService.getRoom(roomId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Room not found: " + roomId)))
                .flatMap(room -> roomService.getRoom(destinationRoomId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Destination room not found: " + destinationRoomId)))
                        .thenReturn(room))
                .map(room -> {
                    applyDoorSettings(room, normalizedDirection, destinationRoomId, doorOpen);
                    return room;
                })
                .flatMap(roomService::saveRoom)
                .as(mono -> await(mono, "set room door"));
    }

    private String normalizeDoorDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            throw new IllegalArgumentException("Direction is required");
        }

        String normalizedValue = normalizeEnumValue(direction);

        return switch (normalizedValue) {
            case "N", "NORTH" -> "NORTH";
            case "S", "SOUTH" -> "SOUTH";
            case "E", "EAST" -> "EAST";
            case "W", "WEST" -> "WEST";
            case "U", "UP" -> "UP";
            case "D", "DOWN" -> "DOWN";
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    private void applyDoorSettings(Room room, String direction, Long destinationRoomId, boolean doorOpen) {
        switch (direction) {
            case "NORTH" -> {
                room.setNorthId(destinationRoomId);
                room.setNorthDoor(true);
                room.setNorthDoorOpen(doorOpen);
            }
            case "SOUTH" -> {
                room.setSouthId(destinationRoomId);
                room.setSouthDoor(true);
                room.setSouthDoorOpen(doorOpen);
            }
            case "EAST" -> {
                room.setEastId(destinationRoomId);
                room.setEastDoor(true);
                room.setEastDoorOpen(doorOpen);
            }
            case "WEST" -> {
                room.setWestId(destinationRoomId);
                room.setWestDoor(true);
                room.setWestDoorOpen(doorOpen);
            }
            case "UP" -> {
                room.setUpId(destinationRoomId);
                room.setUpDoor(true);
                room.setUpDoorOpen(doorOpen);
            }
            case "DOWN" -> {
                room.setDownId(destinationRoomId);
                room.setDownDoor(true);
                room.setDownDoorOpen(doorOpen);
            }
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    @Tool(description = "Retrieve a room by its ID")
    public Room getRoom(@ToolParam(description = "The unique ID of the room") Long id) {
        log.info("MCP Tool: Getting room with id: {}", id);
        return await(roomService.getRoom(id), "get room");
    }

    @Tool(description = "Retrieve all rooms in the world")
    public List<Room> getAllRooms() {
        log.info("MCP Tool: Getting all rooms");
        return awaitList(roomService.getAllRooms(), "get all rooms");
    }

    // --- ITEM TOOLS ---

    @Tool(description = "Create a new item template")
    public Item createItem(
            @ToolParam(description = "The name of the item") String name,
            @ToolParam(description = "The description of the item") String description,
            @ToolParam(description = "The type of the item (e.g. WEAPON, TWO_HANDED_WEAPON, RANGED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR, POTION, etc.)") String itemType,
            @ToolParam(description = "The wear location of the item (e.g. HEAD, CHEST, LEGS, FEET, PRIMARY, OFFHAND, etc.)") String wearLocation) {
        log.info("MCP Tool: Creating item: {}", name);
        log.info("Item details - Description: {}, Type: {}, Wear Location: {}", description, itemType, wearLocation);
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setItemType(parseItemType(itemType));
        item.setWearLocation(parseWearLocation(wearLocation));
        return await(itemService.saveItem(item), "create item");
    }

    @Tool(description = "Update an existing item template")
    public Item updateItem(
            @ToolParam(description = "The ID of the item to update") Long id,
            @ToolParam(description = "The name of the item") String name,
            @ToolParam(description = "The description of the item") String description,
            @ToolParam(description = "The type of the item") String itemType,
            @ToolParam(description = "The wear location of the item") String wearLocation) {
        log.info("MCP Tool: Updating item: {} (id: {})", name, id);
        return itemService.getItem(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Item not found: " + id)))
                .map(item -> {
                    item.setName(name);
                    item.setDescription(description);
                    item.setItemType(parseItemType(itemType));
                    item.setWearLocation(parseWearLocation(wearLocation));
                    return item;
                })
                .flatMap(itemService::saveItem)
                .as(mono -> await(mono, "update item"));
    }

    private ItemType parseItemType(String itemType) {
        if (itemType == null || itemType.isBlank()) {
            return ItemType.NONE;
        }

        String normalizedValue = normalizeEnumValue(itemType);

        return switch (normalizedValue) {
            case "ARMOR" -> {
                log.info("Mapping generic itemType '{}' to LIGHT_ARMOR", itemType);
                yield ItemType.LIGHT_ARMOR;
            }
            case "ONE_HANDED_WEAPON", "ONE_HANDED", "MELEE_WEAPON" -> ItemType.WEAPON;
            case "TWO_HANDED", "TWO_HANDER", "TWOHAND" -> ItemType.TWO_HANDED_WEAPON;
            case "RANGED", "BOW", "CROSSBOW" -> ItemType.RANGED_WEAPON;
            default -> {
                ItemType parsedType = ItemType.fromString(itemType);
                if (parsedType != ItemType.NONE || "NONE".equals(normalizedValue)) {
                    yield parsedType;
                }
                log.warn("Invalid itemType: {}. Defaulting to NONE.", itemType);
                yield ItemType.NONE;
            }
        };
    }

    private WearLocation parseWearLocation(String wearLocation) {
        if (wearLocation == null || wearLocation.isBlank()) {
            return WearLocation.NONE;
        }

        String normalizedValue = normalizeEnumValue(wearLocation);

        return switch (normalizedValue) {
            case "TORSO", "BODY" -> WearLocation.CHEST;
            case "RIGHT_FINGER", "LEFT_FINGER", "FINGERS", "RING" -> WearLocation.FINGER;
            case "RIGHT_WRIST", "LEFT_WRIST", "WRISTS" -> WearLocation.WRIST;
            case "RIGHT_EAR", "LEFT_EAR", "EARS" -> WearLocation.EAR;
            case "MAIN_HAND", "MAINHAND", "RIGHT_HAND", "WEAPON_HAND" -> WearLocation.PRIMARY;
            case "LEFT_HAND", "OFF_HAND", "SHIELD_HAND" -> WearLocation.OFFHAND;
            default -> {
                WearLocation parsedLocation = WearLocation.fromString(wearLocation);
                if (parsedLocation != WearLocation.NONE || "NONE".equals(normalizedValue)) {
                    yield parsedLocation;
                }
                log.warn("Invalid wearLocation: {}. Defaulting to NONE.", wearLocation);
                yield WearLocation.NONE;
            }
        };
    }

    private String normalizeEnumValue(String value) {
        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    @Tool(description = "Retrieve an item template by its ID")
    public Item getItem(@ToolParam(description = "The unique ID of the item") Long id) {
        log.info("MCP Tool: Getting item with id: {}", id);
        return await(itemService.getItem(id), "get item");
    }

    @Tool(description = "Retrieve all item templates")
    public List<Item> getAllItems() {
        log.info("MCP Tool: Getting all items");
        return awaitList(itemService.getAllItems(), "get all items");
    }

    // --- EFFECT TOOLS ---

    @Tool(description = "Create a new effect")
    public Effect createEffect(
            @ToolParam(description = "The type of the effect (e.g. SLASHING_DAMAGE, STRENGTH, ARMOR, etc.)") String effectType,
            @ToolParam(description = "The first modifier value (e.g. number of dice, or bonus amount)") int modifier1,
            @ToolParam(description = "The second modifier value (e.g. size of dice)") int modifier2,
            @ToolParam(description = "The third modifier value") int modifier3,
            @ToolParam(description = "The fourth modifier value") int modifier4) {
        log.info("MCP Tool: Creating effect: {}", effectType);
        Effect effect = new Effect();
        if (effectType != null) {
            try {
                effect.setEffectType(EffectType.valueOf(effectType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid effectType: {}", effectType);
            }
        }
        effect.setModifier1(modifier1);
        effect.setModifier2(modifier2);
        effect.setModifier3(modifier3);
        effect.setModifier4(modifier4);

        return await(effectService.saveEffect(effect), "create effect");
    }

    @Tool(description = "Associate an existing effect with an existing item")
    public Effect linkEffectToItem(
            @ToolParam(description = "The ID of the item to associate this effect with") Long itemId,
            @ToolParam(description = "The ID of the effect to associate with the item") Long effectId) {
        log.info("MCP Tool: Linking effect {} to item {}", effectId, itemId);
        return effectService.linkItemAndEffect(itemId, effectId)
                .then(effectService.getEffect(effectId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Effect not found: " + effectId)))
                .as(mono -> await(mono, "link effect to item"));
    }

    @Tool(description = "Update an existing effect")
    public Effect updateEffect(
            @ToolParam(description = "The ID of the effect to update") Long id,
            @ToolParam(description = "The ID of the item associated with this effect") Long itemId,
            @ToolParam(description = "The type of the effect") String effectType,
            @ToolParam(description = "The first modifier value") int modifier1,
            @ToolParam(description = "The second modifier value") int modifier2,
            @ToolParam(description = "The third modifier value") int modifier3,
            @ToolParam(description = "The fourth modifier value") int modifier4) {
        log.info("MCP Tool: Updating effect: {} (id: {})", effectType, id);
        Effect effect = new Effect();
        effect.setId(id);
        if (effectType != null) {
            try {
                effect.setEffectType(EffectType.valueOf(effectType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid effectType: {}", effectType);
            }
        }
        effect.setModifier1(modifier1);
        effect.setModifier2(modifier2);
        effect.setModifier3(modifier3);
        effect.setModifier4(modifier4);

        return effectService.saveEffect(effect)
                 .flatMap(savedEffect -> {
                     if (itemId != null) {
                         return effectService.linkItemAndEffect(itemId, savedEffect.getId())
                                 .onErrorResume(e -> Mono.empty())
                                 .thenReturn(savedEffect);
                     }
                     return Mono.just(savedEffect);
                 })
                .as(mono -> await(mono, "update effect"));
    }

    @Tool(description = "Retrieve an effect by its ID")
    public Effect getEffect(@ToolParam(description = "The unique ID of the effect") Long id) {
        log.info("MCP Tool: Getting effect with id: {}", id);
        return await(effectService.getEffect(id), "get effect");
    }

    @Tool(description = "Retrieve all effects associated with a specific item")
    public List<Effect> getEffectsByItem(@ToolParam(description = "The ID of the item to retrieve effects for") Long itemId) {
        log.info("MCP Tool: Getting effects for item: {}", itemId);
        return awaitList(effectService.getEffectsByItem(itemId), "get effects by item");
    }

    // --- MOBILE / NPC TOOLS ---

    @Tool(description = "Create a new NPC (mobile) with base stats")
    public Mobile createMobile(
            @ToolParam(description = "The name of the NPC") String name,
            @ToolParam(description = "The race ID of the NPC (optional)") Long raceId,
            @ToolParam(description = "The class ID of the NPC (optional)") Long classId,
            @ToolParam(description = "Strength stat") int strength,
            @ToolParam(description = "Dexterity stat") int dexterity,
            @ToolParam(description = "Constitution stat") int constitution,
            @ToolParam(description = "Intelligence stat") int intelligence,
            @ToolParam(description = "Wisdom stat") int wisdom,
            @ToolParam(description = "Charisma stat") int charisma) {
        log.info("MCP Tool: Creating mobile: {}", name);
        Mobile mobile = new Mobile();
        mobile.setName(name);
        mobile.setRaceId(raceId);
        mobile.setClassId(classId);
        mobile.setStrength(strength);
        mobile.setDexterity(dexterity);
        mobile.setConstitution(constitution);
        mobile.setIntelligence(intelligence);
        mobile.setWisdom(wisdom);
        mobile.setCharisma(charisma);
        return await(mobileService.saveMobile(mobile), "create mobile");
    }

    @Tool(description = "Update an existing NPC's base stats")
    public Mobile updateMobileStats(
            @ToolParam(description = "The ID of the NPC to update") Long id,
            @ToolParam(description = "The name of the NPC") String name,
            @ToolParam(description = "The race ID of the NPC (optional)") Long raceId,
            @ToolParam(description = "The class ID of the NPC (optional)") Long classId,
            @ToolParam(description = "Strength stat") int strength,
            @ToolParam(description = "Dexterity stat") int dexterity,
            @ToolParam(description = "Constitution stat") int constitution,
            @ToolParam(description = "Intelligence stat") int intelligence,
            @ToolParam(description = "Wisdom stat") int wisdom,
            @ToolParam(description = "Charisma stat") int charisma) {
        log.info("MCP Tool: Updating mobile stats: {} (id: {})", name, id);
        return mobileService.getMobile(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + id)))
                .map(mobile -> {
                    mobile.setName(name);
                    mobile.setRaceId(raceId);
                    mobile.setClassId(classId);
                    mobile.setStrength(strength);
                    mobile.setDexterity(dexterity);
                    mobile.setConstitution(constitution);
                    mobile.setIntelligence(intelligence);
                    mobile.setWisdom(wisdom);
                    mobile.setCharisma(charisma);
                    return mobile;
                })
                .flatMap(mobileService::saveMobile)
                .as(mono -> await(mono, "update mobile stats"));
    }

    @Tool(description = "Retrieve an NPC by its ID")
    public Mobile getMobile(
            @ToolParam(description = "The unique ID of the NPC") Long id) {
        log.info("MCP Tool: Getting mobile with id: {}", id);
        return await(mobileService.getMobile(id), "get mobile");
    }

    @Tool(description = "Assign the current room for an NPC")
    public Mobile setMobileRoom(
            @ToolParam(description = "The ID of the NPC") Long mobileId,
            @ToolParam(description = "The ID of the room to place the NPC in") Long roomId) {
        log.info("MCP Tool: Setting room {} for mobile {}", roomId, mobileId);
        return roomService.getRoom(roomId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Room not found: " + roomId)))
                .then(mobileService.setMobileRoom(mobileId, roomId))
                .as(mono -> await(mono, "set mobile room"));
    }

    @Tool(description = "Assign a skill to an NPC, or update its rank if the skill already exists")
    public MobileSkill assignSkillToMobile(
            @ToolParam(description = "The ID of the NPC") Long mobileId,
            @ToolParam(description = "The name of the skill (e.g. 'One Handed Weapon', 'Bandage')") String skillName,
            @ToolParam(description = "The rank of the skill (1-100)") int rank) {
        log.info("MCP Tool: Assigning skill '{}' rank {} to mobile {}", skillName, rank, mobileId);
        return mobileService.getMobile(mobileId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Mobile not found: " + mobileId)))
                .then(mobileService.assignSkill(mobileId, skillName, rank))
                .as(mono -> await(mono, "assign skill to mobile"));
    }

    @Tool(description = "Get all skills assigned to an NPC")
    public List<MobileSkill> getMobileSkills(
            @ToolParam(description = "The ID of the NPC") Long mobileId) {
        log.info("MCP Tool: Getting skills for mobile {}", mobileId);
        return awaitList(mobileService.getMobileSkills(mobileId), "get mobile skills");
    }

    @Tool(description = "Get all available skills from the global skill registry")
    public List<SkillRegistry> getAllSkills() {
        log.info("MCP Tool: Getting all skills from registry");
        return awaitList(configService.getAllSkills(), "get all skills");
    }

    @Tool(description = "Assign an item to a specific wear location on an NPC. For FINGER, fills right then left; for WRIST, fills right then left; for EAR, fills left then right.")
    public Mobile assignItemToMobileWearLocation(
            @ToolParam(description = "The ID of the NPC") Long mobileId,
            @ToolParam(description = "The ID of the item to equip") Long itemId,
            @ToolParam(description = "The wear location (HEAD, CHEST, LEGS, FEET, ARMS, HANDS, FINGER, WRIST, NECK, EAR, FACE, WAIST, PRIMARY, OFFHAND)") String wearLocation) {
        log.info("MCP Tool: Assigning item {} to wear location {} on mobile {}", itemId, wearLocation, mobileId);
        WearLocation loc = parseWearLocation(wearLocation);
        return await(mobileService.assignItemToWearLocation(mobileId, itemId, loc), "assign item to mobile wear location");
    }

    @Tool(description = "Add an item to an NPC's inventory (carried but not worn)")
    public Mobile addItemToMobileInventory(
            @ToolParam(description = "The ID of the NPC") Long mobileId,
            @ToolParam(description = "The ID of the item to add") Long itemId) {
        log.info("MCP Tool: Adding item {} to inventory of mobile {}", itemId, mobileId);
        return await(mobileService.addItemToInventory(mobileId, itemId), "add item to mobile inventory");
    }

    @Tool(description = "Get all items currently worn/equipped by an NPC")
    public List<Item> getMobileWornItems(
            @ToolParam(description = "The ID of the NPC") Long mobileId) {
        log.info("MCP Tool: Getting worn items for mobile {}", mobileId);
        return awaitList(mobileService.getMobileWornItems(mobileId), "get mobile worn items");
    }

    @Tool(description = "Get the inventory (carried items) of an NPC")
    public List<Item> getMobileInventory(
            @ToolParam(description = "The ID of the NPC") Long mobileId) {
        log.info("MCP Tool: Getting inventory for mobile {}", mobileId);
        return awaitList(mobileService.getMobileInventory(mobileId), "get mobile inventory");
    }

    private <T> T await(Mono<T> mono, String operation) {
        try {
            return mono.subscribeOn(Schedulers.boundedElastic())
                    .toFuture()
                    .get(TOOL_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("MCP tool operation interrupted while trying to " + operation, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("MCP tool operation failed while trying to {}", operation, cause);
            throw new IllegalStateException("MCP tool operation failed while trying to " + operation, cause);
        } catch (TimeoutException e) {
            throw new IllegalStateException("MCP tool operation timed out while trying to " + operation, e);
        }
    }

    private <T> List<T> awaitList(Flux<T> flux, String operation) {
        return await(flux.collectList(), operation);
    }
}
