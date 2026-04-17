package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.service.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller exposing HTTP API endpoints for Config manipulation.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    // Server Settings
    /**
     * Handles HTTP GET requests to get server settings.
     * @return dynamic reactive Mono<ServerSettings> response payload
     */
    @GetMapping("/settings")
    public Mono<ServerSettings> getServerSettings() {
        return configService.getServerSettings();
    }

    /**
     * Handles HTTP PUT requests to update server settings.
     * @param settings bound request payload or parameter
     * @return dynamic reactive Mono<ServerSettings> response payload
     */
    @PutMapping("/settings")
    public Mono<ServerSettings> updateServerSettings(@RequestBody ServerSettings settings) {
        return configService.updateServerSettings(settings);
    }

    // Agents
    /**
     * Handles HTTP GET requests to get all agents.
     * @return dynamic reactive Flux<Agent> response payload
     */
    @GetMapping("/agents")
    public Flux<Agent> getAllAgents() {
        return configService.getAllAgents();
    }

    /**
     * Handles HTTP POST requests to create agent.
     * @param agent bound request payload or parameter
     * @return dynamic reactive Mono<Agent> response payload
     */
    @PostMapping("/agents")
    public Mono<Agent> createAgent(@RequestBody Agent agent) {
        return configService.createAgent(agent);
    }

    /**
     * Handles HTTP PUT requests to update agent.
     * @param id bound request payload or parameter
     * @param agent bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Agent>> response payload
     */
    @PutMapping("/agents/{id}")
    public Mono<ResponseEntity<Agent>> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        return configService.updateAgent(id, agent)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete agent.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Void>> response payload
     */
    @DeleteMapping("/agents/{id}")
    public Mono<ResponseEntity<Void>> deleteAgent(@PathVariable Long id) {
        return configService.deleteAgent(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Races
    /**
     * Handles HTTP GET requests to get all races.
     * @param playableOnly bound request payload or parameter
     * @return dynamic reactive Flux<Race> response payload
     */
    @GetMapping("/races")
    public Flux<Race> getAllRaces(@RequestParam(defaultValue = "false") boolean playableOnly) {
        return playableOnly ? configService.getPlayableRaces() : configService.getAllRaces();
    }

    /**
     * Handles HTTP POST requests to create race.
     * @param race bound request payload or parameter
     * @return dynamic reactive Mono<Race> response payload
     */
    @PostMapping("/races")
    public Mono<Race> createRace(@RequestBody Race race) {
        return configService.createRace(race);
    }

    /**
     * Handles HTTP PUT requests to update race.
     * @param id bound request payload or parameter
     * @param race bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Race>> response payload
     */
    @PutMapping("/races/{id}")
    public Mono<ResponseEntity<Race>> updateRace(@PathVariable Long id, @RequestBody Race race) {
        return configService.updateRace(id, race)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete race.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Void>> response payload
     */
    @DeleteMapping("/races/{id}")
    public Mono<ResponseEntity<Void>> deleteRace(@PathVariable Long id) {
        return configService.deleteRace(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Character Classes
    /**
     * Handles HTTP GET requests to get all character classes.
     * @param playableOnly bound request payload or parameter
     * @return dynamic reactive Flux<CharacterClass> response payload
     */
    @GetMapping("/classes")
    public Flux<CharacterClass> getAllCharacterClasses(@RequestParam(defaultValue = "false") boolean playableOnly) {
        return playableOnly ? configService.getPlayableCharacterClasses() : configService.getAllCharacterClasses();
    }

    /**
     * Handles HTTP POST requests to create character class.
     * @param characterClass bound request payload or parameter
     * @return dynamic reactive Mono<CharacterClass> response payload
     */
    @PostMapping("/classes")
    public Mono<CharacterClass> createCharacterClass(@RequestBody CharacterClass characterClass) {
        return configService.createCharacterClass(characterClass);
    }

    /**
     * Handles HTTP PUT requests to update character class.
     * @param id bound request payload or parameter
     * @param characterClass bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<CharacterClass>> response payload
     */
    @PutMapping("/classes/{id}")
    public Mono<ResponseEntity<CharacterClass>> updateCharacterClass(@PathVariable Long id, @RequestBody CharacterClass characterClass) {
        return configService.updateCharacterClass(id, characterClass)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete character class.
     * @param id bound request payload or parameter
     * @return dynamic reactive Mono<ResponseEntity<Void>> response payload
     */
    @DeleteMapping("/classes/{id}")
    public Mono<ResponseEntity<Void>> deleteCharacterClass(@PathVariable Long id) {
        return configService.deleteCharacterClass(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Skills Registry
    /**
     * Handles HTTP GET requests to get all skills.
     * @return dynamic reactive Flux<SkillRegistry> response payload
     */
    @GetMapping("/skills")
    public Flux<SkillRegistry> getAllSkills() {
        return configService.getAllSkills();
    }
}
