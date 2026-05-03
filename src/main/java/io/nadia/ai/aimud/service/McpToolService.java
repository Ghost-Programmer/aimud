package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.RoomType;
import io.nadia.ai.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class McpToolService {

    private static final Duration TOOL_TIMEOUT = Duration.ofSeconds(30);

    private final RoomService roomService;
    private final ItemService itemService;
    private final EffectService effectService;
    private final MobileService mobileService;
    private final ConfigService configService;
    private final StoreService storeService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Constructs a new McpToolService.
     *
     * @param roomService   the room service
     * @param itemService   the item service
     * @param effectService the effect service
     * @param mobileService the mobile service
     * @param configService the config service
     * @param storeService  the store service
     * @param tokenBlacklistService the token blacklist service
     */
    public McpToolService(RoomService roomService, ItemService itemService, EffectService effectService,
            MobileService mobileService, ConfigService configService, StoreService storeService, TokenBlacklistService tokenBlacklistService) {
        this.roomService = roomService;
        this.itemService = itemService;
        this.effectService = effectService;
        this.mobileService = mobileService;
        this.configService = configService;
        this.storeService = storeService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // --- ENUM REFERENCE LISTS ---

    /**
     * Retrieves all possible wear locations as a list of strings for the MCP to consume.
     *
     * @return a list of valid wear location strings
     */
    @Tool(description = "Get a list of all Wear Locations for Items")
    public List<String> getWearLocations() {
        log.info("MCP API Call: getWearLocations");
        return Arrays.stream(WearLocation.values())
                .filter(w -> w != WearLocation.NONE)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all possible item types as a list of strings.
     *
     * @return a list of valid item type strings
     */
    @Tool(description = "Get a list of all Item Types")
    public List<String> getItemTypes() {
        log.info("MCP API Call: getItemTypes");
        return Arrays.stream(ItemType.values())
                .filter(t -> t != ItemType.NONE)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all possible room types as a list of strings.
     *
     * @return a list of valid room type strings
     */
    @Tool(description = "Get a list of all Room Types")
    public List<String> getRoomTypes() {
        log.info("MCP API Call: getRoomTypes");
        return Arrays.stream(RoomType.values())
                .filter(r -> r != RoomType.UNKNOWN)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all registered skill types by querying the config service.
     *
     * @return a list of valid skill names
     */
    @Tool(description = "Get a list of all Skill Types (Registries)")
    public List<String> getSkillTypes() {
        log.info("MCP API Call: getSkillTypes");
        return awaitList(configService.getAllSkills().map(SkillRegistry::getName), "get skills");
    }

    /**
     * Retrieves all base effect enumeration types as strings.
     *
     * @return a list of valid effect type names
     */
    @Tool(description = "Get a list of all base Effect Types")
    public List<String> getEffectTypes() {
        log.info("MCP API Call: getEffectTypes");
        return Arrays.stream(EffectType.values()).map(Enum::name).collect(Collectors.toList());
    }

    /**
     * Retrieves all active configured effect entities from the database.
     *
     * @return a list of effects
     */
    @Tool(description = "Get a list of all active Effect entities")
    public List<Effect> getEffects() {
        log.info("MCP API Call: getEffects");
        return awaitList(effectService.getAllEffects(), "get effects");
    }

    // --- ITEM CRUD ---

    /**
     * Exposes an MCP tool for creating an entirely new item via the LLM agent.
     * Validates enumerated types and parses effect IDs before persisting.
     *
     * @param name         the item name
     * @param description  the item description
     * @param itemType     the exact string matching an ItemType enum
     * @param wearLocation the exact string matching a WearLocation enum
     * @param stackable    true if stackable
     * @param property1    property slot 1
     * @param property2    property slot 2
     * @param property3    property slot 3
     * @param property4    property slot 4
     * @param effectIdsStr a string of comma-separated effect IDs
     * @return the saved database entity
     */
    @Tool(description = "Create a new item")
    public Item createItem(
            @ToolParam(description = "Item name. REQUIRED.") String name,
            @ToolParam(description = "Item description. REQUIRED.") String description,
            @ToolParam(description = "Item type. REQUIRED. Must be EXACTLY ONE OF: WEAPON, TWO_HANDED_WEAPON, RANGED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR, FOOD, DRINK, POTION, BOOK, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC") String itemType,
            @ToolParam(description = "Wear location. REQUIRED. Must be EXACTLY ONE OF: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, FINGER, WRIST, NECK, EAR, FACE, WAIST, PRIMARY, OFFHAND") String wearLocation,
            @ToolParam(description = "Is stackable") Boolean stackable,
            @ToolParam(description = "Property 1 cost/value") Integer property1,
            @ToolParam(description = "Property 2") Integer property2,
            @ToolParam(description = "Property 3") Integer property3,
            @ToolParam(description = "Property 4") Integer property4,
            @ToolParam(description = "Comma-separated list of Effect IDs (e.g. '1, 2')") String effectIdsStr) {

        List<Long> effectIds = parseIds(effectIdsStr);
        log.info(
                "MCP API Call: createItem(name={}, itemType={}, wearLocation={}, stackable={}, effectIdsStr={}(parsed={}))",
                name, itemType, wearLocation, stackable, effectIdsStr, effectIds);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Item name is required.");
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Item description is required.");
        if (itemType == null || itemType.isBlank())
            throw new IllegalArgumentException("Item type is required.");
        if (wearLocation == null || wearLocation.isBlank())
            throw new IllegalArgumentException("Wear location is required.");

        Item item = new Item();
        item.setName(name);
        item.setDescription(description);

        ItemType parsedType = ItemType.fromString(itemType);
        if (parsedType == ItemType.NONE) {
            String allowed = Arrays.stream(ItemType.values()).filter(v -> v != ItemType.NONE).map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid itemType: " + itemType + ". Allowed: [" + allowed + "]");
        }
        item.setItemType(parsedType);

        WearLocation parsedLoc = WearLocation.fromString(wearLocation);
        if (parsedLoc == WearLocation.NONE) {
            String allowed = Arrays.stream(WearLocation.values()).filter(v -> v != WearLocation.NONE).map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid wearLocation: " + wearLocation + ". Allowed: [" + allowed + "]");
        }
        item.setWearLocation(parsedLoc);

        if (stackable != null)
            item.setStackable(stackable);
        item.setProperty1(property1 == null ? 0 : property1);
        item.setProperty2(property2 == null ? 0 : property2);
        item.setProperty3(property3 == null ? 0 : property3);
        item.setProperty4(property4 == null ? 0 : property4);

        return await(itemService.saveItem(item).flatMap(saved -> {
            if (!effectIds.isEmpty()) {
                return Flux.fromIterable(effectIds)
                        .flatMap(effectId -> effectService.linkItemAndEffect(saved.getId(), effectId))
                        .then(itemService.getItem(saved.getId()));
            }
            return Mono.just(saved);
        }), "create item");
    }

    /**
     * Retrieves a single item by its ID.
     *
     * @param id the item's database ID
     * @return the fully populated item
     */
    @Tool(description = "Retrieve an item")
    public Item getItem(Long id) {
        log.info("MCP API Call: getItem(id={})", id);
        return await(itemService.getItem(id), "get item");
    }

    /**
     * Exposes an MCP tool for updating properties of an existing item.
     *
     * @param id           the item ID to modify
     * @param name         the updated item name
     * @param description  the updated item description
     * @param itemType     the updated string matching an ItemType enum
     * @param wearLocation the updated string matching a WearLocation enum
     * @param stackable    the updated stackable boolean
     * @param property1    property slot 1
     * @param property2    property slot 2
     * @param property3    property slot 3
     * @param property4    property slot 4
     * @param effectIdsStr the updated comma-separated effect IDs
     * @return the saved database item
     */
    @Tool(description = "Update an existing item")
    public Item updateItem(
            @ToolParam(description = "Item ID") Long id,
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Item description") String description,
            @ToolParam(description = "Item type. REQUIRED. Must be EXACTLY ONE OF: WEAPON, TWO_HANDED_WEAPON, RANGED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR, FOOD, DRINK, POTION, BOOK, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC") String itemType,
            @ToolParam(description = "Wear location. REQUIRED. Must be EXACTLY ONE OF: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, FINGER, WRIST, NECK, EAR, FACE, WAIST, PRIMARY, OFFHAND") String wearLocation,
            @ToolParam(description = "Is stackable") Boolean stackable,
            @ToolParam(description = "Property 1") Integer property1,
            @ToolParam(description = "Property 2") Integer property2,
            @ToolParam(description = "Property 3") Integer property3,
            @ToolParam(description = "Property 4") Integer property4,
            @ToolParam(description = "Comma-separated list of Effect IDs (e.g. '1, 2')") String effectIdsStr) {

        List<Long> effectIds = parseIds(effectIdsStr);
        log.info(
                "MCP API Call: updateItem(id={}, name={}, itemType={}, wearLocation={}, stackable={}, effectIdsStr={}(parsed={}))",
                id, name, itemType, wearLocation, stackable, effectIdsStr, effectIds);

        return await(itemService.getItem(id).flatMap(item -> {
            if (name != null)
                item.setName(name);
            if (description != null)
                item.setDescription(description);
            if (itemType != null) {
                ItemType parsedType = ItemType.fromString(itemType);
                if (parsedType == ItemType.NONE) {
                    String allowed = Arrays.stream(ItemType.values()).filter(v -> v != ItemType.NONE).map(Enum::name)
                            .collect(Collectors.joining(", "));
                    throw new IllegalArgumentException(
                            "Invalid itemType: " + itemType + ". Allowed: [" + allowed + "]");
                }
                item.setItemType(parsedType);
            }
            if (wearLocation != null) {
                WearLocation parsedLoc = WearLocation.fromString(wearLocation);
                if (parsedLoc == WearLocation.NONE) {
                    String allowed = Arrays.stream(WearLocation.values()).filter(v -> v != WearLocation.NONE)
                            .map(Enum::name).collect(Collectors.joining(", "));
                    throw new IllegalArgumentException(
                            "Invalid wearLocation: " + wearLocation + ". Allowed: [" + allowed + "]");
                }
                item.setWearLocation(parsedLoc);
            }
            if (stackable != null)
                item.setStackable(stackable);
            if (property1 != null)
                item.setProperty1(property1);
            if (property2 != null)
                item.setProperty2(property2);
            if (property3 != null)
                item.setProperty3(property3);
            if (property4 != null)
                item.setProperty4(property4);

            return itemService.saveItem(item).flatMap(saved -> {
                if (effectIdsStr != null) {
                    return effectService.deleteByItemId(saved.getId())
                            .thenMany(Flux.fromIterable(effectIds))
                            .flatMap(effectId -> effectService.linkItemAndEffect(saved.getId(), effectId))
                            .then(itemService.getItem(saved.getId()));
                }
                return Mono.just(saved);
            });
        }), "update item");
    }

    /**
     * Retrieves an unfiltered list of all item entities.
     *
     * @return the complete item list
     */
    @Tool(description = "List all items")
    public List<Item> listItems() {
        log.info("MCP API Call: listItems");
        return awaitList(itemService.getAllItems(), "list items");
    }

    // --- MOBILE CRUD ---

    /**
     * Allows an MCP agent to construct a new mobile (NPC) and attach initial inventory.
     *
     * @param name                the mobile's display name
     * @param roomId              the room ID where the mobile rests
     * @param strength            the base strength stat
     * @param dexterity           the base dexterity stat
     * @param constitution        the base constitution stat
     * @param intelligence        the base intelligence stat
     * @param wisdom              the base wisdom stat
     * @param charisma            the base charisma stat
     * @param inventoryItemIdsStr comma-separated list of item IDs to place in inventory
     * @return the created mobile
     */
    @Tool(description = "Create a new Mobile (NPC)")
    public Mobile createMobile(
            @ToolParam(description = "Name. REQUIRED.") String name,
            @ToolParam(description = "Room ID. REQUIRED.") Long roomId,
            @ToolParam(description = "Strength") Integer strength,
            @ToolParam(description = "Dexterity") Integer dexterity,
            @ToolParam(description = "Constitution") Integer constitution,
            @ToolParam(description = "Intelligence") Integer intelligence,
            @ToolParam(description = "Wisdom") Integer wisdom,
            @ToolParam(description = "Charisma") Integer charisma,
            @ToolParam(description = "Comma-separated list of Inventory Item IDs") String inventoryItemIdsStr) {

        List<Long> inventoryItemIds = parseIds(inventoryItemIdsStr);
        log.info("MCP API Call: createMobile(name={}, roomId={}, inventoryItemIdsStr={}(parsed={}))", name, roomId,
                inventoryItemIdsStr, inventoryItemIds);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Mobile name is required.");
        if (roomId == null)
            throw new IllegalArgumentException("Room ID is required.");

        Mobile mobile = new Mobile();
        mobile.setName(name);
        mobile.setCurrentRoomId(roomId);
        if (strength != null)
            mobile.setStrength(strength);
        if (dexterity != null)
            mobile.setDexterity(dexterity);
        if (constitution != null)
            mobile.setConstitution(constitution);
        if (intelligence != null)
            mobile.setIntelligence(intelligence);
        if (wisdom != null)
            mobile.setWisdom(wisdom);
        if (charisma != null)
            mobile.setCharisma(charisma);

        return await(mobileService.saveMobile(mobile).flatMap(saved -> {
            if (!inventoryItemIds.isEmpty()) {
                return Flux.fromIterable(inventoryItemIds)
                        .flatMap(itemId -> mobileService.addItemToInventory(saved.getId(), itemId))
                        .then(Mono.just(saved));
            }
            return Mono.just(saved);
        }), "create mobile");
    }

    /**
     * Retrieves a single mobile by its ID.
     *
     * @param id the mobile's ID
     * @return the fully populated mobile
     */
    @Tool(description = "Retrieve a Mobile")
    public Mobile getMobile(Long id) {
        log.info("MCP API Call: getMobile(id={})", id);
        return await(mobileService.getMobile(id), "get mobile");
    }

    /**
     * Allows an MCP agent to update attributes on an existing generic mobile.
     *
     * @param id                  the mobile's ID
     * @param name                the mobile's updated display name
     * @param roomId              the updated room ID to move the mobile
     * @param strength            the base strength stat
     * @param dexterity           the base dexterity stat
     * @param constitution        the base constitution stat
     * @param intelligence        the base intelligence stat
     * @param wisdom              the base wisdom stat
     * @param charisma            the base charisma stat
     * @param inventoryItemIdsStr comma-separated list of items to ensure exist in inventory
     * @return the saved mobile
     */
    @Tool(description = "Update an existing Mobile")
    public Mobile updateMobile(
            @ToolParam(description = "Mobile ID") Long id,
            @ToolParam(description = "Name") String name,
            @ToolParam(description = "Room ID") Long roomId,
            @ToolParam(description = "Strength") Integer strength,
            @ToolParam(description = "Dexterity") Integer dexterity,
            @ToolParam(description = "Constitution") Integer constitution,
            @ToolParam(description = "Intelligence") Integer intelligence,
            @ToolParam(description = "Wisdom") Integer wisdom,
            @ToolParam(description = "Charisma") Integer charisma,
            @ToolParam(description = "Comma-separated list of Inventory Item IDs") String inventoryItemIdsStr) {

        List<Long> inventoryItemIds = parseIds(inventoryItemIdsStr);
        log.info("MCP API Call: updateMobile(id={}, name={}, roomId={}, inventoryItemIdsStr={}(parsed={}))", id, name,
                roomId, inventoryItemIdsStr, inventoryItemIds);

        return await(mobileService.getMobile(id).flatMap(mobile -> {
            if (name != null)
                mobile.setName(name);
            if (roomId != null)
                mobile.setCurrentRoomId(roomId);
            if (strength != null)
                mobile.setStrength(strength);
            if (dexterity != null)
                mobile.setDexterity(dexterity);
            if (constitution != null)
                mobile.setConstitution(constitution);
            if (intelligence != null)
                mobile.setIntelligence(intelligence);
            if (wisdom != null)
                mobile.setWisdom(wisdom);
            if (charisma != null)
                mobile.setCharisma(charisma);

            return mobileService.saveMobile(mobile).flatMap(saved -> {
                if (inventoryItemIdsStr != null && !inventoryItemIds.isEmpty()) {
                    return Flux.fromIterable(inventoryItemIds)
                            .flatMap(itemId -> mobileService.addItemToInventory(saved.getId(), itemId))
                            .then(Mono.just(saved));
                }
                return Mono.just(saved);
            });
        }), "update mobile");
    }

    /**
     * Retrieves an unfiltered list of all mobile entities.
     *
     * @return the complete mobile list
     */
    @Tool(description = "List all Mobiles")
    public List<Mobile> listMobiles() {
        log.info("MCP API Call: listMobiles");
        return awaitList(mobileService.getAllMobiles(), "list mobiles");
    }

    // --- ROOM CRUD ---

    /**
     * Exposes an MCP tool for the generation and linkage of a newly mapped geographical room.
     *
     * @param name        the display name of the room
     * @param description the verbose environment description
     * @param roomType    a string perfectly matching a RoomType enum
     * @param northId     the ID of the room to logically link to the North
     * @param southId     the ID of the room to logically link to the South
     * @param eastId      the ID of the room to logically link to the East
     * @param westId      the ID of the room to logically link to the West
     * @param upId        the ID of the room to logically link to the Up
     * @param downId      the ID of the room to logically link to the Down
     * @return the newly committed room instance
     */
    @Tool(description = "Create a new Room")
    public Room createRoom(
            @ToolParam(description = "Room name. REQUIRED.") String name,
            @ToolParam(description = "Room description. REQUIRED.") String description,
            @ToolParam(description = "Room type. REQUIRED. Must be EXACTLY ONE OF: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON") String roomType,
            @ToolParam(description = "Location North Room ID") Long northId,
            @ToolParam(description = "Location South Room ID") Long southId,
            @ToolParam(description = "Location East Room ID") Long eastId,
            @ToolParam(description = "Location West Room ID") Long westId,
            @ToolParam(description = "Location Up Room ID") Long upId,
            @ToolParam(description = "Location Down Room ID") Long downId) {

        log.info("MCP API Call: createRoom(name={}, roomType={}, N={}, S={}, E={}, W={}, U={}, D={})", name, roomType,
                northId, southId, eastId, westId, upId, downId);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Room name is required.");
        if (description == null || description.isBlank())
            throw new IllegalArgumentException("Room description is required.");
        if (roomType == null || roomType.isBlank())
            throw new IllegalArgumentException("Room type is required.");

        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        try {
            RoomType parsed = RoomType.valueOf(roomType.toUpperCase());
            if (parsed == RoomType.UNKNOWN)
                throw new IllegalArgumentException();
            room.setRoomType(parsed);
        } catch (IllegalArgumentException e) {
            String allowed = Arrays.stream(RoomType.values()).filter(v -> v != RoomType.UNKNOWN).map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid roomType: " + roomType + ". Allowed: [" + allowed + "]");
        }

        if (northId != null) {
            room.setNorthId(northId);
            room.setNorthDoor(true);
        }
        if (southId != null) {
            room.setSouthId(southId);
            room.setSouthDoor(true);
        }
        if (eastId != null) {
            room.setEastId(eastId);
            room.setEastDoor(true);
        }
        if (westId != null) {
            room.setWestId(westId);
            room.setWestDoor(true);
        }
        if (upId != null) {
            room.setUpId(upId);
            room.setUpDoor(true);
        }
        if (downId != null) {
            room.setDownId(downId);
            room.setDownDoor(true);
        }

        return await(roomService.saveRoom(room), "create room");
    }

    /**
     * Retrieves a singular room via ID.
     *
     * @param id the internal ID assigned to a room
     * @return a single room object
     */
    @Tool(description = "Retrieve a Room")
    public Room getRoom(Long id) {
        log.info("MCP API Call: getRoom(id={})", id);
        return await(roomService.getRoom(id), "get room");
    }

    /**
     * Exposes an MCP tool for modifying the parameters or exits of an existing room.
     *
     * @param id          the room's unique ID
     * @param name        the display name of the room
     * @param description the verbose environment description
     * @param roomType    a string perfectly matching a RoomType enum
     * @param northId     the ID of the room to logically link to the North
     * @param southId     the ID of the room to logically link to the South
     * @param eastId      the ID of the room to logically link to the East
     * @param westId      the ID of the room to logically link to the West
     * @param upId        the ID of the room to logically link to the Up
     * @param downId      the ID of the room to logically link to the Down
     * @return the successfully modified room object
     */
    @Tool(description = "Update an existing Room")
    public Room updateRoom(
            @ToolParam(description = "Room ID") Long id,
            @ToolParam(description = "Room name") String name,
            @ToolParam(description = "Room description") String description,
            @ToolParam(description = "Room type. Must be EXACTLY ONE OF: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON") String roomType,
            @ToolParam(description = "Location North Room ID") Long northId,
            @ToolParam(description = "Location South Room ID") Long southId,
            @ToolParam(description = "Location East Room ID") Long eastId,
            @ToolParam(description = "Location West Room ID") Long westId,
            @ToolParam(description = "Location Up Room ID") Long upId,
            @ToolParam(description = "Location Down Room ID") Long downId) {

        log.info("MCP API Call: updateRoom(id={}, name={}, roomType={}, N={}, S={}, E={}, W={}, U={}, D={})", id, name,
                roomType, northId, southId, eastId, westId, upId, downId);

        return await(roomService.getRoom(id).flatMap(room -> {
            if (name != null)
                room.setName(name);
            if (description != null)
                room.setDescription(description);
            if (roomType != null) {
                try {
                    RoomType parsed = RoomType.valueOf(roomType.toUpperCase());
                    if (parsed == RoomType.UNKNOWN)
                        throw new IllegalArgumentException();
                    room.setRoomType(parsed);
                } catch (IllegalArgumentException e) {
                    String allowed = Arrays.stream(RoomType.values()).filter(v -> v != RoomType.UNKNOWN).map(Enum::name)
                            .collect(Collectors.joining(", "));
                    throw new IllegalArgumentException(
                            "Invalid roomType: " + roomType + ". Allowed: [" + allowed + "]");
                }
            }
            if (northId != null) {
                room.setNorthId(northId);
                room.setNorthDoor(true);
            }
            if (southId != null) {
                room.setSouthId(southId);
                room.setSouthDoor(true);
            }
            if (eastId != null) {
                room.setEastId(eastId);
                room.setEastDoor(true);
            }
            if (westId != null) {
                room.setWestId(westId);
                room.setWestDoor(true);
            }
            if (upId != null) {
                room.setUpId(upId);
                room.setUpDoor(true);
            }
            if (downId != null) {
                room.setDownId(downId);
                room.setDownDoor(true);
            }

            return roomService.saveRoom(room);
        }), "update room");
    }

    /**
     * Retrieves an unfiltered list of all rooms.
     *
     * @return the list
     */
    @Tool(description = "List all Rooms")
    public List<Room> listRooms() {
        log.info("MCP API Call: listRooms");
        return awaitList(roomService.getAllRooms(), "list rooms");
    }

    // --- STORES CRUD ---

    /**
     * Provides an MCP tool logic allowing AI to create in-game merchant stores.
     *
     * @param name        the name of the store abstraction
     * @param description contextual data about the store
     * @param itemIdsStr  comma separated IDs of the items immediately available to sell
     * @return the persistent store entity representation
     */
    @Tool(description = "Create a new Store")
    public Store createStore(
            @ToolParam(description = "Store name. REQUIRED.") String name,
            @ToolParam(description = "Store description") String description,
            @ToolParam(description = "Comma-separated list of Item IDs available in store") String itemIdsStr) {

        List<Long> itemIds = parseIds(itemIdsStr);
        log.info("MCP API Call: createStore(name={}, itemIdsStr={}(parsed={}))", name, itemIdsStr, itemIds);

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Store name is required.");

        Store store = new Store();
        store.setName(name);
        if (description != null)
            store.setDescription(description);

        return await(storeService.createStore(store).flatMap(saved -> {
            if (!itemIds.isEmpty()) {
                return Flux.fromIterable(itemIds)
                        .flatMap(itemId -> storeService.addStoreItem(saved.getId(), itemId))
                        .then(Mono.just(saved));
            }
            return Mono.just(saved);
        }), "create store");
    }

    /**
     * Retrieves a single in-game store entity representation.
     *
     * @param id the identifier of a store abstraction
     * @return the fully resolved Store instance
     */
    @Tool(description = "Retrieve a Store")
    public Store getStore(Long id) {
        log.info("MCP API Call: getStore(id={})", id);
        return await(storeService.getStore(id), "get store");
    }

    /**
     * Provides an MCP tool logic allowing AI to update the stock or description of an existing store.
     *
     * @param id            the integer ID
     * @param name          the optionally modified string
     * @param description   the optionally modified text explanation
     * @param addItemIdsStr an optional string representing new IDs to supplement the existing items
     * @return the updated Store element
     */
    @Tool(description = "Update an existing Store")
    public Store updateStore(
            @ToolParam(description = "Store ID") Long id,
            @ToolParam(description = "Store name") String name,
            @ToolParam(description = "Store description") String description,
            @ToolParam(description = "Comma-separated list of Item IDs to add to store") String addItemIdsStr) {

        List<Long> addItemIds = parseIds(addItemIdsStr);
        log.info("MCP API Call: updateStore(id={}, name={}, addItemIdsStr={}(parsed={}))", id, name, addItemIdsStr,
                addItemIds);

        Store store = new Store();
        if (name != null)
            store.setName(name);
        if (description != null)
            store.setDescription(description);

        return await(storeService.updateStore(id, store).flatMap(saved -> {
            if (addItemIdsStr != null && !addItemIds.isEmpty()) {
                return Flux.fromIterable(addItemIds)
                        .flatMap(itemId -> storeService.addStoreItem(saved.getId(), itemId))
                        .then(Mono.just(saved));
            }
            return Mono.just(saved);
        }), "update store");
    }

    /**
     * Retrieves a fully unbounded list of configured store entities in the active persistence matrix.
     *
     * @return the configured lists array
     */
    @Tool(description = "List all Stores")
    public List<Store> listStores() {
        log.info("MCP API Call: listStores");
        return awaitList(storeService.getAllStores(), "list stores");
    }

    // --- SECURITY CRUD ---

    /**
     * Allows an MCP agent to immediately invalidate a user's session globally.
     *
     * @param username the username to invalidate
     * @return a success message
     */
    @Tool(description = "Invalidate a user session across all instances")
    public String invalidateUserSession(@ToolParam(description = "The username to invalidate. REQUIRED.") String username) {
        log.info("MCP API Call: invalidateUserSession(username={})", username);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        await(tokenBlacklistService.invalidateUser(username), "invalidate user");
        return "User session for '" + username + "' has been invalidated.";
    }

    // --- HELPER METHODS ---

    private List<Long> parseIds(String idString) {
        if (idString == null || idString.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(idString.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private <T> T await(Mono<T> mono, String operation) {
        try {
            T result = mono.subscribeOn(Schedulers.boundedElastic())
                    .toFuture()
                    .get(TOOL_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            return result;
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
        List<T> results = await(flux.collectList(), operation);
        return results;
    }
}
