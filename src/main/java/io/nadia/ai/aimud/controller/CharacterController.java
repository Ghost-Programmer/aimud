package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.MobileMacro;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller exposing HTTP API endpoints for Character manipulation.
 */
@RestController
@RequestMapping("/api/characters")
@Slf4j
public class CharacterController {

    private final MobileService mobileService;
    private final RoomService roomService;

    public CharacterController(MobileService mobileService, RoomService roomService) {
        this.mobileService = mobileService;
        this.roomService = roomService;
    }

    /**
     * Handles HTTP POST requests to select character.
     * @param id the character id
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @PostMapping("/{id}/select")
    public Mono<ResponseEntity<Void>> selectCharacter(@PathVariable Long id) {
        log.info("REST Request to select character: {}", id);
        return mobileService.selectCharacter(id)
                .thenReturn(ResponseEntity.ok().build());
    }

    /**
     * Handles HTTP GET requests to get available characters.
     * @return dynamic reactive {@code Flux<Mobile>} response payload
     */
    @GetMapping("/available")
    public Flux<Mobile> getAvailableCharacters() {
        log.info("REST Request to get available characters");
        return Flux.fromIterable(mobileService.getAvailableCharacters());
    }

    /**
     * Handles HTTP POST requests to create character.
     * @param character the character to create
     * @return dynamic reactive {@code Mono<ResponseEntity<Mobile>>} response payload
     */
    @PostMapping
    public Mono<ResponseEntity<Mobile>> createCharacter(@RequestBody Mobile character) {
        log.info("REST Request to create character: {}", character.getName());
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> mobileService.createCharacter(username, character))
                .map(ResponseEntity::ok);
    }

    /**
     * Handles HTTP GET requests to get characters.
     * @return dynamic reactive {@code Flux<Mobile>} response payload
     */
    @GetMapping
    public Flux<Mobile> getCharacters() {
        log.info("REST Request to get characters for current user");
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> mobileService.getCharactersByUser(username))
                .flatMapIterable(list -> list);
    }

    /**
     * Handles HTTP PUT requests to update character.
     * @param id the character id
     * @param character the updated character
     * @return dynamic reactive {@code Mono<ResponseEntity<Object>>} response payload
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> updateCharacter(@PathVariable Long id, @RequestBody Mobile character) {
        log.info("REST Request to update character: {}", id);
        return mobileService.updateCharacter(id, character)
                .map(updated -> ResponseEntity.ok((Object) updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP POST requests to generate character.
     * @param character the base character
     * @return dynamic reactive {@code Mono<ResponseEntity<Mobile>>} response payload
     */
    @PostMapping("/generate")
    public Mono<ResponseEntity<Mobile>> generateCharacter(@RequestBody Mobile character) {
        log.info("REST Request to generate character: {}", character.getName());
        return mobileService.generateCharacter(character)
                .map(ResponseEntity::ok);
    }

    /**
     * Handles HTTP GET requests to get character.
     * @param id the character id
     * @return dynamic reactive {@code Mono<ResponseEntity<Object>>} response payload
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getCharacter(@PathVariable Long id) {
        log.info("REST Request to get character: {}", id);
        return mobileService.getCharacterById(id)
                .map(c -> ResponseEntity.ok((Object) c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP POST requests to add command.
     * @param id the character id
     * @param command the command string
     * @return dynamic reactive {@code Mono<ResponseEntity<Void>>} response payload
     */
    @PostMapping("/{id}/command")
    public Mono<ResponseEntity<Void>> addCommand(@PathVariable Long id, @RequestBody String command) {
        log.info("REST Request to add command '{}' to character: {}", command, id);
        mobileService.addCommand(id, command);
        return Mono.just(ResponseEntity.ok().build());
    }

    /**
     * Handles HTTP POST requests to equip item.
     * @param id the character id
     * @param itemId the item id
     * @return dynamic reactive {@code Mono<ResponseEntity<Mobile>>} response payload
     */
    @PostMapping("/{id}/equip/{itemId}")
    public Mono<ResponseEntity<Mobile>> equipItem(@PathVariable Long id, @PathVariable Long itemId) {
        log.info("REST Request to equip item {} for character: {}", itemId, id);
        return mobileService.getCharacterById(id)
                .flatMap(character -> mobileService.equipItem(character, itemId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP POST requests to unequip item.
     * @param id the character id
     * @param slot the slot to unequip
     * @return dynamic reactive {@code Mono<ResponseEntity<Mobile>>} response payload
     */
    @PostMapping("/{id}/unequip/{slot}")
    public Mono<ResponseEntity<Mobile>> unequipItem(@PathVariable Long id, @PathVariable String slot) {
        log.info("REST Request to unequip slot '{}' for character: {}", slot, id);
        return mobileService.getCharacterById(id)
                .flatMap(character -> mobileService.unequipItem(character, slot))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP POST requests to drop item.
     * @param id the character id
     * @param itemId the item id
     * @return dynamic reactive {@code Mono<ResponseEntity<Mobile>>} response payload
     */
    @PostMapping("/{id}/drop/{itemId}")
    public Mono<ResponseEntity<Mobile>> dropItem(@PathVariable Long id, @PathVariable Long itemId) {
        log.info("REST Request to drop item {} for character: {}", itemId, id);
        return mobileService.getCharacterById(id)
                .flatMap(character -> mobileService.dropItem(character, itemId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Handles HTTP GET requests to get character room.
     * @param id the character id
     * @return dynamic reactive {@code Mono<ResponseEntity<Object>>} response payload
     */
    @GetMapping("/{id}/room")
    public Mono<ResponseEntity<Object>> getCharacterRoom(@PathVariable Long id) {
        log.info("REST Request to get room for character: {}", id);
        return mobileService.getCharacterById(id)
                .flatMap(character -> roomService.getRoom(character.getCurrentRoomId())
                        .map(room -> ResponseEntity.ok((Object) room))
                        .defaultIfEmpty(ResponseEntity.notFound().build()))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Handles HTTP GET requests to get macros.
     * @param id the character id
     * @return dynamic reactive {@code Flux<MobileMacro>} response payload
     */
    @GetMapping("/{id}/macros")
    public Flux<MobileMacro> getMacros(@PathVariable Long id) {
        log.info("REST Request to get macros for character: {}", id);
        return mobileService.getCharacterMacros(id);
    }

    /**
     * Handles HTTP POST requests to save macros.
     * @param id the character id
     * @param macros the macros
     * @return dynamic reactive {@code Flux<MobileMacro>} response payload
     */
    @PostMapping("/{id}/macros")
    public Flux<MobileMacro> saveMacros(@PathVariable Long id, @RequestBody java.util.List<MobileMacro> macros) {
        log.info("REST Request to save macros for character: {}", id);
        return mobileService.saveCharacterMacros(id, macros);
    }
}

