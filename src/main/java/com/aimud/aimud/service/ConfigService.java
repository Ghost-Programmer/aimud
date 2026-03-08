package com.aimud.aimud.service;

import com.aimud.aimud.model.CharacterClass;
import com.aimud.aimud.model.Race;
import com.aimud.aimud.model.ServerSettings;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.RaceRepository;
import com.aimud.aimud.repository.ServerSettingsRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ConfigService {

    private final ServerSettingsRepository serverSettingsRepository;
    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;

    public ConfigService(ServerSettingsRepository serverSettingsRepository, RaceRepository raceRepository, CharacterClassRepository characterClassRepository) {
        this.serverSettingsRepository = serverSettingsRepository;
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
    }

    // Server Settings
    public Mono<ServerSettings> getServerSettings() {
        return serverSettingsRepository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance"));
    }

    public Mono<ServerSettings> updateServerSettings(ServerSettings settings) {
        return serverSettingsRepository.save(settings);
    }

    // Races
    public Flux<Race> getAllRaces() {
        return raceRepository.findAll().filter(race -> !race.isDeleted());
    }

    public Mono<Race> createRace(Race race) {
        return raceRepository.save(race);
    }

    public Mono<Race> updateRace(Long id, Race race) {
        return raceRepository.findById(id)
                .flatMap(existingRace -> {
                    existingRace.setName(race.getName());
                    existingRace.setDescription(race.getDescription());
                    existingRace.setStrengthMod(race.getStrengthMod());
                    existingRace.setDexterityMod(race.getDexterityMod());
                    existingRace.setConstitutionMod(race.getConstitutionMod());
                    existingRace.setIntelligenceMod(race.getIntelligenceMod());
                    existingRace.setWisdomMod(race.getWisdomMod());
                    existingRace.setCharismaMod(race.getCharismaMod());
                    return raceRepository.save(existingRace);
                });
    }

    public Mono<Void> deleteRace(Long id) {
        return raceRepository.findById(id)
                .flatMap(race -> {
                    race.setDeleted(true);
                    return raceRepository.save(race);
                })
                .then();
    }

    // Character Classes
    public Flux<CharacterClass> getAllCharacterClasses() {
        return characterClassRepository.findAll().filter(cc -> !cc.isDeleted());
    }

    public Mono<CharacterClass> createCharacterClass(CharacterClass characterClass) {
        return characterClassRepository.save(characterClass);
    }

    public Mono<CharacterClass> updateCharacterClass(Long id, CharacterClass characterClass) {
        return characterClassRepository.findById(id)
                .flatMap(existingClass -> {
                    existingClass.setName(characterClass.getName());
                    existingClass.setDescription(characterClass.getDescription());
                    existingClass.setStrengthMod(characterClass.getStrengthMod());
                    existingClass.setDexterityMod(characterClass.getDexterityMod());
                    existingClass.setConstitutionMod(characterClass.getConstitutionMod());
                    existingClass.setIntelligenceMod(characterClass.getIntelligenceMod());
                    existingClass.setWisdomMod(characterClass.getWisdomMod());
                    existingClass.setCharismaMod(characterClass.getCharismaMod());
                    return characterClassRepository.save(existingClass);
                });
    }

    public Mono<Void> deleteCharacterClass(Long id) {
        return characterClassRepository.findById(id)
                .flatMap(cc -> {
                    cc.setDeleted(true);
                    return characterClassRepository.save(cc);
                })
                .then();
    }
}
