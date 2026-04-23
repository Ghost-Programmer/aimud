package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.PartyUpdate;
import io.nadia.ai.aimud.model.StoreDialogEvent;
import io.nadia.ai.aimud.model.TargetUpdate;
import io.nadia.ai.aimud.model.TextMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Slf4j
public class CommunicationService {

    private final Sinks.Many<Mobile> characterUpdates = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<TextMessage> textMessages = Sinks.many().replay().limit(20);
    private final Sinks.Many<Mobile> logoutMessages = Sinks.many().replay().limit(10);
    private final Sinks.Many<TargetUpdate> targetUpdates = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<PartyUpdate> partyUpdates = Sinks.many().multicast().directBestEffort();
    private final Sinks.Many<StoreDialogEvent> storeDialogs = Sinks.many().multicast().directBestEffort();

    @Setter
    private CharacterService characterService;

    // Local room memory buffer utilizing infinite timebound ChatMessage payload strings
    public static record ChatMessage(String message, java.time.Instant timestamp) {}
    private final java.util.concurrent.ConcurrentHashMap<Long, java.util.LinkedList<ChatMessage>> roomChatHistory = new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ConversationService conversationService;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Retrieves the stream of character updates.
     *
     * @return a {@link Flux} emitting character updates
     */
    public Flux<Mobile> getCharacterUpdates() {
        return characterUpdates.asFlux();
    }

    /**
     * Retrieves the stream of text messages.
     *
     * @return a {@link Flux} emitting text messages
     */
    public Flux<TextMessage> getTextMessages() {
        return textMessages.asFlux();
    }

    /**
     * Retrieves the stream of logout messages.
     *
     * @return a {@link Flux} emitting mobile logout events
     */
    public Flux<Mobile> getLogoutMessages() {
        return logoutMessages.asFlux();
    }

    /**
     * Retrieves the stream of target updates (e.g. for combat).
     *
     * @return a {@link Flux} emitting target updates
     */
    public Flux<TargetUpdate> getTargetUpdates() {
        return targetUpdates.asFlux();
    }

    /**
     * Retrieves the stream of party updates.
     *
     * @return a {@link Flux} emitting party updates
     */
    public Flux<PartyUpdate> getPartyUpdates() {
        return partyUpdates.asFlux();
    }

    /**
     * Retrieves the stream of store dialog events.
     *
     * @return a {@link Flux} emitting store dialog events
     */
    public Flux<StoreDialogEvent> getStoreDialogs() {
        return storeDialogs.asFlux();
    }

    /**
     * Emits a character update if the character is associated with a user.
     *
     * @param character the mobile character to update
     */
    public void sendCharacterUpdate(Mobile character) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending character update for {}", character.getName());
        characterUpdates.tryEmitNext(character);
    }

    /**
     * Emits a party update event.
     *
     * @param partyUpdate the party update to emit
     */
    public void sendPartyUpdate(PartyUpdate partyUpdate) {
        log.info("Sending party update for character {}", partyUpdate.getCharacterId());
        partyUpdates.tryEmitNext(partyUpdate);
    }

    /**
     * Emits a store dialog event if the character ID is present.
     *
     * @param event the store dialog event to emit
     */
    public void sendStoreDialog(StoreDialogEvent event) {
        if (event.getCharacterId() == null) return;
        log.info("Sending store dialog for character {} and store {}", event.getCharacterId(), event.getStoreId());
        Sinks.EmitResult result = storeDialogs.tryEmitNext(event);
        log.info("StoreDialog Emit result: {}", result);
    }

    /**
     * Emits a target update indicating a change in combat target.
     *
     * @param character the primary character
     * @param target    the new target character
     */
    public void sendTargetUpdate(Mobile character, Mobile target) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending target update for {}", character.getName());
        targetUpdates.tryEmitNext(new TargetUpdate(character, target));
    }

    /**
     * Emits a logout event if the character is associated with a user.
     *
     * @param character the character logging out
     */
    public void sendLogout(Mobile character) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending logout message for {}", character.getName());
        logoutMessages.tryEmitNext(character);
    }

    /**
     * Broadcasts a global text message to all users.
     *
     * @param message the message string to broadcast
     */
    public void sendTextMessage(String message) {
        log.info("Broadcasting text message: {}", message);
        textMessages.tryEmitNext(new TextMessage(null, message));
    }

    /**
     * Sends a direct text message to a specific character.
     *
     * @param character the recipient character
     * @param message   the message string to send
     */
    public void sendTextMessage(Mobile character, String message) {
        if (character == null || character.getId() == null || character.getUserId() == null) {
            return;
        }
        log.info("Sending text message to {}: {}", character.getName(), message);
        textMessages.tryEmitNext(new TextMessage(character.getId(), message));
    }

    /**
     * Broadcasts a message to all characters in a specific room, saving it to room history.
     *
     * @param mobile  the mobile entity initiating the message
     * @param message the message to broadcast
     */
    public void roomMessage(Mobile mobile, String message) {
        Long roomId = mobile.getCurrentRoomId();
        if (roomId != null) {
            String loggedMessage = message.trim();
            roomChatHistory.compute(roomId, (k, v) -> {
                if (v == null)
                    v = new java.util.LinkedList<>();
                v.add(new ChatMessage(loggedMessage, java.time.Instant.now()));
                return v;
            });
            // Immediately trigger NPC conversational AI pipeline!
            conversationService.triggerRoomConversations(roomId);
        }

        this.characterService.findAllByRoomId(roomId).stream()
                .filter(c -> !c.getId().equals(mobile.getId()))
                .forEach(c -> {
                    this.sendTextMessage(c, message);
                    if (eventPublisher != null) {
                        if (mobile.getUserId() != null && c.getUserId() == null) {
                            eventPublisher.publishEvent(new io.nadia.ai.aimud.event.NpcInteractionEvent(c.getId(), mobile.getId(), message.trim()));
                        } else if (mobile.getUserId() == null && c.getUserId() != null) {
                            eventPublisher.publishEvent(new io.nadia.ai.aimud.event.NpcInteractionEvent(mobile.getId(), c.getId(), message.trim()));
                        }
                    }
                });
    }

    /**
     * Retrieves the recent chat history for a given room.
     *
     * @param roomId the ID of the room
     * @return a list of chat message strings
     */
    public java.util.List<String> getRoomHistory(Long roomId) {
        java.util.LinkedList<ChatMessage> history = roomChatHistory.get(roomId);
        if (history == null) {
            return java.util.Collections.emptyList();
        }
        // Thread-safe copy while mapping payload strings out of standard temporal wrapper
        return new java.util.ArrayList<>(history).stream().map(ChatMessage::message).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves the map of room chat histories.
     *
     * @return the concurrent hash map containing room chat history data
     */
    public java.util.concurrent.ConcurrentHashMap<Long, java.util.LinkedList<ChatMessage>> getRoomChatHistoryMap() {
        return roomChatHistory;
    }
}
