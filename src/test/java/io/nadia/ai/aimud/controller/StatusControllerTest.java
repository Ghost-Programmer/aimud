package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.service.StatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusService statusService;

    @Test
    void getStatus_ReturnsServicePayload() {
        StatusController controller = new StatusController(statusService);
        Map<String, Object> payload = Map.of("status", "ONLINE");
        when(statusService.getSystemStatus()).thenReturn(Mono.just(payload));

        StepVerifier.create(controller.getStatus())
                .expectNext(payload)
                .verifyComplete();

        verify(statusService).getSystemStatus();
    }
}

