package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.repository.CharacterRepository;
import com.aimud.aimud.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final StatService statService;
    private final Random random = new Random();

    public CharacterService(CharacterRepository characterRepository, UserRepository userRepository, StatService statService) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.statService = statService;
    }

    public Mono<Character> createCharacter(String username, Character character) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    character.setUserId(user.getId());
                    return characterRepository.save(character);
                })
                .flatMap(statService::updateCurrentStats);
    }

    public Flux<Character> getCharactersByUser(String username) {
        return userRepository.findByUsername(username)
                .flatMapMany(user -> characterRepository.findByUserId(user.getId()))
                .flatMap(statService::updateCurrentStats);
    }

    public Mono<Character> updateCharacter(Long id, Character character) {
        return characterRepository.findById(id)
                .flatMap(existingCharacter -> {
                    existingCharacter.setName(character.getName());
                    existingCharacter.setStrength(character.getStrength());
                    existingCharacter.setDexterity(character.getDexterity());
                    existingCharacter.setConstitution(character.getConstitution());
                    existingCharacter.setIntelligence(character.getIntelligence());
                    existingCharacter.setWisdom(character.getWisdom());
                    existingCharacter.setCharisma(character.getCharisma());
                    existingCharacter.setRaceId(character.getRaceId());
                    existingCharacter.setClassId(character.getClassId());
                    return characterRepository.save(existingCharacter);
                })
                .flatMap(statService::updateCurrentStats);
    }

    public Mono<Character> generateCharacter(Character character) {
        if (character.getStrength() == 0) {
            character.setStrength(rollStat());
            character.setDexterity(rollStat());
            character.setConstitution(rollStat());
            character.setIntelligence(rollStat());
            character.setWisdom(rollStat());
            character.setCharisma(rollStat());
        }
        return statService.updateCurrentStats(character);
    }

    private int rollStat() {
        return random.nextInt(4) + 1;
    }
}
