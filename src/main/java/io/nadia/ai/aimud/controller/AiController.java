package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * REST Controller exposing HTTP API endpoints for Ai manipulation.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    /**
     * Handles HTTP POST requests to process prompt.
     * @param Map<String bound request payload or parameter
     * @param request bound request payload or parameter
     * @return dynamic reactive Flux<String> response payload
     */
    @PostMapping(value = "/prompt", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> processPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        log.info("REST Request: Process AI prompt: {}", prompt);
        return aiService.processPrompt(prompt);
    }
}
