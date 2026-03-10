package com.aimud.aimud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final OllamaChatModel chatModel;

    private static final String SYSTEM_PROMPT = 
        "You are an Expert Multi-User Dungeon World Builder. " +
        "You have access to MCP tools for creating rooms, items, and effects for items. " +
        "Use these tools to help the user build their world.";

    public Mono<String> processPrompt(String userPrompt) {
        log.info("Processing AI prompt: {}", userPrompt);
        
        String combinedPrompt = SYSTEM_PROMPT + "\n\nUser request: " + userPrompt;
        Prompt prompt = new Prompt(combinedPrompt);
        
        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .collectList()
                .map(list -> String.join("", list));
    }
}
