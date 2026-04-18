package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ConfigService {

    private final ServerSettingsRepository serverSettingsRepository;
    private final AgentRepository agentRepository;
    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;
    private final SkillRegistryRepository skillRegistryRepository;

    /**
     * Constructs a new ConfigService.
     *
     * @param serverSettingsRepository the repository for server settings
     * @param agentRepository          the repository for AI agents
     * @param raceRepository           the repository for races
     * @param characterClassRepository the repository for character classes
     * @param skillRegistryRepository  the repository for the skills registry
     */
    public ConfigService(ServerSettingsRepository serverSettingsRepository, AgentRepository agentRepository, RaceRepository raceRepository, CharacterClassRepository characterClassRepository, SkillRegistryRepository skillRegistryRepository) {
        this.serverSettingsRepository = serverSettingsRepository;
        this.agentRepository = agentRepository;
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.skillRegistryRepository = skillRegistryRepository;
    }

    // Server Settings
    /**
     * Retrieves the current server settings from the cache or database.
     * If no settings are found, default settings are returned.
     *
     * @return a {@link Mono} containing the server settings
     */
    @Cacheable(value = "serverSettings")
    public Mono<ServerSettings> getServerSettings() {
        return serverSettingsRepository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", 0, 1, 1, 1, null, null, null, null))
                .cache();
    }

    /**
     * Updates and saves the server settings, effectively clearing the cache.
     *
     * @param settings the new server settings to apply
     * @return a {@link Mono} containing the updated server settings
     */
    @CacheEvict(value = "serverSettings", allEntries = true)
    public Mono<ServerSettings> updateServerSettings(ServerSettings settings) {
        return serverSettingsRepository.findById(1L)
                .flatMap(existingSettings -> {
                    ServerSettings settingsToSave = new ServerSettings(
                            1L,
                            settings.serverName(),
                            settings.allowNewUser(),
                            settings.maintenance(),
                            settings.maintenanceText(),
                            settings.mudHour(),
                            settings.mudDay(),
                            settings.mudMonth(),
                            settings.mudYear(),
                            existingSettings.createdAt(),
                            existingSettings.modifiedAt(),
                            existingSettings.createdBy(),
                            existingSettings.modifiedBy()
                    );
                    return serverSettingsRepository.save(settingsToSave);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    ServerSettings newSettings = new ServerSettings(
                            1L,
                            settings.serverName(),
                            settings.allowNewUser(),
                            settings.maintenance(),
                            settings.maintenanceText(),
                            settings.mudHour(),
                            settings.mudDay(),
                            settings.mudMonth(),
                            settings.mudYear(),
                            null, null, null, null
                    );
                    return serverSettingsRepository.save(newSettings);
                }));
    }

    // Agents
    /**
     * Retrieves all configured AI agents, cached.
     *
     * @return a {@link Flux} emitting all available agents
     */
    @Cacheable(value = "agents")
    public Flux<Agent> getAllAgents() {
        return agentRepository.findAllByOrderByIdAsc().cache();
    }

    /**
     * Creates a new AI agent and clears the agents cache.
     *
     * @param agent the agent to create
     * @return a {@link Mono} containing the created agent
     */
    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Agent> createAgent(Agent agent) {
        return agentRepository.save(new Agent(agent.id(), agent.title(), agent.content()));
    }

    /**
     * Updates an existing AI agent and clears the agents cache.
     *
     * @param id    the ID of the agent to update
     * @param agent the updated agent data
     * @return a {@link Mono} containing the updated agent
     */
    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Agent> updateAgent(Long id, Agent agent) {
        return agentRepository.findById(id)
                .flatMap(existingAgent -> agentRepository.save(new Agent(
                        id,
                        agent.title(),
                        agent.content()
                )));
    }

    /**
     * Deletes an AI agent by its ID and clears the agents cache.
     *
     * @param id the ID of the agent to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Void> deleteAgent(Long id) {
        return agentRepository.deleteById(id);
    }

    // Races
    /**
     * Retrieves all races that are not marked as deleted, cached.
     *
     * @return a {@link Flux} emitting all active races
     */
    @Cacheable(value = "races")
    public Flux<Race> getAllRaces() {
        return raceRepository.findAll().filter(race -> !race.isDeleted()).cache();
    }

    /**
     * Retrieves all races that are playable (not deleted and not restricted to NPCs).
     *
     * @return a {@link Flux} emitting playable races
     */
    public Flux<Race> getPlayableRaces() {
        return raceRepository.findAll()
                .filter(race -> !race.isDeleted() && !race.isNpcOnly());
    }

    /**
     * Creates a new race and clears the races cache.
     *
     * @param race the race to create
     * @return a {@link Mono} containing the created race
     */
    @CacheEvict(value = "races", allEntries = true)
    public Mono<Race> createRace(Race race) {
        return raceRepository.save(race);
    }

    /**
     * Updates an existing race and clears the races cache.
     *
     * @param id   the ID of the race to update
     * @param race the updated race data
     * @return a {@link Mono} containing the updated race
     */
    @CacheEvict(value = "races", allEntries = true)
    public Mono<Race> updateRace(Long id, Race race) {
        return raceRepository.findById(id)
                .flatMap(existingRace -> {
                    existingRace.setName(race.getName());
                    existingRace.setDescription(race.getDescription());
                    existingRace.setNpcOnly(race.isNpcOnly());
                    existingRace.setStrengthMod(race.getStrengthMod());
                    existingRace.setDexterityMod(race.getDexterityMod());
                    existingRace.setConstitutionMod(race.getConstitutionMod());
                    existingRace.setIntelligenceMod(race.getIntelligenceMod());
                    existingRace.setWisdomMod(race.getWisdomMod());
                    existingRace.setCharismaMod(race.getCharismaMod());
                    return raceRepository.save(existingRace);
                });
    }

    /**
     * Soft-deletes a race by its ID by setting the deleted flag.
     *
     * @param id the ID of the race to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = "races", allEntries = true)
    public Mono<Void> deleteRace(Long id) {
        return raceRepository.findById(id)
                .flatMap(race -> {
                    race.setDeleted(true);
                    return raceRepository.save(race);
                })
                .then();
    }

    // Character Classes
    /**
     * Retrieves all character classes that are not marked as deleted, cached.
     *
     * @return a {@link Flux} emitting all active character classes
     */
    @Cacheable(value = "characterClasses")
    public Flux<CharacterClass> getAllCharacterClasses() {
        return characterClassRepository.findAll().filter(cc -> !cc.isDeleted()).cache();
    }

    /**
     * Retrieves all character classes that are playable (not deleted and not restricted to NPCs).
     *
     * @return a {@link Flux} emitting playable character classes
     */
    public Flux<CharacterClass> getPlayableCharacterClasses() {
        return characterClassRepository.findAll()
                .filter(cc -> !cc.isDeleted() && !cc.isNpcOnly());
    }

    /**
     * Creates a new character class and clears the related cache.
     *
     * @param characterClass the character class to create
     * @return a {@link Mono} containing the created character class
     */
    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<CharacterClass> createCharacterClass(CharacterClass characterClass) {
        return characterClassRepository.save(characterClass);
    }

    /**
     * Updates an existing character class and clears the related cache.
     *
     * @param id             the ID of the character class to update
     * @param characterClass the updated character class data
     * @return a {@link Mono} containing the updated character class
     */
    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<CharacterClass> updateCharacterClass(Long id, CharacterClass characterClass) {
        return characterClassRepository.findById(id)
                .flatMap(existingClass -> {
                    existingClass.setName(characterClass.getName());
                    existingClass.setDescription(characterClass.getDescription());
                    existingClass.setNpcOnly(characterClass.isNpcOnly());
                    existingClass.setStrengthMod(characterClass.getStrengthMod());
                    existingClass.setDexterityMod(characterClass.getDexterityMod());
                    existingClass.setConstitutionMod(characterClass.getConstitutionMod());
                    existingClass.setIntelligenceMod(characterClass.getIntelligenceMod());
                    existingClass.setWisdomMod(characterClass.getWisdomMod());
                    existingClass.setCharismaMod(characterClass.getCharismaMod());
                    existingClass.setStartingItems(characterClass.getStartingItems());
                    existingClass.setStartingSkills(characterClass.getStartingSkills());
                    return characterClassRepository.save(existingClass);
                });
    }

    /**
     * Soft-deletes a character class by its ID by setting the deleted flag.
     *
     * @param id the ID of the character class to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<Void> deleteCharacterClass(Long id) {
        return characterClassRepository.findById(id)
                .flatMap(cc -> {
                    cc.setDeleted(true);
                    return characterClassRepository.save(cc);
                })
                .then();
    }

    // Skills
    /**
     * Retrieves all registered skills, cached.
     *
     * @return a {@link Flux} emitting all skills from the registry
     */
    @Cacheable(value = "skillsRegistry")
    public Flux<SkillRegistry> getAllSkills() {
        return skillRegistryRepository.findAll().cache();
    }
}
