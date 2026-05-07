package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final ConfigService configService;
    private final List<ToolCallback> mcpTools;

    public AiService(OllamaChatModel chatModel, ConfigService configService, List<ToolCallback> mcpTools) {
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
     * @param customOptions custom {@link OllamaChatOptions} to configure the model request, or null for default options
     * @return a reactive {@link Flux} emitting the streamed string chunks of the AI's response
     */
    public Flux<String> processPrompt(String userPrompt, OllamaChatOptions customOptions) {
        log.info("Processing AI prompt with tools: {}", userPrompt);

        return configService.getAllAgents()
                .filter(agent -> "Admin AI".equals(agent.title()))
                .collectList()
                .flatMapMany(agents -> {
                    StringBuilder systemText = new StringBuilder();
                    for (Agent agent : agents) {
                        systemText.append("Agent: ").append(agent.title()).append("\n").append(agent.content()).append("\n\n");
                    }

                    return chatClient.mutate()
                            .defaultOptions(customOptions != null ? customOptions : OllamaChatOptions.builder().build())
                            .defaultTools(mcpTools.toArray(new ToolCallback[0]))
                            .build()
                            .prompt()
                            .system(systemText.toString())
                            .user(userPrompt)
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
     * @param customOptions custom {@link OllamaChatOptions} to configure the model request, or null for default options
     * @return a reactive {@link Flux} emitting the streamed string chunks of the AI's response
     */
    public Flux<String> processPromptNoTools(String userPrompt, OllamaChatOptions customOptions) {
        log.info("Processing AI prompt WITHOUT tools: {}", userPrompt);

        return configService.getAllAgents()
                .filter(agent -> "Admin AI".equals(agent.title()))
                .collectList()
                .flatMapMany(agents -> {
                    StringBuilder systemText = new StringBuilder();
                    for (Agent agent : agents) {
                        systemText.append("Agent: ").append(agent.title()).append("\n").append(agent.content()).append("\n\n");
                    }

                    return chatClient.mutate()
                            .defaultOptions(customOptions != null ? customOptions : OllamaChatOptions.builder().build())
                            .build()
                            .prompt()
                            .system(systemText.toString())
                            .user(userPrompt)
                            .stream().content()
                            .filter(text -> text != null && !text.isEmpty());
                });
    }
}
