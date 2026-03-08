package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.CharacterClass;
import com.aimud.aimud.model.Race;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.RaceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StatService {

    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;

    public StatService(RaceRepository raceRepository, CharacterClassRepository characterClassRepository) {
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
    }

    public Mono<Character> updateCurrentStats(Character character) {
        Mono<Race> raceMono = character.getRaceId() != null ? raceRepository.findById(character.getRaceId()) : Mono.empty();
        Mono<CharacterClass> classMono = character.getClassId() != null ? characterClassRepository.findById(character.getClassId()) : Mono.empty();

        return Mono.zip(raceMono.defaultIfEmpty(new Race()), classMono.defaultIfEmpty(new CharacterClass()))
                .map(tuple -> {
                    Race race = tuple.getT1();
                    CharacterClass characterClass = tuple.getT2();

                    character.setCurrentStrength(character.getStrength() + race.getStrengthMod() + characterClass.getStrengthMod());
                    character.setCurrentDexterity(character.getDexterity() + race.getDexterityMod() + characterClass.getDexterityMod());
                    character.setCurrentConstitution(character.getConstitution() + race.getConstitutionMod() + characterClass.getConstitutionMod());
                    character.setCurrentIntelligence(character.getIntelligence() + race.getIntelligenceMod() + characterClass.getIntelligenceMod());
                    character.setCurrentWisdom(character.getWisdom() + race.getWisdomMod() + characterClass.getWisdomMod());
                    character.setCurrentCharisma(character.getCharisma() + race.getCharismaMod() + characterClass.getCharismaMod());

                    return character;
                });
    }
}
