package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * WebSocket messaging payload used to synchronize group state across multiple clients.
 * Formatted dynamically whenever player health/mana or roster composition changes.
 */
@Getter
@Setter
@AllArgsConstructor
public class PartyUpdate {
    private Long characterId;
    private Long leaderId;
    private List<PartyMemberInfo> members;

    /**
     * DTO containing lightweight metric readouts representing a single active group member's status.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PartyMemberInfo {
        private Long id;
        private String name;
        private int currentHp;
        private int maxHp;
        private int currentMana;
        private int maxMana;
    }
}
