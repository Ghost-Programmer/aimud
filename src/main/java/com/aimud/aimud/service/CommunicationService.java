package com.aimud.aimud.service;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.PartyUpdate;
import com.aimud.aimud.model.TargetUpdate;
import com.aimud.aimud.model.TextMessage;
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
    private final Sinks.Many<com.aimud.aimud.model.StoreDialogEvent> storeDialogs = Sinks.many().multicast().directBestEffort();

    @Setter
    private CharacterService characterService;

    // Local room memory buffer utilizing infinite timebound ChatMessage payload strings
    public static record ChatMessage(String message, java.time.Instant timestamp) {}
    private final java.util.concurrent.ConcurrentHashMap<Long, java.util.LinkedList<ChatMessage>> roomChatHistory = new java.util.concurrent.ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ConversationService conversationService;

    public Flux<Mobile> getCharacterUpdates() {
        return characterUpdates.asFlux();
    }

    public Flux<TextMessage> getTextMessages() {
        return textMessages.asFlux();
    }

    public Flux<Mobile> getLogoutMessages() {
        return logoutMessages.asFlux();
    }

    public Flux<TargetUpdate> getTargetUpdates() {
        return targetUpdates.asFlux();
    }

    public Flux<PartyUpdate> getPartyUpdates() {
        return partyUpdates.asFlux();
    }

    public Flux<com.aimud.aimud.model.StoreDialogEvent> getStoreDialogs() {
        return storeDialogs.asFlux();
    }

    public void sendCharacterUpdate(Mobile character) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending character update for {}", character.getName());
        characterUpdates.tryEmitNext(character);
    }

    public void sendPartyUpdate(PartyUpdate partyUpdate) {
        log.info("Sending party update for character {}", partyUpdate.getCharacterId());
        partyUpdates.tryEmitNext(partyUpdate);
    }

    public void sendStoreDialog(com.aimud.aimud.model.StoreDialogEvent event) {
        if (event.getCharacterId() == null) return;
        log.info("Sending store dialog for character {} and store {}", event.getCharacterId(), event.getStoreId());
        Sinks.EmitResult result = storeDialogs.tryEmitNext(event);
        log.info("StoreDialog Emit result: {}", result);
    }

    public void sendTargetUpdate(Mobile character, Mobile target) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending target update for {}", character.getName());
        targetUpdates.tryEmitNext(new TargetUpdate(character, target));
    }

    public void sendLogout(Mobile character) {
        if (character.getUserId() == null) {
            return;
        }
        log.info("Sending logout message for {}", character.getName());
        logoutMessages.tryEmitNext(character);
    }

    public void sendTextMessage(String message) {
        log.info("Broadcasting text message: {}", message);
        textMessages.tryEmitNext(new TextMessage(null, message));
    }

    public void sendTextMessage(Mobile character, String message) {
        if (character == null || character.getId() == null || character.getUserId() == null) {
            return;
        }
        log.info("Sending text message to {}: {}", character.getName(), message);
        textMessages.tryEmitNext(new TextMessage(character.getId(), message));
    }

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
                });
    }

    public java.util.List<String> getRoomHistory(Long roomId) {
        java.util.LinkedList<ChatMessage> history = roomChatHistory.get(roomId);
        if (history == null) {
            return java.util.Collections.emptyList();
        }
        // Thread-safe copy while mapping payload strings out of standard temporal wrapper
        return new java.util.ArrayList<>(history).stream().map(ChatMessage::message).collect(java.util.stream.Collectors.toList());
    }

    public java.util.concurrent.ConcurrentHashMap<Long, java.util.LinkedList<ChatMessage>> getRoomChatHistoryMap() {
        return roomChatHistory;
    }
}
