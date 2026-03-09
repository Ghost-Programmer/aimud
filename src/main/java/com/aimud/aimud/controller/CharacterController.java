package com.aimud.aimud.controller;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final RoomService roomService;

    public CharacterController(CharacterService characterService, RoomService roomService) {
        this.characterService = characterService;
        this.roomService = roomService;
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
                .flatMapMany(username -> characterService.getCharactersByUser(username)
                        .flatMapMany(Flux::fromIterable));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateCharacter(@PathVariable Long id, @RequestBody Character character) {
        return characterService.updateCharacter(id, character)
                .map(updated -> ResponseEntity.ok((Object)updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<Character>> generateCharacter(@RequestBody Character character) {
        return characterService.generateCharacter(character)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getCharacter(@PathVariable Long id) {
        return characterService.getCharacterById(id)
                .map(c -> ResponseEntity.ok((Object)c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/room")
    public Mono<ResponseEntity<Object>> getCharacterRoom(@PathVariable Long id) {
        return characterService.getCharacterById(id)
                .flatMap(character -> roomService.getRoom(character.getCurrentRoomId())
                        .map(room -> ResponseEntity.ok((Object)room))
                        .defaultIfEmpty(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
