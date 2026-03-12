package com.aimud.aimud.websocket;

import com.aimud.aimud.service.TickService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketHandler implements WebSocketHandler {

    private final TickService tickService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                tickService.getCharacterUpdates()
                        .flatMap(character -> {
                            try {
                                String json = objectMapper.writeValueAsString(character);
                                return Mono.just(session.textMessage(json));
                            } catch (JsonProcessingException e) {
                                log.error("Error serializing character update", e);
                                return Mono.empty();
                            }
                        })
        );
    }
}
