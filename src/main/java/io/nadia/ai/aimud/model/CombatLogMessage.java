package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a compressed combat log message transmitted via the WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombatLogMessage {
    private Long roomId;
    private Long characterId;
    private String message;
    private long timestamp;
}
