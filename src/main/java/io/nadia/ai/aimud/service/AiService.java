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

    public Flux<String> processPrompt(String userPrompt) {
        return processPrompt(userPrompt, null);
    }

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
