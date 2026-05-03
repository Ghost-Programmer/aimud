package io.nadia.ai.aimud.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebSocketConfigurationTest {

    private final WebSocketConfiguration config = new WebSocketConfiguration();

    @Test
    void webSocketHandlerMapping_RegistersHandlerAtGamePath() {
        GameWebSocketHandler handler = mock(GameWebSocketHandler.class);

        HandlerMapping mapping = config.webSocketHandlerMapping(handler);

        assertThat(mapping).isInstanceOf(SimpleUrlHandlerMapping.class);
        SimpleUrlHandlerMapping simpleMapping = (SimpleUrlHandlerMapping) mapping;

        @SuppressWarnings("unchecked")
        Map<String, WebSocketHandler> urlMap = (Map<String, WebSocketHandler>) simpleMapping.getUrlMap();

        assertThat(urlMap).containsKey("/ws/game");
        assertThat(urlMap.get("/ws/game")).isSameAs(handler);
    }

    @Test
    void webSocketHandlerMapping_HasHighPriority() {
        GameWebSocketHandler handler = mock(GameWebSocketHandler.class);
    }
}
