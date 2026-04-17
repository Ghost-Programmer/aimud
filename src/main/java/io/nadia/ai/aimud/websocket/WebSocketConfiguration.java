package io.nadia.ai.aimud.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration and handler bindings for websocket connections via WebSocketConfiguration.
 */
@Configuration
public class WebSocketConfiguration {

    /**
     * Socket processing logic for web socket handler mapping.
     * @return payload of type HandlerMapping
     */
    @Bean
    public HandlerMapping webSocketHandlerMapping(GameWebSocketHandler handler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/game", handler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(1);
        return mapping;
    }

    /**
     * Socket processing logic for handler adapter.
     * @return payload of type WebSocketHandlerAdapter
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
