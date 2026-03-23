package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.TargetUpdate;
import com.aimud.aimud.model.TextMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@Slf4j
public class CommunicationService {

    private final Sinks.Many<Character> characterUpdates = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<TextMessage> textMessages = Sinks.many().replay().limit(20);
    private final Sinks.Many<Character> logoutMessages = Sinks.many().replay().limit(10);
    private final Sinks.Many<TargetUpdate> targetUpdates = Sinks.many().multicast().onBackpressureBuffer();

    @Setter
    private CharacterService characterService;

    public Flux<Character> getCharacterUpdates() {
        return characterUpdates.asFlux();
    }

    public Flux<TextMessage> getTextMessages() {
        return textMessages.asFlux();
    }

    public Flux<Character> getLogoutMessages() {
        return logoutMessages.asFlux();
    }

    public Flux<TargetUpdate> getTargetUpdates() {
        return targetUpdates.asFlux();
    }

    public void sendCharacterUpdate(Character character) {
        log.info("Sending character update for {}", character.getName());
        characterUpdates.tryEmitNext(character);
    }

    public void sendTargetUpdate(Character character, Mobile target) {
        log.info("Sending target update for {}", character.getName());
        targetUpdates.tryEmitNext(new TargetUpdate(character, target));
    }

    public void sendLogout(Character character) {
        log.info("Sending logout message for {}", character.getName());
        logoutMessages.tryEmitNext(character);
    }

    public void sendTextMessage(String message) {
        log.info("Broadcasting text message: {}", message);
        textMessages.tryEmitNext(new TextMessage(null, message));
    }

    public void sendTextMessage(Character character, String message) {
        if (character == null || character.getId() == null) {
            sendTextMessage(message);
            return;
        }
        log.info("Sending text message to {}: {}", character.getName(), message);
        textMessages.tryEmitNext(new TextMessage(character.getId(), message));
    }

    public void roomMessage(Mobile mobile, String message){
        this.characterService.findAllByRoomId(mobile.getCurrentRoomId()).stream().filter(c -> !c.getId().equals(mobile.getId())).forEach(c -> {
            this.sendTextMessage(c, message);
        });

    }
}
