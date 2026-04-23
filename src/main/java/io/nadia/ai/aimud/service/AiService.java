package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final ConfigService configService;
    private final List<FunctionCallback> mcpTools;

    public AiService(OllamaChatModel chatModel, ConfigService configService, List<FunctionCallback> mcpTools) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.configService = configService;
        this.mcpTools = mcpTools;
    }

    /**
     * Processes a user prompt using the AI model with default options and tools enabled.
     *
     * @param userPrompt the prompt string provided by the user
     * @return a reactive {@link Flux} emitting the streamed string chunks of the AI's response
     */
    public Flux<String> processPrompt(String userPrompt) {
        return processPrompt(userPrompt, null);
    }

    /**
     * Processes a user prompt using the AI model with custom configuration options and tools enabled.
     * Incorporates system agents into the prompt context.
     *
     * @param userPrompt    the prompt string provided by the user
     * @param customOptions custom {@link OllamaOptions} to configure the model request, or null for default options
     * @return a reactive {@link Flux} emitting the streamed string chunks of the AI's response
     */
    public Flux<String> processPrompt(String userPrompt, OllamaOptions customOptions) {
        log.info("Processing AI prompt with tools: {}", userPrompt);

        return configService.getAllAgents()
                .collectList()
                .flatMapMany(agents -> {
                    OllamaOptions options = customOptions != null ? customOptions : new OllamaOptions();
                    options.setFunctionCallbacks(mcpTools);
                    options.setTruncate(false);

                    StringBuilder systemText = new StringBuilder();
                    for (Agent agent : agents) {
                        systemText.append("Agent: ").append(agent.title()).append("\n").append(agent.content()).append("\n\n");
                    }

                    return chatClient.prompt()
                            .system(systemText.toString())
                            .user(userPrompt)
                            .options(options)
                            .stream()
                            .content()
                            .filter(text -> text != null && !text.isEmpty());
                });
    }

    /**
     * Processes a user prompt using the AI model without making MCP tools available.
     * This is typically used for isolated dialogue interactions where tool execution is undesired.
     *
     * @param userPrompt    the prompt string provided by the user
     * @param customOptions custom {@link OllamaOptions} to configure the model request, or null for default options
     * @return a reactive {@link Flux} emitting the streamed string chunks of the AI's response
     */
    public Flux<String> processPromptNoTools(String userPrompt, OllamaOptions customOptions) {
        log.info("Processing AI prompt WITHOUT tools: {}", userPrompt);

        return configService.getAllAgents()
                .collectList()
                .flatMapMany(agents -> {
                    OllamaOptions options = customOptions != null ? customOptions : new OllamaOptions();
                    options.setTruncate(false);

                    StringBuilder systemText = new StringBuilder();
                    for (Agent agent : agents) {
                        systemText.append("Agent: ").append(agent.title()).append("\n").append(agent.content()).append("\n\n");
                    }

                    return chatClient.prompt()
                            .system(systemText.toString())
                            .user(userPrompt)
                            .options(options)
                            .stream().content()
                            .filter(text -> text != null && !text.isEmpty());
                });
    }
}
