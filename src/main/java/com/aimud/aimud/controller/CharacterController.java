package com.aimud.aimud.controller;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.service.CharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping
    public Mono<ResponseEntity<Character>> createCharacter(@RequestBody Character character) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> characterService.createCharacter(username, character))
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<Character> getCharacters() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMapMany(characterService::getCharactersByUser);
    }
}
