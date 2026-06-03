package io.nadia.ai.aimud.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.service.CommunicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameWebSocketHandlerTest {

    @Mock private CommunicationService communicationService;
    @Mock private WebSocketSession session;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private GameWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameWebSocketHandler(communicationService, objectMapper);

        // Default all sinks to empty so tests can override selectively
        lenient().when(communicationService.getCharacterUpdates()).thenReturn(Flux.empty());
        lenient().when(communicationService.getTextMessages()).thenReturn(Flux.empty());
        lenient().when(communicationService.getLogoutMessages()).thenReturn(Flux.empty());
        lenient().when(communicationService.getTargetUpdates()).thenReturn(Flux.empty());
        lenient().when(communicationService.getPartyUpdates()).thenReturn(Flux.empty());
        lenient().when(communicationService.getStoreDialogs()).thenReturn(Flux.empty());
        lenient().when(communicationService.getMapUpdates()).thenReturn(Flux.empty());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Drives handle(), captures every JSON string sent to the session, and
     * returns the parsed JsonNode list so tests can assert on payload structure.
     */
    private List<JsonNode> captureMessages() throws Exception {
        List<String> rawMessages = new ArrayList<>();

        lenient().when(session.textMessage(any(String.class))).thenAnswer(inv -> {
            rawMessages.add(inv.getArgument(0));
            return mock(WebSocketMessage.class);
        });
        when(session.send(any())).thenAnswer(inv -> {
            Flux<?> flux = inv.getArgument(0);
            flux.collectList().block();
            return Mono.empty();
        });

        StepVerifier.create(handler.handle(session))
                .verifyComplete();

        List<JsonNode> nodes = new ArrayList<>();
        for (String raw : rawMessages) {
            nodes.add(objectMapper.readTree(raw));
        }
        return nodes;
    }

    // ── character updates ─────────────────────────────────────────────────────

    @Test
    void characterUpdate_ProducesTypeCharacterMessage() throws Exception {
        Mobile character = new Mobile();
        character.setId(10L);
        character.setName("Hero");

        when(communicationService.getCharacterUpdates()).thenReturn(Flux.just(character));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("character");
        assertThat(msg.get("id").asLong()).isEqualTo(10L);
        assertThat(msg.get("data").get("name").asText()).isEqualTo("Hero");
    }

    @Test
    void characterUpdate_MultipleCharactersAllEmitted() throws Exception {
        Mobile c1 = new Mobile(); c1.setId(1L); c1.setName("Alice");
        Mobile c2 = new Mobile(); c2.setId(2L); c2.setName("Bob");
        when(communicationService.getCharacterUpdates()).thenReturn(Flux.just(c1, c2));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(2);
        assertThat(messages).anyMatch(n -> n.get("id").asLong() == 1L);
        assertThat(messages).anyMatch(n -> n.get("id").asLong() == 2L);
    }

    // ── text messages ─────────────────────────────────────────────────────────

    @Test
    void textMessage_WithCharacterIdProducesIdField() throws Exception {
        TextMessage tm = new TextMessage(42L, "Hello world");
        when(communicationService.getTextMessages()).thenReturn(Flux.just(tm));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("text");
        assertThat(msg.get("id").asLong()).isEqualTo(42L);
        assertThat(msg.get("data").asText()).isEqualTo("Hello world");
    }

    @Test
    void textMessage_NullCharacterIdProducesMinusOne() throws Exception {
        TextMessage tm = new TextMessage(null, "Broadcast");
        when(communicationService.getTextMessages()).thenReturn(Flux.just(tm));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).get("id").asInt()).isEqualTo(-1);
    }

    // ── logout messages ───────────────────────────────────────────────────────

    @Test
    void logoutMessage_ProducesTypeLogout() throws Exception {
        Mobile character = new Mobile();
        character.setId(7L);
        character.setName("Quitter");
        when(communicationService.getLogoutMessages()).thenReturn(Flux.just(character));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("logout");
        assertThat(msg.get("id").asLong()).isEqualTo(7L);
        assertThat(msg.get("data").get("name").asText()).isEqualTo("Quitter");
    }

    // ── target updates ────────────────────────────────────────────────────────

    @Test
    void targetUpdate_ProducesTypeTarget() throws Exception {
        Mobile attacker = new Mobile(); attacker.setId(5L); attacker.setName("Attacker");
        Mobile target = new Mobile();   target.setId(6L);   target.setName("Victim");
        TargetUpdate tu = new TargetUpdate(attacker, target);
        when(communicationService.getTargetUpdates()).thenReturn(Flux.just(tu));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("target");
        assertThat(msg.get("id").asLong()).isEqualTo(5L);
        assertThat(msg.get("data").get("name").asText()).isEqualTo("Victim");
    }

    @Test
    void targetUpdate_NullTargetIsSerialised() throws Exception {
        Mobile attacker = new Mobile(); attacker.setId(5L); attacker.setName("Attacker");
        TargetUpdate tu = new TargetUpdate(attacker, null);
        when(communicationService.getTargetUpdates()).thenReturn(Flux.just(tu));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).get("data").isNull()).isTrue();
    }

    // ── party updates ─────────────────────────────────────────────────────────

    @Test
    void partyUpdate_ProducesTypeParty() throws Exception {
        PartyUpdate pu = new PartyUpdate(99L, 99L, List.of(
                new PartyUpdate.PartyMemberInfo(99L, "Leader", 100, 100, 50, 50)
        ));
        when(communicationService.getPartyUpdates()).thenReturn(Flux.just(pu));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("party");
        assertThat(msg.get("id").asLong()).isEqualTo(99L);
        assertThat(msg.get("data").get("characterId").asLong()).isEqualTo(99L);
    }

    // ── store dialog ──────────────────────────────────────────────────────────

    @Test
    void storeDialog_ProducesTypeStoreDialog() throws Exception {
        StoreDialogEvent event = new StoreDialogEvent(12L, 3L, "Merchant Bob");
        when(communicationService.getStoreDialogs()).thenReturn(Flux.just(event));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(1);
        JsonNode msg = messages.get(0);
        assertThat(msg.get("type").asText()).isEqualTo("storeDialog");
        assertThat(msg.get("id").asLong()).isEqualTo(12L);
        assertThat(msg.get("data").get("shopkeeperName").asText()).isEqualTo("Merchant Bob");
    }

    // ── merged streams ────────────────────────────────────────────────────────

    @Test
    void handle_MergesAllSixStreamTypes() throws Exception {
        Mobile character = new Mobile(); character.setId(1L); character.setName("C");
        Mobile logout = new Mobile();    logout.setId(2L);    logout.setName("L");
        Mobile attacker = new Mobile();  attacker.setId(3L);  attacker.setName("A");
        Mobile victim = new Mobile();    victim.setId(4L);    victim.setName("V");

        when(communicationService.getCharacterUpdates()).thenReturn(Flux.just(character));
        when(communicationService.getTextMessages()).thenReturn(
                Flux.just(new TextMessage(1L, "Hi")));
        when(communicationService.getLogoutMessages()).thenReturn(Flux.just(logout));
        when(communicationService.getTargetUpdates()).thenReturn(
                Flux.just(new TargetUpdate(attacker, victim)));
        when(communicationService.getPartyUpdates()).thenReturn(
                Flux.just(new PartyUpdate(9L, 9L, List.of())));
        when(communicationService.getStoreDialogs()).thenReturn(
                Flux.just(new StoreDialogEvent(5L, 1L, "Shopkeeper")));

        List<JsonNode> messages = captureMessages();

        assertThat(messages).hasSize(6);
        assertThat(messages).extracting(n -> n.get("type").asText())
                .containsExactlyInAnyOrder("character", "text", "logout", "target", "party", "storeDialog");
    }

    @Test
    void handle_EmptyStreamsProducesNoMessages() throws Exception {
        List<JsonNode> messages = captureMessages();
        assertThat(messages).isEmpty();
    }

    // ── payload correctness ───────────────────────────────────────────────────

    @Test
    void textMessage_ContentIsUnwrappedStringNotObject() throws Exception {
        when(communicationService.getTextMessages())
                .thenReturn(Flux.just(new TextMessage(1L, "direct text")));

        List<JsonNode> messages = captureMessages();

        // "data" must be a plain string, not a nested JSON object
        assertThat(messages.get(0).get("data").isTextual()).isTrue();
        assertThat(messages.get(0).get("data").asText()).isEqualTo("direct text");
    }

    @Test
    void characterUpdate_DataContainsExpectedFields() throws Exception {
        Mobile character = new Mobile();
        character.setId(20L);
        character.setName("TestChar");
        character.setCurrentHp(80);
        character.setMaxHp(100);
        when(communicationService.getCharacterUpdates()).thenReturn(Flux.just(character));

        List<JsonNode> messages = captureMessages();

        JsonNode data = messages.get(0).get("data");
        assertThat(data.get("id").asLong()).isEqualTo(20L);
        assertThat(data.get("name").asText()).isEqualTo("TestChar");
        assertThat(data.get("currentHp").asInt()).isEqualTo(80);
        assertThat(data.get("maxHp").asInt()).isEqualTo(100);
    }
}

