package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.repository.CharacterRepository;
import com.aimud.aimud.repository.ItemRepository;
import com.aimud.aimud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final StatService statService;
    private final DatabaseClient databaseClient;
    private final Random random = new Random();

    public CharacterService(CharacterRepository characterRepository, UserRepository userRepository, StatService statService, DatabaseClient databaseClient) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.statService = statService;
        this.databaseClient = databaseClient;
    }

    @CacheEvict(value = "userCharacters", key = "#username")
    public Mono<Character> createCharacter(String username, Character character) {
        log.info("Creating character for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    character.setUserId(user.getId());
                    log.debug("Found user id: {} for username: {}", user.getId(), username);
                    return characterRepository.save(character);
                })
                .flatMap(statService::updateCurrentStats);
    }

    @Cacheable(value = "userCharacters", key = "#username")
    public Mono<List<Character>> getCharactersByUser(String username) {
        log.info("Fetching characters for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMapMany(user -> characterRepository.findByUserId(user.getId()))
                .flatMap(statService::updateCurrentStats)
                .collectList();
    }

    @Caching(evict = {
        @CacheEvict(value = "characters", key = "#id"),
        @CacheEvict(value = "userCharacters", allEntries = true)
    })
    public Mono<Character> updateCharacter(Long id, Character character) {
        log.info("Updating character with id: {}", id);
        return characterRepository.findById(id)
                .flatMap(existingCharacter -> {
                    log.debug("Merging character data for id: {}", id);
                    existingCharacter.setName(character.getName());
                    existingCharacter.setStrength(character.getStrength());
                    existingCharacter.setDexterity(character.getDexterity());
                    existingCharacter.setConstitution(character.getConstitution());
                    existingCharacter.setIntelligence(character.getIntelligence());
                    existingCharacter.setWisdom(character.getWisdom());
                    existingCharacter.setCharisma(character.getCharisma());
                    existingCharacter.setRaceId(character.getRaceId());
                    existingCharacter.setClassId(character.getClassId());
                    existingCharacter.setHeadId(character.getHeadId());
                    existingCharacter.setChestId(character.getChestId());
                    existingCharacter.setLegsId(character.getLegsId());
                    existingCharacter.setFeetId(character.getFeetId());
                    existingCharacter.setArmsId(character.getArmsId());
                    existingCharacter.setHandsId(character.getHandsId());
                    existingCharacter.setRightFingerId(character.getRightFingerId());
                    existingCharacter.setLeftFingerId(character.getLeftFingerId());
                    existingCharacter.setRightWristId(character.getRightWristId());
                    existingCharacter.setLeftWristId(character.getLeftWristId());
                    existingCharacter.setNeckId(character.getNeckId());
                    existingCharacter.setLeftEarId(character.getLeftEarId());
                    existingCharacter.setRightEarId(character.getRightEarId());
                    existingCharacter.setFaceId(character.getFaceId());
                    existingCharacter.setWaistId(character.getWaistId());
                    existingCharacter.setPrimaryId(character.getPrimaryId());
                    existingCharacter.setOffhandId(character.getOffhandId());
                    if (character.getCurrentRoomId() != null) {
                        existingCharacter.setCurrentRoomId(character.getCurrentRoomId());
                    }
                    return characterRepository.save(existingCharacter)
                            .flatMap(savedCharacter -> updateInventory(savedCharacter, character.getInventory()));
                })
                .flatMap(statService::updateCurrentStats);
    }

    private Mono<Character> updateInventory(Character character, List<Item> inventory) {
        if (inventory == null) return Mono.just(character);
        log.debug("Updating inventory for character: {}", character.getId());
        return databaseClient.sql("DELETE FROM character_inventory WHERE character_id = :characterId")
                .bind("characterId", character.getId())
                .then()
                .thenMany(Flux.fromIterable(inventory))
                .flatMap(item -> {
                    if (item.getId() == null) return Mono.empty();
                    return databaseClient.sql("INSERT INTO character_inventory (character_id, item_id) VALUES (:characterId, :itemId)")
                            .bind("characterId", character.getId())
                            .bind("itemId", item.getId())
                            .fetch()
                            .rowsUpdated();
                })
                .then(Mono.just(character));
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

    @Cacheable(value = "characters", key = "#id")
    public Mono<Character> getCharacterById(Long id) {
        log.info("Fetching character by id: {}", id);
        return characterRepository.findById(id)
                .flatMap(statService::updateCurrentStats);
    }

    private int rollStat() {
        return random.nextInt(4) + 1;
    }
}
