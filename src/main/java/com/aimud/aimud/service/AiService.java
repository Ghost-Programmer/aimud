package com.aimud.aimud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final OllamaChatModel chatModel;
    private final ConfigService configService;
    private final List<FunctionCallback> mcpTools;

    public Mono<String> processPrompt(String userPrompt) {
        log.info("Processing AI prompt: {}", userPrompt);

        return configService.getServerSettings()
                .flatMap(settings -> {
                    String systemPrompt = settings.aiSystemPrompt();
                    SystemMessage systemMessage = new SystemMessage(systemPrompt);
                    UserMessage userMessage = new UserMessage(userPrompt);

                    OllamaOptions options = new OllamaOptions();
                    options.setFunctionCallbacks(mcpTools);
                    options.setTruncate(false);

                    Prompt prompt = new Prompt(List.of(systemMessage, userMessage), options);

                    return Mono.fromCallable(() -> {
                        var response = chatModel.call(prompt);
                        if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                            return response.getResult().getOutput().getText();
                        }
                        return "";
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }
}
