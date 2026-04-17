package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket messaging payload sent during combat lock-on or engagement.
 * Informs the client to track a specific entity's HP gauge.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetUpdate {
    private Mobile character;
    private Mobile target;
}
