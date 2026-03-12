package com.aimud.aimud.controller;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/characters")
@Slf4j
public class CharacterController {

    private final CharacterService characterService;
    private final RoomService roomService;

    public CharacterController(CharacterService characterService, RoomService roomService) {
        this.characterService = characterService;
        this.roomService = roomService;
    }

    @PostMapping("/{id}/select")
    public Mono<ResponseEntity<Void>> selectCharacter(@PathVariable Long id) {
        log.info("REST Request to select character: {}", id);
        return characterService.selectCharacter(id)
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping("/available")
    public Flux<Character> getAvailableCharacters() {
        log.info("REST Request to get available characters");
        return Flux.fromIterable(characterService.getAvailableCharacters());
    }

    @PostMapping
    public Mono<ResponseEntity<Character>> createCharacter(@RequestBody Character character) {
        log.info("REST Request to create character: {}", character.getName());
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> characterService.createCharacter(username, character))
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<Character> getCharacters() {
        log.info("REST Request to get characters for current user");
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMapMany(characterService::getCharactersByUser)
                .flatMapIterable(list -> list);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateCharacter(@PathVariable Long id, @RequestBody Character character) {
        log.info("REST Request to update character: {}", id);
        return characterService.updateCharacter(id, character)
                .map(updated -> ResponseEntity.ok((Object)updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<Character>> generateCharacter(@RequestBody Character character) {
        log.info("REST Request to generate character: {}", character.getName());
        return characterService.generateCharacter(character)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getCharacter(@PathVariable Long id) {
        log.info("REST Request to get character: {}", id);
        return characterService.getCharacterById(id)
                .map(c -> ResponseEntity.ok((Object)c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/room")
    public Mono<ResponseEntity<Object>> getCharacterRoom(@PathVariable Long id) {
        log.info("REST Request to get room for character: {}", id);
        return characterService.getCharacterById(id)
                .flatMap(character -> roomService.getRoom(character.getCurrentRoomId())
                        .map(room -> ResponseEntity.ok((Object)room))
                        .defaultIfEmpty(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
