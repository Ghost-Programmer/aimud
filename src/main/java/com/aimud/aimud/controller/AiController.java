package com.aimud.aimud.controller;

import com.aimud.aimud.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    @PostMapping("/prompt")
    public Mono<Map<String, String>> processPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        log.info("REST Request: Process AI prompt: {}", prompt);
        return aiService.processPrompt(prompt)
                .map(response -> Map.of("response", response));
    }
}
