package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Faction;
import io.nadia.ai.aimud.service.FactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
     * Handles HTTP GET requests to get all factions with pagination.
     */
    @GetMapping
    public Mono<Map<String, Object>> getAllFactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return factionService.findAllFactions()
                .collectList()
                .map(factions -> {
                    factions.sort(Comparator.comparing(f -> f.getName().toLowerCase()));
                    int total = factions.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, total);
                    List<Faction> paged = (fromIndex < total) ? factions.subList(fromIndex, toIndex) : List.of();
                    Map<String, Object> response = new HashMap<>();
                    response.put("factions", paged);
                    response.put("total", total);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    /**
     * Handles HTTP GET requests to get mobile faction ratings.
     * @param mobileId the mobile id
     * @return dynamic reactive {@code Mono<Map<Long, Integer>>} response payload
     */
    @GetMapping("/mobile/{mobileId}")
    public Mono<Map<Long, Integer>> getMobileRatings(@PathVariable Long mobileId) {
        return factionService.getMobileRatings(mobileId);
    }

    /**
     * Handles HTTP POST requests to update mobile ratings.
     * @param mobileId the mobile id
     * @param ratings the map of faction ratings
     * @return dynamic reactive {@code Mono<Void>} response payload
     */
    @PostMapping("/mobile/{mobileId}")
    public Mono<Void> updateMobileRatings(@PathVariable Long mobileId, @RequestBody Map<Long, Integer> ratings) {
        return factionService.updateMobileRatings(mobileId, ratings);
    }

    /**
     * Handles HTTP POST requests to create faction.
     * @param faction the faction to create
     * @return dynamic reactive {@code Mono<Faction>} response payload
     */
    @PostMapping
    public Mono<Faction> createFaction(@RequestBody Faction faction) {
        return factionService.createFaction(faction);
    }

    /**
     * Handles HTTP PUT requests to update faction.
     * @param id the faction id
     * @param faction the updated faction
     * @return dynamic reactive {@code Mono<ResponseEntity<Faction>>} response payload
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Faction>> updateFaction(@PathVariable Long id, @RequestBody Faction faction) {
        return factionService.updateFaction(id, faction)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete faction.
     * @param id the faction id
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteFaction(@PathVariable Long id) {
        return factionService.deleteFaction(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
