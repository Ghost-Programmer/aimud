package com.aimud.aimud.service;

import com.aimud.aimud.model.*;
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

    public McpToolService(RoomService roomService, ItemService itemService, EffectService effectService,
            MobileService mobileService, ConfigService configService, StoreService storeService) {
        this.roomService = roomService;
        this.itemService = itemService;
        this.effectService = effectService;
        this.mobileService = mobileService;
        this.configService = configService;
        this.storeService = storeService;
    }

    // --- ENUM REFERENCE LISTS ---

    @Tool(description = "Get a list of all Wear Locations for Items")
    public List<String> getWearLocations() {
        log.info("MCP API Call: getWearLocations");
        return Arrays.stream(WearLocation.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Tool(description = "Get a list of all Item Types")
    public List<String> getItemTypes() {
        log.info("MCP API Call: getItemTypes");
        return Arrays.stream(ItemType.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Tool(description = "Get a list of all Room Types")
    public List<String> getRoomTypes() {
        log.info("MCP API Call: getRoomTypes");
        return Arrays.stream(RoomType.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Tool(description = "Get a list of all Skill Types (Registries)")
    public List<String> getSkillTypes() {
        log.info("MCP API Call: getSkillTypes");
        return awaitList(configService.getAllSkills().map(SkillRegistry::getName), "get skills");
    }

    @Tool(description = "Get a list of all base Effect Types")
    public List<String> getEffectTypes() {
        log.info("MCP API Call: getEffectTypes");
        return Arrays.stream(EffectType.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Tool(description = "Get a list of all active Effect entities")
    public List<Effect> getEffects() {
        log.info("MCP API Call: getEffects");
        return awaitList(effectService.getAllEffects(), "get effects");
    }

    // --- ITEM CRUD ---

    @Tool(description = "Create a new item")
    public Item createItem(
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Item description") String description,
            @ToolParam(description = "Item type. Must be EXACTLY ONE OF: WEAPON, TWO_HANDED_WEAPON, RANGED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR, FOOD, DRINK, POTION, BOOK, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC, NONE") String itemType,
            @ToolParam(description = "Wear location. Must be EXACTLY ONE OF: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, FINGER, WRIST, NECK, EAR, FACE, WAIST, PRIMARY, OFFHAND, NONE") String wearLocation,
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

        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        if (itemType != null) {
            ItemType parsedType = ItemType.fromString(itemType);
            if (parsedType == ItemType.NONE && !itemType.equalsIgnoreCase("NONE")) {
                log.error("Invalid itemType: {}", itemType);
                throw new IllegalArgumentException(
                        "Invalid itemType: " + itemType + ". Allowed: " + Arrays.toString(ItemType.values()));
            }
            item.setItemType(parsedType);
        }
        if (wearLocation != null) {
            WearLocation parsedLoc = WearLocation.fromString(wearLocation);
            if (parsedLoc == WearLocation.NONE && !wearLocation.equalsIgnoreCase("NONE")) {
                log.error("Invalid wearLocation: {}", wearLocation);
                throw new IllegalArgumentException("Invalid wearLocation: " + wearLocation + ". Allowed: "
                        + Arrays.toString(WearLocation.values()));
            }
            item.setWearLocation(parsedLoc);
        }
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

    @Tool(description = "Retrieve an item")
    public Item getItem(Long id) {
        log.info("MCP API Call: getItem(id={})", id);
        return await(itemService.getItem(id), "get item");
    }

    @Tool(description = "Update an existing item")
    public Item updateItem(
            @ToolParam(description = "Item ID") Long id,
            @ToolParam(description = "Item name") String name,
            @ToolParam(description = "Item description") String description,
            @ToolParam(description = "Item type. Must be EXACTLY ONE OF: WEAPON, TWO_HANDED_WEAPON, RANGED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR, FOOD, DRINK, POTION, BOOK, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC, NONE") String itemType,
            @ToolParam(description = "Wear location. Must be EXACTLY ONE OF: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, FINGER, WRIST, NECK, EAR, FACE, WAIST, PRIMARY, OFFHAND, NONE") String wearLocation,
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
                if (parsedType == ItemType.NONE && !itemType.equalsIgnoreCase("NONE")) {
                    throw new IllegalArgumentException(
                            "Invalid itemType: " + itemType + ". Allowed: " + Arrays.toString(ItemType.values()));
                }
                item.setItemType(parsedType);
            }
            if (wearLocation != null) {
                WearLocation parsedLoc = WearLocation.fromString(wearLocation);
                if (parsedLoc == WearLocation.NONE && !wearLocation.equalsIgnoreCase("NONE")) {
                    throw new IllegalArgumentException("Invalid wearLocation: " + wearLocation + ". Allowed: "
                            + Arrays.toString(WearLocation.values()));
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

    @Tool(description = "List all items")
    public List<Item> listItems() {
        log.info("MCP API Call: listItems");
        return awaitList(itemService.getAllItems(), "list items");
    }

    // --- MOBILE CRUD ---

    @Tool(description = "Create a new Mobile (NPC)")
    public Mobile createMobile(
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
        log.info("MCP API Call: createMobile(name={}, roomId={}, inventoryItemIdsStr={}(parsed={}))", name, roomId,
                inventoryItemIdsStr, inventoryItemIds);

        Mobile mobile = new Mobile();
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

        return await(mobileService.saveMobile(mobile).flatMap(saved -> {
            if (!inventoryItemIds.isEmpty()) {
                return Flux.fromIterable(inventoryItemIds)
                        .flatMap(itemId -> mobileService.addItemToInventory(saved.getId(), itemId))
                        .then(Mono.just(saved));
            }
            return Mono.just(saved);
        }), "create mobile");
    }

    @Tool(description = "Retrieve a Mobile")
    public Mobile getMobile(Long id) {
        log.info("MCP API Call: getMobile(id={})", id);
        return await(mobileService.getMobile(id), "get mobile");
    }

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

    @Tool(description = "List all Mobiles")
    public List<Mobile> listMobiles() {
        log.info("MCP API Call: listMobiles");
        return awaitList(mobileService.getAllMobiles(), "list mobiles");
    }

    // --- ROOM CRUD ---

    @Tool(description = "Create a new Room")
    public Room createRoom(
            @ToolParam(description = "Room name") String name,
            @ToolParam(description = "Room description") String description,
            @ToolParam(description = "Room type. Must be EXACTLY ONE OF: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON, UNKNOWN") String roomType,
            @ToolParam(description = "Location North Room ID") Long northId,
            @ToolParam(description = "Location South Room ID") Long southId,
            @ToolParam(description = "Location East Room ID") Long eastId,
            @ToolParam(description = "Location West Room ID") Long westId,
            @ToolParam(description = "Location Up Room ID") Long upId,
            @ToolParam(description = "Location Down Room ID") Long downId) {

        log.info("MCP API Call: createRoom(name={}, roomType={}, N={}, S={}, E={}, W={}, U={}, D={})", name, roomType,
                northId, southId, eastId, westId, upId, downId);

        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        if (roomType != null) {
            try {
                room.setRoomType(RoomType.valueOf(roomType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid roomType: " + roomType + ". Allowed: " + Arrays.toString(RoomType.values()));
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

        return await(roomService.saveRoom(room), "create room");
    }

    @Tool(description = "Retrieve a Room")
    public Room getRoom(Long id) {
        log.info("MCP API Call: getRoom(id={})", id);
        return await(roomService.getRoom(id), "get room");
    }

    @Tool(description = "Update an existing Room")
    public Room updateRoom(
            @ToolParam(description = "Room ID") Long id,
            @ToolParam(description = "Room name") String name,
            @ToolParam(description = "Room description") String description,
            @ToolParam(description = "Room type. Must be EXACTLY ONE OF: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON, UNKNOWN") String roomType,
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
                    room.setRoomType(RoomType.valueOf(roomType.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Invalid roomType: " + roomType + ". Allowed: " + Arrays.toString(RoomType.values()));
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

    @Tool(description = "List all Rooms")
    public List<Room> listRooms() {
        log.info("MCP API Call: listRooms");
        return awaitList(roomService.getAllRooms(), "list rooms");
    }

    // --- STORES CRUD ---

    @Tool(description = "Create a new Store")
    public Store createStore(
            @ToolParam(description = "Store name") String name,
            @ToolParam(description = "Store description") String description,
            @ToolParam(description = "Comma-separated list of Item IDs available in store") String itemIdsStr) {

        List<Long> itemIds = parseIds(itemIdsStr);
        log.info("MCP API Call: createStore(name={}, itemIdsStr={}(parsed={}))", name, itemIdsStr, itemIds);

        Store store = new Store();
        store.setName(name);
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

    @Tool(description = "Retrieve a Store")
    public Store getStore(Long id) {
        log.info("MCP API Call: getStore(id={})", id);
        return await(storeService.getStore(id), "get store");
    }

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

    @Tool(description = "List all Stores")
    public List<Store> listStores() {
        log.info("MCP API Call: listStores");
        return awaitList(storeService.getAllStores(), "list stores");
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
