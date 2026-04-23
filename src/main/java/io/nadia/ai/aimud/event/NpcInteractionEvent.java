package io.nadia.ai.aimud.event;

/**
 * Event published when a player character interacts with a non-player character.
 * This event is used to store interaction history in the vector database for RAG.
 *
 * @param npcId       the ID of the NPC
 * @param pcId        the ID of the Player Character
 * @param interaction the string representation of the interaction
 */
public record NpcInteractionEvent(Long npcId, Long pcId, String interaction) {
}
