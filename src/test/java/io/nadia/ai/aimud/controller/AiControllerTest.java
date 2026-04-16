package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.service.AiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private AiService aiService;

    @Test
    void processPrompt_DelegatesToService() {
        AiController controller = new AiController(aiService);
        when(aiService.processPrompt("hello")).thenReturn(Flux.just("a", "b"));

        StepVerifier.create(controller.processPrompt(Map.of("prompt", "hello")))
                .expectNext("a", "b")
                .verifyComplete();

        verify(aiService).processPrompt("hello");
    }
}

