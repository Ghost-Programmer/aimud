package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.repository.CharacterRepository;
import com.aimud.aimud.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    public CharacterService(CharacterRepository characterRepository, UserRepository userRepository) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
    }

    public Mono<Character> createCharacter(String username, Character character) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    character.setUserId(user.getId());
                    return characterRepository.save(character);
                });
    }

    public Flux<Character> getCharactersByUser(String username) {
        return userRepository.findByUsername(username)
                .flatMapMany(user -> characterRepository.findByUserId(user.getId()));
    }
}
