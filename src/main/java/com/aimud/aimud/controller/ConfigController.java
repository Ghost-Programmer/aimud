package com.aimud.aimud.controller;

import com.aimud.aimud.model.CharacterClass;
import com.aimud.aimud.model.Race;
import com.aimud.aimud.model.ServerSettings;
import com.aimud.aimud.service.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    // Server Settings
    @GetMapping("/settings")
    public Mono<ServerSettings> getServerSettings() {
        return configService.getServerSettings();
    }

    @PutMapping("/settings")
    public Mono<ServerSettings> updateServerSettings(@RequestBody ServerSettings settings) {
        return configService.updateServerSettings(settings);
    }

    // Races
    @GetMapping("/races")
    public Flux<Race> getAllRaces() {
        return configService.getAllRaces();
    }

    @PostMapping("/races")
    public Mono<Race> createRace(@RequestBody Race race) {
        return configService.createRace(race);
    }

    @PutMapping("/races/{id}")
    public Mono<ResponseEntity<Race>> updateRace(@PathVariable Long id, @RequestBody Race race) {
        return configService.updateRace(id, race)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/races/{id}")
    public Mono<ResponseEntity<Void>> deleteRace(@PathVariable Long id) {
        return configService.deleteRace(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    // Character Classes
    @GetMapping("/classes")
    public Flux<CharacterClass> getAllCharacterClasses() {
        return configService.getAllCharacterClasses();
    }

    @PostMapping("/classes")
    public Mono<CharacterClass> createCharacterClass(@RequestBody CharacterClass characterClass) {
        return configService.createCharacterClass(characterClass);
    }

    @PutMapping("/classes/{id}")
    public Mono<ResponseEntity<CharacterClass>> updateCharacterClass(@PathVariable Long id, @RequestBody CharacterClass characterClass) {
        return configService.updateCharacterClass(id, characterClass)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/classes/{id}")
    public Mono<ResponseEntity<Void>> deleteCharacterClass(@PathVariable Long id) {
        return configService.deleteCharacterClass(id)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }
}
