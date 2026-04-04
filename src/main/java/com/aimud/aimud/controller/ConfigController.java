package com.aimud.aimud.controller;

import com.aimud.aimud.model.*;
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

    // Agents
    @GetMapping("/agents")
    public Flux<Agent> getAllAgents() {
        return configService.getAllAgents();
    }

    @PostMapping("/agents")
    public Mono<Agent> createAgent(@RequestBody Agent agent) {
        return configService.createAgent(agent);
    }

    @PutMapping("/agents/{id}")
    public Mono<ResponseEntity<Agent>> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        return configService.updateAgent(id, agent)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/agents/{id}")
    public Mono<ResponseEntity<Void>> deleteAgent(@PathVariable Long id) {
        return configService.deleteAgent(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Races
    @GetMapping("/races")
    public Flux<Race> getAllRaces(@RequestParam(defaultValue = "false") boolean playableOnly) {
        return playableOnly ? configService.getPlayableRaces() : configService.getAllRaces();
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
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Character Classes
    @GetMapping("/classes")
    public Flux<CharacterClass> getAllCharacterClasses(@RequestParam(defaultValue = "false") boolean playableOnly) {
        return playableOnly ? configService.getPlayableCharacterClasses() : configService.getAllCharacterClasses();
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
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Skills Registry
    @GetMapping("/skills")
    public Flux<SkillRegistry> getAllSkills() {
        return configService.getAllSkills();
    }
}
