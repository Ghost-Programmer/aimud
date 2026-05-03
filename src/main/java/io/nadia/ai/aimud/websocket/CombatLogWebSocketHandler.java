package io.nadia.ai.aimud.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import io.nadia.ai.aimud.service.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CombatLogWebSocketHandler implements WebSocketHandler {

    private final CommunicationService communicationService;
    private final ObjectMapper cborMapper;

    public CombatLogWebSocketHandler(CommunicationService communicationService) {
        this.communicationService = communicationService;
        this.cborMapper = new ObjectMapper(new CBORFactory());
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<byte[]> combatLogStream = communicationService.getCombatLogs()
                .flatMap(logMessage -> {
                    try {
                        byte[] bytes = cborMapper.writeValueAsBytes(logMessage);
                        return Mono.just(bytes);
                    } catch (Exception e) {
                        log.error("Error serializing combat log message", e);
                        return Mono.empty();
                    }
                });

        return session.send(
                combatLogStream.map(bytes -> session.binaryMessage(factory -> factory.wrap(bytes)))
        );
    }
}
