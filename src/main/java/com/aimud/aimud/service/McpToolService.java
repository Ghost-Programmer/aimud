package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

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
    public Room createRoom(Room room) {
        log.info("MCP Tool: Creating room: {}", room.getName());
        return roomService.saveRoom(room).block();
    }

    @Tool(description = "Update an existing room")
    public Room updateRoom(Room room) {
        log.info("MCP Tool: Updating room: {} (id: {})", room.getName(), room.getId());
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
    public Item createItem(Item item) {
        log.info("MCP Tool: Creating item: {}", item.getName());
        return itemService.saveItem(item).block();
    }

    @Tool(description = "Update an existing item template")
    public Item updateItem(Item item) {
        log.info("MCP Tool: Updating item: {} (id: {})", item.getName(), item.getId());
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
    public Effect createEffect(Effect effect) {
        log.info("MCP Tool: Creating effect: {} for item: {}", effect.getEffectType(), effect.getItemId());
        return effectService.saveEffect(effect).block();
    }

    @Tool(description = "Update an existing effect")
    public Effect updateEffect(Effect effect) {
        log.info("MCP Tool: Updating effect: {} (id: {})", effect.getEffectType(), effect.getId());
        return effectService.saveEffect(effect).block();
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
