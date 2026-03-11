package com.aimud.aimud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final OllamaChatModel chatModel;
    private final ConfigService configService;

    public Mono<String> processPrompt(String userPrompt) {
        log.info("Processing AI prompt: {}", userPrompt);

        return configService.getServerSettings()
                .flatMap(settings -> {
                    String systemPrompt = settings.aiSystemPrompt();
                    SystemMessage systemMessage = new SystemMessage(systemPrompt);
                    UserMessage userMessage = new UserMessage(userPrompt);
                    Prompt prompt = new Prompt("User Request: " + userPrompt + "\n" + systemPrompt);

                    return chatModel.stream(prompt)
                            .map(response -> {
                                if (response.getResult() != null && response.getResult().getOutput() != null) {
                                    return response.getResult().getOutput().getText();
                                }
                                return "";
                            })
                            .collectList()
                            .map(list -> String.join("", list));
                });
    }
}
