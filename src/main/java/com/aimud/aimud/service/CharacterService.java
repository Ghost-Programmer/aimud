package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.CharacterEffectRepository;
import com.aimud.aimud.repository.CharacterRepository;
import com.aimud.aimud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final StatService statService;
    private final DatabaseClient databaseClient;
    private final CharacterEffectRepository characterEffectRepository;
    private final Random random = new Random();
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterClassRepository characterClassRepository;

    // In-memory storage for active/available characters
    private final ConcurrentHashMap<Long, Character> availableCharacters = new ConcurrentHashMap<>();

    public CharacterService(CharacterRepository characterRepository, UserRepository userRepository, StatService statService, DatabaseClient databaseClient, CharacterEffectRepository characterEffectRepository, CommunicationService communicationService, RoomService roomService, CharacterClassRepository characterClassRepository) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.statService = statService;
        this.databaseClient = databaseClient;
        this.characterEffectRepository = characterEffectRepository;
        this.communicationService = communicationService;
        this.communicationService.setCharacterService(this);
        this.roomService = roomService;
        this.characterClassRepository = characterClassRepository;
    }

    public List<Character> findAllByRoomId(Long roomId) {
        return availableCharacters.values().stream()
                .filter(character -> character.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    public Mono<Void> selectCharacter(Long characterId) {
        log.info("Selecting character with id: {}", characterId);
        return getCharacterById(characterId)
                .flatMap(character -> {
                    availableCharacters.put(character.getId(), character);
                    log.info("Character {} added to available list", character.getName());
                    return this.enterRoom(character, character.getCurrentRoomId());
                })
                .then();
    }

    public Mono<Void> deselectCharacter(Long characterId) {
        log.info("Deselecting character with id: {}", characterId);
        Character character = availableCharacters.get(characterId);
        if (character != null) {
            this.communicationService.roomMessage(character, "\n" + character.getName() + " has left the game.");
            availableCharacters.remove(characterId);
        }
        return Mono.empty();
    }

    public List<Character> getAvailableCharacters() {
        return new ArrayList<>(availableCharacters.values());
    }
    
    public void removeAvailableCharacter(Long characterId) {
        availableCharacters.remove(characterId);
    }

    public Mono<Character> createCharacter(String username, Character character) {
        log.info("Creating character for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    character.setUserId(user.getId());
                    log.debug("Found user id: {} for username: {}", user.getId(), username);
                    return characterRepository.save(character);
                })
                .flatMap(savedCharacter -> {
                    if (savedCharacter.getClassId() != null) {
                        return characterClassRepository.findById(savedCharacter.getClassId())
                                .flatMap(characterClass -> {
                                    List<Long> startingItemIds = characterClass.getStartingItemIds();
                                    if (startingItemIds.isEmpty()) {
                                        return Mono.just(savedCharacter);
                                    }
                                    return Flux.fromIterable(startingItemIds)
                                            .flatMap(itemId -> databaseClient.sql("INSERT INTO character_inventory (character_id, item_id) VALUES (:characterId, :itemId)")
                                                    .bind("characterId", savedCharacter.getId())
                                                    .bind("itemId", itemId)
                                                    .fetch()
                                                    .rowsUpdated()
                                                    .onErrorResume(e -> {
                                                        log.error("Failed to add starting item {} to character {}", itemId, savedCharacter.getId(), e);
                                                        return Mono.just(0L);
                                                    })
                                            )
                                            .then(Mono.just(savedCharacter));
                                })
                                .defaultIfEmpty(savedCharacter);
                    }
                    return Mono.just(savedCharacter);
                })
                .flatMap(statService::updateCurrentStats);
    }

    public Mono<List<Character>> getCharactersByUser(String username) {
        log.info("Fetching characters for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMapMany(user -> characterRepository.findByUserId(user.getId()))
                .flatMap(statService::updateCurrentStats)
                .collectList();
    }

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
                .flatMap(updatedCharacter -> statService.updateCurrentStats(updatedCharacter)
                        .doOnNext(c -> {
                            // Update the character in the available map if it exists there
                            if (availableCharacters.containsKey(c.getId())) {
                                availableCharacters.put(c.getId(), c);
                            }
                        }));
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

    public Mono<Character> getCharacterById(Long id) {
        log.info("Fetching character by id: {}", id);
        return characterRepository.findById(id)
                .flatMap(statService::updateCurrentStats);
    }

    public void addCommand(Long characterId, String command) {
        log.info("Adding command '{}' to character id {}", command, characterId);
        getAvailableCharacters().stream()
                .filter(c -> c.getId().equals(characterId))
                .findFirst()
                .ifPresent(c -> {
                    c.getCommandQueue().add(command);
                    c.setIdle(0);
                });
    }

    public Mono<Character> save(Character character) {
        log.info("Saving character: {}", character.getName());
        return characterRepository.save(character)
                .flatMap(savedCharacter -> {
                    log.debug("Updating spell effects for character: {}", savedCharacter.getId());
                    return characterEffectRepository.deleteByCharacterId(savedCharacter.getId())
                            .thenMany(Flux.fromIterable(character.getSpellEffects()))
                            .doOnNext(effect -> effect.setCharacterId(savedCharacter.getId()))
                            .flatMap(characterEffectRepository::save)
                            .collectList()
                            .doOnNext(savedCharacter::setSpellEffects)
                            .thenReturn(savedCharacter);
                })
                .flatMap(statService::updateCurrentStats)
                .doOnNext(c -> {
                    if (availableCharacters.containsKey(c.getId())) {
                        availableCharacters.put(c.getId(), c);
                    }
                });
    }

    private int rollStat() {
        return random.nextInt(4) + 1;
    }

    public Mono<Void> enterRoom(Character character, Long roomId) {
        log.info("Entering room {} for character {}", roomId, character.getName());
        return this.roomService.getRoom(roomId)
                .flatMap(room -> {
                    if (character.getCurrentRoomId() != null) {
                        this.communicationService.roomMessage(character, "\n" + character.getName() + " has left the room.");
                    }

                    character.setCurrentRoomId(room.getId());
                    return this.save(character)
                            .doOnNext(savedChar -> {
                                this.communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " has entered the room.");

                                this.communicationService.sendTextMessage(character, "\n\nYou have entered " + room.getName() + ".");
                                this.communicationService.sendTextMessage(character, "\n\n" + room.getDescription() + "\n");
                                List<String> exits = new ArrayList<>();
                                if (room.getNorthId() != null) {
                                    exits.add("North");
                                }
                                if (room.getEastId() != null) {
                                    exits.add("East");
                                }
                                if (room.getSouthId() != null) {
                                    exits.add("South");
                                }
                                if (room.getWestId() != null) {
                                    exits.add("West");
                                }
                                if (room.getUpId() != null) {
                                    exits.add("Up");
                                }
                                if (room.getDownId() != null) {
                                    exits.add("Down");
                                }
                                this.communicationService.sendTextMessage(character, "\n\nExits: " + String.join(", ", exits));
                            })
                            .then();
                });
    }
}
