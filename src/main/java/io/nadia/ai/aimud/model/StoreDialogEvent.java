package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDialogEvent {
    private Long characterId;
    private Long storeId;
    private String shopkeeperName;
}
