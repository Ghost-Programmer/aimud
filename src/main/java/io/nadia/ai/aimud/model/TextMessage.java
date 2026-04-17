package io.nadia.ai.aimud.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Core generalized WebSocket payload carrying raw terminal string data for client rendering.
 * Used for chat, server echo, combat logs, and environmental narration.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextMessage {
    private Long characterId;
    private String content;
}
