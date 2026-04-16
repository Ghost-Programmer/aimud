package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class AiService {

    private final OllamaChatModel chatModel;
    private final ConfigService configService;
    private final List<FunctionCallback> mcpTools;

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
                    UserMessage userMessage = new UserMessage(userPrompt);

                    OllamaOptions options = customOptions != null ? customOptions : new OllamaOptions();
                    options.setFunctionCallbacks(mcpTools);
                    options.setTruncate(false);

                    List<Message> messages = new ArrayList<>(agents.size() + 1);
                    for (Agent agent : agents) {
                        messages.add(new SystemMessage("Agent: " + agent.title() + "\n" + agent.content()));
                    }
                    messages.add(userMessage);

                    Prompt prompt = new Prompt(messages, options);

                    return chatModel.stream(prompt)
                            .map(response -> {
                                if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                                    return response.getResult().getOutput().getText();
                                }
                                return "";
                            })
                            .filter(text -> !text.isEmpty());
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
                    UserMessage userMessage = new UserMessage(userPrompt);

                    // Explicitly ignore MCP tools for isolated dialogue interactions
                    OllamaOptions options = customOptions != null ? customOptions : new OllamaOptions();
                    options.setTruncate(false);

                    List<Message> messages = new ArrayList<>(agents.size() + 1);
                    for (Agent agent : agents) {
                        messages.add(new SystemMessage("Agent: " + agent.title() + "\n" + agent.content()));
                    }
                    messages.add(userMessage);

                    Prompt prompt = new Prompt(messages, options);

                    return chatModel.stream(prompt)
                            .map(response -> {
                                if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                                    return response.getResult().getOutput().getText();
                                }
                                return "";
                            })
                            .filter(text -> !text.isEmpty());
                });
    }
}
