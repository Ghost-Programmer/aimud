package io.nadia.ai.aimud.model;

/**
 * A data transfer object representing a grouped relationship between an Item and an Effect.
 * Useful for extracting bulk elements via DatabaseClient.
 */
public record ItemEffectDTO(Long itemId, Effect effect) {
}
