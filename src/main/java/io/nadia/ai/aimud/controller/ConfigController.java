package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.model.dto.ItemTypeDetails;
import io.nadia.ai.aimud.service.ConfigService;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
     * @return dynamic reactive {@code Mono<ServerSettings>} response payload
     */
    @GetMapping("/settings")
    public Mono<ServerSettings> getServerSettings() {
        return configService.getServerSettings();
    }

    /**
     * Handles HTTP PUT requests to update server settings.
     * @param settings the updated server settings
     * @return dynamic reactive {@code Mono<ServerSettings>} response payload
     */
    @PutMapping("/settings")
    public Mono<ServerSettings> updateServerSettings(@RequestBody ServerSettings settings) {
        return configService.updateServerSettings(settings);
    }

    // Types
    @GetMapping("/item-types")
    public Mono<List<String>> getItemTypes() {
        return Mono.just(Arrays.stream(ItemType.values())
                .map(ItemType::getLabel)
                .sorted()
                .collect(Collectors.toList()));
    }

    /**
     * Handles HTTP GET requests to retrieve detailed metadata for all item types,
     * including property names.
     * @return dynamic reactive {@code Mono<Map<String, ItemTypeDetails>>} mapping item type labels to their details.
     */
    @GetMapping("/item-types/details")
    public Mono<Map<String, ItemTypeDetails>> getItemTypesDetails() {
        Map<String, ItemTypeDetails> details = Arrays.stream(ItemType.values())
                .collect(Collectors.toMap(
                        ItemType::getLabel,
                        it -> new ItemTypeDetails(
                                it.name(),
                                it.getLabel(),
                                it.getProperty1Name(),
                                it.getProperty2Name(),
                                it.getProperty3Name(),
                                it.getProperty4Name()
                        )
                ));
        return Mono.just(details);
    }

    @GetMapping("/wear-locations")
    public Mono<List<String>> getWearLocations() {
        return Mono.just(Arrays.stream(WearLocation.values())
                .map(WearLocation::getLabel)
                .sorted()
                .collect(Collectors.toList()));
    }

    // Agents
    /**
     * Handles HTTP GET requests to get all agents.
     * @return dynamic reactive {@code Flux<Agent>} response payload
     */
    @GetMapping("/agents")
    public Mono<Map<String, Object>> getAllAgents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return configService.getAllAgents()
                .collectList()
                .map(agents -> {
                    int total = agents.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, total);
                    List<Agent> paged = (fromIndex < total) ? agents.subList(fromIndex, toIndex) : List.of();
                    Map<String, Object> response = new HashMap<>();
                    response.put("agents", paged);
                    response.put("total", total);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    /**
     * Handles HTTP POST requests to create agent.
     * @param agent the agent to create
     * @return dynamic reactive {@code Mono<Agent>} response payload
     */
    @PostMapping("/agents")
    public Mono<Agent> createAgent(@RequestBody Agent agent) {
        return configService.createAgent(agent);
    }

    /**
     * Handles HTTP PUT requests to update agent.
     * @param id the agent id
     * @param agent the updated agent
     * @return dynamic reactive {@code Mono<ResponseEntity<Agent>>} response payload
     */
    @PutMapping("/agents/{id}")
    public Mono<ResponseEntity<Agent>> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        return configService.updateAgent(id, agent)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete agent.
     * @param id the agent id
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @DeleteMapping("/agents/{id}")
    public Mono<ResponseEntity<Void>> deleteAgent(@PathVariable Long id) {
        return configService.deleteAgent(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Races
    /**
     * Handles HTTP GET requests to get all races.
     * @param playableOnly whether to return only playable races
     * @return dynamic reactive {@code Flux<Race>} response payload
     */
    @GetMapping("/races")
    public Mono<Map<String, Object>> getAllRaces(
            @RequestParam(defaultValue = "false") boolean playableOnly,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Flux<Race> source = playableOnly ? configService.getPlayableRaces() : configService.getAllRaces();
        return source.collectList()
                .map(races -> {
                    if (!search.isEmpty()) {
                        String lowerSearch = search.toLowerCase();
                        races = races.stream()
                                .filter(r -> r.getName().toLowerCase().contains(lowerSearch))
                                .collect(Collectors.toList());
                    }
                    races.sort(java.util.Comparator.comparing(r -> r.getName().toLowerCase()));
                    int total = races.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, total);
                    List<Race> paged = (fromIndex < total) ? races.subList(fromIndex, toIndex) : List.of();
                    Map<String, Object> response = new HashMap<>();
                    response.put("races", paged);
                    response.put("total", total);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    /**
     * Handles HTTP POST requests to create race.
     * @param race the race to create
     * @return dynamic reactive {@code Mono<Race>} response payload
     */
    @PostMapping("/races")
    public Mono<Race> createRace(@RequestBody Race race) {
        return configService.createRace(race);
    }

    /**
     * Handles HTTP PUT requests to update race.
     * @param id the race id
     * @param race the updated race
     * @return dynamic reactive {@code Mono<ResponseEntity<Race>>} response payload
     */
    @PutMapping("/races/{id}")
    public Mono<ResponseEntity<Race>> updateRace(@PathVariable Long id, @RequestBody Race race) {
        return configService.updateRace(id, race)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete race.
     * @param id the race id
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @DeleteMapping("/races/{id}")
    public Mono<ResponseEntity<Void>> deleteRace(@PathVariable Long id) {
        return configService.deleteRace(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Character Classes
    /**
     * Handles HTTP GET requests to get all character classes.
     * @param playableOnly whether to return only playable character classes
     * @return dynamic reactive {@code Flux<CharacterClass>} response payload
     */
    @GetMapping("/classes")
    public Mono<Map<String, Object>> getAllCharacterClasses(
            @RequestParam(defaultValue = "false") boolean playableOnly,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Flux<CharacterClass> source = playableOnly ? configService.getPlayableCharacterClasses() : configService.getAllCharacterClasses();
        return source.collectList()
                .map(classes -> {
                    if (!search.isEmpty()) {
                        String lowerSearch = search.toLowerCase();
                        classes = classes.stream()
                                .filter(c -> c.getName().toLowerCase().contains(lowerSearch))
                                .collect(Collectors.toList());
                    }
                    classes.sort(java.util.Comparator.comparing(c -> c.getName().toLowerCase()));
                    int total = classes.size();
                    int fromIndex = page * size;
                    int toIndex = Math.min(fromIndex + size, total);
                    List<CharacterClass> paged = (fromIndex < total) ? classes.subList(fromIndex, toIndex) : List.of();
                    Map<String, Object> response = new HashMap<>();
                    response.put("classes", paged);
                    response.put("total", total);
                    response.put("page", page);
                    response.put("size", size);
                    return response;
                });
    }

    /**
     * Handles HTTP POST requests to create character class.
     * @param characterClass the character class to create
     * @return dynamic reactive {@code Mono<CharacterClass>} response payload
     */
    @PostMapping("/classes")
    public Mono<CharacterClass> createCharacterClass(@RequestBody CharacterClass characterClass) {
        return configService.createCharacterClass(characterClass);
    }

    /**
     * Handles HTTP PUT requests to update character class.
     * @param id the character class id
     * @param characterClass the updated character class
     * @return dynamic reactive {@code Mono<ResponseEntity<CharacterClass>>} response payload
     */
    @PutMapping("/classes/{id}")
    public Mono<ResponseEntity<CharacterClass>> updateCharacterClass(@PathVariable Long id, @RequestBody CharacterClass characterClass) {
        return configService.updateCharacterClass(id, characterClass)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP DELETE requests to delete character class.
     * @param id the character class id
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @DeleteMapping("/classes/{id}")
    public Mono<ResponseEntity<Void>> deleteCharacterClass(@PathVariable Long id) {
        return configService.deleteCharacterClass(id)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    // Skills Registry
    /**
     * Handles HTTP GET requests to get all skills.
     * @return dynamic reactive {@code Flux<SkillRegistry>} response payload
     */
    @GetMapping("/skills")
    public Flux<SkillRegistry> getAllSkills() {
        return configService.getAllSkills();
    }
}
