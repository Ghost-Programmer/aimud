package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Room;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service providing MCP Tools for AI to manage Rooms, Items, and Effects in AIMUD.
 */
@Service
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
        return roomService.saveRoom(room).block();
    }

    @Tool(description = "Update an existing room")
    public Room updateRoom(Room room) {
        return roomService.saveRoom(room).block();
    }

    @Tool(description = "Retrieve a room by its ID")
    public Room getRoom(@ToolParam(description = "The unique ID of the room") Long id) {
        return roomService.getRoom(id).block();
    }

    @Tool(description = "Retrieve all rooms in the world")
    public List<Room> getAllRooms() {
        return roomService.getAllRooms().collectList().block();
    }

    // --- ITEM TOOLS ---

    @Tool(description = "Create a new item template")
    public Item createItem(Item item) {
        return itemService.saveItem(item).block();
    }

    @Tool(description = "Update an existing item template")
    public Item updateItem(Item item) {
        return itemService.saveItem(item).block();
    }

    @Tool(description = "Retrieve an item template by its ID")
    public Item getItem(@ToolParam(description = "The unique ID of the item") Long id) {
        return itemService.getItem(id).block();
    }

    @Tool(description = "Retrieve all item templates")
    public List<Item> getAllItems() {
        return itemService.getAllItems().collectList().block();
    }

    // --- EFFECT TOOLS ---

    @Tool(description = "Create a new effect and optionally associate it with an item")
    public Effect createEffect(Effect effect) {
        return effectService.saveEffect(effect).block();
    }

    @Tool(description = "Update an existing effect")
    public Effect updateEffect(Effect effect) {
        return effectService.saveEffect(effect).block();
    }

    @Tool(description = "Retrieve an effect by its ID")
    public Effect getEffect(@ToolParam(description = "The unique ID of the effect") Long id) {
        return effectService.getEffect(id).block();
    }

    @Tool(description = "Retrieve all effects associated with a specific item")
    public List<Effect> getEffectsByItem(@ToolParam(description = "The ID of the item to retrieve effects for") Long itemId) {
        return effectService.getEffectsByItem(itemId).collectList().block();
    }
}
