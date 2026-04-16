package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Faction;
import io.nadia.ai.aimud.service.FactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/factions")
@RequiredArgsConstructor
public class FactionController {

    private final FactionService factionService;

    @GetMapping
    public Flux<Faction> getAllFactions() {
        return factionService.findAllFactions();
    }

    @GetMapping("/mobile/{mobileId}")
    public Mono<Map<Long, Integer>> getMobileRatings(@PathVariable Long mobileId) {
        return factionService.getMobileRatings(mobileId);
    }

    @PostMapping("/mobile/{mobileId}")
    public Mono<Void> updateMobileRatings(@PathVariable Long mobileId, @RequestBody Map<Long, Integer> ratings) {
        return factionService.updateMobileRatings(mobileId, ratings);
    }
}
