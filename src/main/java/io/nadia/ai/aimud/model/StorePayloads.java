package io.nadia.ai.aimud.model;

import java.util.List;

public class StorePayloads {
    public record StoreItemDTO(Long itemId, Item item, int available, int buyPrice) {}
    public record PlayerItemDTO(Long itemId, Item item, int sellPrice) {}
    public record StoreDialogPayload(Long storeId, String shopkeeperName, int factionRating, int charisma, int playerGold, List<StoreItemDTO> storeItems, List<PlayerItemDTO> playerInventory) {}
}
