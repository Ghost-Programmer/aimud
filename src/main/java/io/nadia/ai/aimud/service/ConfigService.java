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

    public ConfigService(ServerSettingsRepository serverSettingsRepository, AgentRepository agentRepository, RaceRepository raceRepository, CharacterClassRepository characterClassRepository, SkillRegistryRepository skillRegistryRepository) {
        this.serverSettingsRepository = serverSettingsRepository;
        this.agentRepository = agentRepository;
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
        this.skillRegistryRepository = skillRegistryRepository;
    }

    // Server Settings
    @Cacheable(value = "serverSettings")
    public Mono<ServerSettings> getServerSettings() {
        return serverSettingsRepository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", null, null, null, null))
                .cache();
    }

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
                            null, null, null, null
                    );
                    return serverSettingsRepository.save(newSettings);
                }));
    }

    // Agents
    @Cacheable(value = "agents")
    public Flux<Agent> getAllAgents() {
        return agentRepository.findAllByOrderByIdAsc().cache();
    }

    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Agent> createAgent(Agent agent) {
        return agentRepository.save(new Agent(agent.id(), agent.title(), agent.content()));
    }

    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Agent> updateAgent(Long id, Agent agent) {
        return agentRepository.findById(id)
                .flatMap(existingAgent -> agentRepository.save(new Agent(
                        id,
                        agent.title(),
                        agent.content()
                )));
    }

    @CacheEvict(value = {"agents", "agent"}, allEntries = true)
    public Mono<Void> deleteAgent(Long id) {
        return agentRepository.deleteById(id);
    }

    // Races
    @Cacheable(value = "races")
    public Flux<Race> getAllRaces() {
        return raceRepository.findAll().filter(race -> !race.isDeleted()).cache();
    }

    public Flux<Race> getPlayableRaces() {
        return raceRepository.findAll()
                .filter(race -> !race.isDeleted() && !race.isNpcOnly());
    }

    @CacheEvict(value = "races", allEntries = true)
    public Mono<Race> createRace(Race race) {
        return raceRepository.save(race);
    }

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
    @Cacheable(value = "characterClasses")
    public Flux<CharacterClass> getAllCharacterClasses() {
        return characterClassRepository.findAll().filter(cc -> !cc.isDeleted()).cache();
    }

    public Flux<CharacterClass> getPlayableCharacterClasses() {
        return characterClassRepository.findAll()
                .filter(cc -> !cc.isDeleted() && !cc.isNpcOnly());
    }

    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<CharacterClass> createCharacterClass(CharacterClass characterClass) {
        return characterClassRepository.save(characterClass);
    }

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
    @Cacheable(value = "skillsRegistry")
    public Flux<SkillRegistry> getAllSkills() {
        return skillRegistryRepository.findAll().cache();
    }
}
