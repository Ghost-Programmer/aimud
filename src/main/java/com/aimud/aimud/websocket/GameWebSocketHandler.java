package com.aimud.aimud.websocket;

import com.aimud.aimud.service.CommunicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketHandler implements WebSocketHandler {

    private final CommunicationService communicationService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<String> characterUpdates = communicationService.getCharacterUpdates()
                .flatMap(character -> {
                    try {
                        String json = objectMapper.writeValueAsString(Map.of(
                                "type", "character",
                                "id", character.getId(),
                                "data", character
                        ));
                        return Mono.just(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing character update", e);
                        return Mono.empty();
                    }
                });

        Flux<String> textMessages = communicationService.getTextMessages()
                .flatMap(message -> {
                    try {
                        String json = objectMapper.writeValueAsString(Map.of(
                                "type", "text",
                                "id", message.getCharacterId() != null ? message.getCharacterId() : -1,
                                "data", message.getContent()
                        ));
                        return Mono.just(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing text message", e);
                        return Mono.empty();
                    }
                });

        Flux<String> logoutMessages = communicationService.getLogoutMessages()
                .flatMap(character -> {
                    log.info("Preparing logout message for character: {}", character.getName());
                    try {
                        String json = objectMapper.writeValueAsString(Map.of(
                                "type", "logout",
                                "id", character.getId(),
                                "data", character
                        ));
                        return Mono.just(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing logout message", e);
                        return Mono.empty();
                    }
                });

        Flux<String> targetUpdates = communicationService.getTargetUpdates()
                .flatMap(targetUpdate -> {
                    log.info("Preparing target update message for character: {}", targetUpdate.getCharacter().getName());
                    try {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", "target");
                        map.put("id", targetUpdate.getCharacter().getId());
                        map.put("data", targetUpdate.getTarget());

                        String json = objectMapper.writeValueAsString(map);
                        return Mono.just(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing target update message", e);
                        return Mono.empty();
                    }
                });

        Flux<String> partyUpdates = communicationService.getPartyUpdates()
                .flatMap(partyUpdate -> {
                    try {
                        String json = objectMapper.writeValueAsString(Map.of(
                                "type", "party",
                                "id", partyUpdate.getCharacterId(),
                                "data", partyUpdate
                        ));
                        return Mono.just(json);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing party update", e);
                        return Mono.empty();
                    }
                });

        return session.send(
                Flux.merge(characterUpdates, textMessages, logoutMessages, targetUpdates, partyUpdates)
                        .map(session::textMessage)
        );
    }
}
