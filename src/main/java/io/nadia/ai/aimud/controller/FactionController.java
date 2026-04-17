package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Faction;
import io.nadia.ai.aimud.service.FactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST Controller exposing HTTP API endpoints for Faction manipulation.
 */
@RestController
@RequestMapping("/api/factions")
@RequiredArgsConstructor
public class FactionController {

    private final FactionService factionService;

    /**
     * Handles HTTP GET requests to get all factions.
     * @return dynamic reactive Flux<Faction> response payload
     */
    @GetMapping
    public Flux<Faction> getAllFactions() {
        return factionService.findAllFactions();
    }

    @GetMapping("/mobile/{mobileId}")
    public Mono<Map<Long, Integer>> getMobileRatings(@PathVariable Long mobileId) {
        return factionService.getMobileRatings(mobileId);
    }

    /**
     * Handles HTTP POST requests to update mobile ratings.
     * @param mobileId bound request payload or parameter
     * @param Map<Long bound request payload or parameter
     * @param ratings bound request payload or parameter
     * @return dynamic reactive Mono<Void> response payload
     */
    @PostMapping("/mobile/{mobileId}")
    public Mono<Void> updateMobileRatings(@PathVariable Long mobileId, @RequestBody Map<Long, Integer> ratings) {
        return factionService.updateMobileRatings(mobileId, ratings);
    }
}
