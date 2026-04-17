package io.nadia.ai.aimud.model;

import java.util.List;

/**
 * Container class holding WebSocket Data Transfer Objects for asynchronous store transaction syncs.
 */
public class StorePayloads {
    /**
     * DTO mapping a merchant's inventory slot, attaching calculated buy price logic based on local charisma margins.
     */
    public record StoreItemDTO(Long itemId, Item item, int available, int buyPrice) {}
    
    /**
     * DTO mapping a player's sellable inventory, attaching calculated return value yields.
     */
    public record PlayerItemDTO(Long itemId, Item item, int sellPrice) {}
    
    /**
     * Fully localized merchant session state, combining the shopkeeper's contextual relations with the
     * active player's financial limits.
     */
    public record StoreDialogPayload(Long storeId, String shopkeeperName, int factionRating, int charisma, int playerGold, List<StoreItemDTO> storeItems, List<PlayerItemDTO> playerInventory) {}
}
