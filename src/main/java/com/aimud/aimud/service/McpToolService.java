package com.aimud.aimud.service;
import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.types.RoomType;
import com.aimud.aimud.types.EffectType;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.WearLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service providing MCP Tools for AI to manage Rooms, Items, and Effects in AIMUD.
 */
@Service
@Slf4j
public class McpToolService {

    private final RoomService roomService;
    private final ItemService itemService;
    private final EffectService effectService;

    public McpToolService(RoomService roomService, ItemService itemService, EffectService effectService) {
        this.roomService = roomService;
        this.itemService = itemService;
        this.effectService = effectService;
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
        return roomService.saveRoom(room).block();
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
        return roomService.saveRoom(room).block();
    }

    @Tool(description = "Retrieve a room by its ID")
    public Room getRoom(@ToolParam(description = "The unique ID of the room") Long id) {
        log.info("MCP Tool: Getting room with id: {}", id);
        return roomService.getRoom(id).block();
    }

    @Tool(description = "Retrieve all rooms in the world")
    public List<Room> getAllRooms() {
        log.info("MCP Tool: Getting all rooms");
        return roomService.getAllRooms().collectList().block();
    }

    // --- ITEM TOOLS ---

    @Tool(description = "Create a new item template")
    public Item createItem(
            @ToolParam(description = "The name of the item") String name,
            @ToolParam(description = "The description of the item") String description,
            @ToolParam(description = "The type of the item (e.g. WEAPON, ARMOR, POTION, etc.)") String itemType,
            @ToolParam(description = "The wear location of the item (e.g. HEAD, TORSO, PRIMARY, etc.)") String wearLocation) {
        log.info("MCP Tool: Creating item: {}", name);
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        if (itemType != null) {
            try {
                item.setItemType(ItemType.valueOf(itemType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid itemType: {}", itemType);
            }
        }
        if (wearLocation != null) {
            try {
                item.setWearLocation(WearLocation.valueOf(wearLocation.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid wearLocation: {}", wearLocation);
            }
        }
        return itemService.saveItem(item).block();
    }

    @Tool(description = "Update an existing item template")
    public Item updateItem(
            @ToolParam(description = "The ID of the item to update") Long id,
            @ToolParam(description = "The name of the item") String name,
            @ToolParam(description = "The description of the item") String description,
            @ToolParam(description = "The type of the item") String itemType,
            @ToolParam(description = "The wear location of the item") String wearLocation) {
        log.info("MCP Tool: Updating item: {} (id: {})", name, id);
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        if (itemType != null) {
            try {
                item.setItemType(ItemType.valueOf(itemType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid itemType: {}", itemType);
            }
        }
        if (wearLocation != null) {
            try {
                item.setWearLocation(WearLocation.valueOf(wearLocation.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid wearLocation: {}", wearLocation);
            }
        }
        return itemService.saveItem(item).block();
    }

    @Tool(description = "Retrieve an item template by its ID")
    public Item getItem(@ToolParam(description = "The unique ID of the item") Long id) {
        log.info("MCP Tool: Getting item with id: {}", id);
        return itemService.getItem(id).block();
    }

    @Tool(description = "Retrieve all item templates")
    public List<Item> getAllItems() {
        log.info("MCP Tool: Getting all items");
        return itemService.getAllItems().collectList().block();
    }

    // --- EFFECT TOOLS ---

    @Tool(description = "Create a new effect and optionally associate it with an item")
    public Effect createEffect(
            @ToolParam(description = "The ID of the item to associate this effect with (optional)") Long itemId,
            @ToolParam(description = "The type of the effect (e.g. SLASHING_DAMAGE, STRENGTH, ARMOR, etc.)") String effectType,
            @ToolParam(description = "The first modifier value (e.g. number of dice, or bonus amount)") int modifier1,
            @ToolParam(description = "The second modifier value (e.g. size of dice)") int modifier2,
            @ToolParam(description = "The third modifier value") int modifier3,
            @ToolParam(description = "The fourth modifier value") int modifier4) {
        log.info("MCP Tool: Creating effect: {} for item: {}", effectType, itemId);
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
        
        return effectService.saveEffect(effect)
                .flatMap(savedEffect -> {
                    if (itemId != null) {
                        return effectService.linkItemAndEffect(itemId, savedEffect.getId())
                                .thenReturn(savedEffect);
                    }
                    return Mono.just(savedEffect);
                })
                .block();
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
                .block();
    }

    @Tool(description = "Retrieve an effect by its ID")
    public Effect getEffect(@ToolParam(description = "The unique ID of the effect") Long id) {
        log.info("MCP Tool: Getting effect with id: {}", id);
        return effectService.getEffect(id).block();
    }

    @Tool(description = "Retrieve all effects associated with a specific item")
    public List<Effect> getEffectsByItem(@ToolParam(description = "The ID of the item to retrieve effects for") Long itemId) {
        log.info("MCP Tool: Getting effects for item: {}", itemId);
        return effectService.getEffectsByItem(itemId).collectList().block();
    }
}
