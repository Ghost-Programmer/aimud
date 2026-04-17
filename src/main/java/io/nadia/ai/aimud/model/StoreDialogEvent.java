package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket messaging payload fired when a player initiates trading with an NPC.
 * Instructs the frontend to overlay the store buy/sell UI dialogue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDialogEvent {
    private Long characterId;
    private Long storeId;
    private String shopkeeperName;
}
