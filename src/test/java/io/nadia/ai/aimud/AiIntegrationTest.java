package io.nadia.ai.aimud;

import io.nadia.ai.aimud.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AiIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private OllamaChatModel chatModel;

    @Autowired
    private AiService aiService;

    @Test
    void aiServiceLoads() {
        assertThat(applicationContext.containsBean("aiService")).isTrue();
        assertThat(aiService).isNotNull();
    }

    @Test
    void aiControllerLoads() {
        assertThat(applicationContext.containsBean("aiController")).isTrue();
    }

    @Test
    void processPromptReturnsResponse() {
        AssistantMessage assistantMessage = new AssistantMessage("Mocked AI response");
        Generation generation = new Generation(assistantMessage);
        ChatResponse chatResponse = new ChatResponse(List.of(generation));
        when(chatModel.stream(any(Prompt.class))).thenReturn(reactor.core.publisher.Flux.just(chatResponse));

        StepVerifier.create(aiService.processPrompt("Hello AI"))
                .expectNext("Mocked AI response")
                .verifyComplete();
    }
}
