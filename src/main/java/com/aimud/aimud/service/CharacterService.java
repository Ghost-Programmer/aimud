package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.CharacterEffectRepository;
import com.aimud.aimud.repository.CharacterRepository;
import com.aimud.aimud.repository.SkillRepository;
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
    private final SkillRepository skillRepository;
    private final ItemService itemService;
    private final MobileService mobileService;

    // In-memory storage for active/available characters
    private final ConcurrentHashMap<Long, Character> availableCharacters = new ConcurrentHashMap<>();

    public CharacterService(CharacterRepository characterRepository, UserRepository userRepository, StatService statService, DatabaseClient databaseClient, CharacterEffectRepository characterEffectRepository, CommunicationService communicationService, RoomService roomService, CharacterClassRepository characterClassRepository, SkillRepository skillRepository, ItemService itemService, MobileService mobileService) {
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.statService = statService;
        this.databaseClient = databaseClient;
        this.characterEffectRepository = characterEffectRepository;
        this.communicationService = communicationService;
        this.itemService = itemService;
        this.mobileService = mobileService;
        this.communicationService.setCharacterService(this);
        this.roomService = roomService;
        this.characterClassRepository = characterClassRepository;
        this.skillRepository = skillRepository;
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

                    return statService.updateCurrentStats(character)
                            .map(preparedCharacter -> {
                                preparedCharacter.setCurrentHp(preparedCharacter.getMaxHp());
                                preparedCharacter.setCurrentMana(preparedCharacter.getMaxMana());
                                return preparedCharacter;
                            })
                            .flatMap(characterRepository::save);
                })
                .flatMap(savedCharacter -> {
                    if (savedCharacter.getClassId() != null) {
                        return characterClassRepository.findById(savedCharacter.getClassId())
                                .flatMap(characterClass -> {
                                    Mono<Character> itemsMono = Mono.just(savedCharacter);
                                    
                                    List<Long> startingItemIds = characterClass.getStartingItemIds();
                                    if (!startingItemIds.isEmpty()) {
                                        itemsMono = Flux.fromIterable(startingItemIds)
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
                                    }
                                    
                                    return itemsMono.flatMap(c -> {
                                        List<String> startingSkills = characterClass.getStartingSkillNames();
                                        if (startingSkills.isEmpty()) {
                                            return Mono.just(c);
                                        }
                                        return Flux.fromIterable(startingSkills)
                                                .flatMap(skillName -> {
                                                    Skill skill = new Skill();
                                                    skill.setCharacterId(c.getId());
                                                    skill.setName(skillName);
                                                    skill.setRank(1);
                                                    return skillRepository.save(skill)
                                                            .onErrorResume(e -> {
                                                                log.error("Failed to add starting skill {} to character {}", skillName, c.getId(), e);
                                                                return Mono.empty();
                                                            });
                                                })
                                                .then(Mono.just(c));
                                    });
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

        // Extract unique item IDs from the inventory list to prevent duplicate key exceptions
        List<Long> uniqueItemIds = inventory.stream()
                                            .map(Item::getId)
                                            .filter(java.util.Objects::nonNull)
                                            .distinct() // This ensures only unique item IDs are processed
                                            .collect(Collectors.toList());

        return databaseClient.sql("DELETE FROM character_inventory WHERE character_id = :characterId")
                .bind("characterId", character.getId())
                .then()
                .thenMany(Flux.fromIterable(uniqueItemIds)) // Iterate over unique IDs
                .flatMap(itemId -> databaseClient.sql("INSERT INTO character_inventory (character_id, item_id) VALUES (:characterId, :itemId)")
                            .bind("characterId", character.getId())
                            .bind("itemId", itemId)
                            .fetch()
                            .rowsUpdated()
                )
                .then(Mono.just(character));
    }

    public Mono<Character> equipItem(Character character, Long itemId) {
        log.info("Equipping item {} for character {}", itemId, character.getName());
        Item itemToEquip = character.getInventory().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (itemToEquip == null) {
            communicationService.sendTextMessage(character, "\n\nYou don't have that item in your inventory.");
            return Mono.just(character);
        }

        // Check if item is equippable
        boolean isEquippable = switch (itemToEquip.getItemType()) {
            case WEAPON, TWO_HANDED_WEAPON, LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR -> true;
            default -> false;
        };

        if (!isEquippable) {
            communicationService.sendTextMessage(character, "\n\nYou cannot equip " + itemToEquip.getName() + ".");
            return Mono.just(character);
        }

        List<Item> currentInventory = new ArrayList<>(character.getInventory());
        Item oldItem1 = null;
        Item oldItem2 = null;

        switch (itemToEquip.getWearLocation()) {
            case HEAD -> {
                oldItem1 = character.getHead();
                character.setHead(itemToEquip);
            }
            case CHEST -> {
                oldItem1 = character.getChest();
                character.setChest(itemToEquip);
            }
            case LEGS -> {
                oldItem1 = character.getLegs();
                character.setLegs(itemToEquip);
            }
            case FEET -> {
                oldItem1 = character.getFeet();
                character.setFeet(itemToEquip);
            }
            case ARMS -> {
                oldItem1 = character.getArms();
                character.setArms(itemToEquip);
            }
            case HANDS -> {
                oldItem1 = character.getHands();
                character.setHands(itemToEquip);
            }
            case FINGER -> {
                if (character.getRightFinger() == null) {
                    character.setRightFinger(itemToEquip);
                } else if (character.getLeftFinger() == null) {
                    character.setLeftFinger(itemToEquip);
                } else {
                    oldItem1 = character.getLeftFinger();
                    character.setLeftFinger(itemToEquip);
                }
            }
            case WRIST -> {
                if (character.getRightWrist() == null) {
                    character.setRightWrist(itemToEquip);
                } else if (character.getLeftWrist() == null) {
                    character.setLeftWrist(itemToEquip);
                } else {
                    oldItem1 = character.getLeftWrist();
                    character.setLeftWrist(itemToEquip);
                }
            }
            case EAR -> {
                if (character.getRightEar() == null) {
                    character.setRightEar(itemToEquip);
                } else if (character.getLeftEar() == null) {
                    character.setLeftEar(itemToEquip);
                } else {
                    oldItem1 = character.getLeftEar();
                    character.setLeftEar(itemToEquip);
                }
            }
            case NECK -> {
                oldItem1 = character.getNeck();
                character.setNeck(itemToEquip);
            }
            case FACE -> {
                oldItem1 = character.getFace();
                character.setFace(itemToEquip);
            }
            case WAIST -> {
                oldItem1 = character.getWaist();
                character.setWaist(itemToEquip);
            }
            case PRIMARY -> {
                oldItem1 = character.getPrimary();
                character.setPrimary(itemToEquip);
            }
            case OFFHAND -> {
                oldItem1 = character.getOffhand();
                character.setOffhand(itemToEquip);
            }
            default -> {
                communicationService.sendTextMessage(character, "\n\nThis item cannot be worn.");
                return Mono.just(character);
            }
        }

        // Special handling for 2H weapons: if it's a 2H weapon, it goes to primary and we might need to clear offhand
        if (itemToEquip.getItemType() == com.aimud.aimud.types.ItemType.TWO_HANDED_WEAPON) {
            // If it wasn't already assigned to primary (which it should be if wear location is PRIMARY)
            if (character.getPrimary() != itemToEquip) {
                oldItem1 = character.getPrimary();
                character.setPrimary(itemToEquip);
            }
            if (character.getOffhand() != null) {
                oldItem2 = character.getOffhand();
                character.setOffhand(null);
            }
        }

        currentInventory.remove(itemToEquip);
        if (oldItem1 != null) {
            currentInventory.add(oldItem1);
        }
        if (oldItem2 != null) {
            currentInventory.add(oldItem2);
        }
        character.setInventory(currentInventory);

        communicationService.sendTextMessage(character, "\n\nYou equip " + itemToEquip.getName() + ".");

        return save(character)
                .flatMap(savedChar -> updateInventory(savedChar, currentInventory));
    }

    public Mono<Character> dropItem(Character character, Long itemId) {
        log.info("Dropping item {} for character {}", itemId, character.getName());
        Item itemToDrop = character.getInventory().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (itemToDrop == null) {
            communicationService.sendTextMessage(character, "\n\nYou don't have that item in your inventory.");
            return Mono.just(character);
        }

        List<Item> currentInventory = new ArrayList<>(character.getInventory());
        currentInventory.remove(itemToDrop);
        character.setInventory(currentInventory);

        return this.roomService.addItemToRoom(character.getCurrentRoomId(), itemId)
                .then(this.save(character))
                .flatMap(savedChar -> updateInventory(savedChar, currentInventory))
                .flatMap(savedChar -> getCharacterById(savedChar.getId()))
                .doOnNext(savedChar -> {
                    communicationService.sendTextMessage(savedChar, "\n\nYou drop " + itemToDrop.getName() + ".");
                    communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " drops " + itemToDrop.getName() + ".");
                    communicationService.sendCharacterUpdate(savedChar);
                });
    }

    public Mono<Character> takeItem(Character character, Long itemId) {
        log.info("Taking item {} for character {}", itemId, character.getName());

        return this.itemService.getItem(itemId)
                .flatMap(itemToTake -> {
                    List<Item> currentInventory = new ArrayList<>(character.getInventory());
                    currentInventory.add(itemToTake);
                    character.setInventory(currentInventory);

                    return this.roomService.removeItemFromRoom(character.getCurrentRoomId(), itemId)
                            .then(this.save(character))
                            .flatMap(savedChar -> updateInventory(savedChar, currentInventory))
                            .flatMap(savedChar -> getCharacterById(savedChar.getId()))
                            .doOnNext(savedChar -> {
                                communicationService.sendTextMessage(savedChar, "\n\nYou take " + itemToTake.getName() + ".");
                                communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " takes " + itemToTake.getName() + ".");
                                communicationService.sendCharacterUpdate(savedChar);
                            });
                });
    }

    public Mono<Character> generateCharacter(Character character) {
        if (character.getStrength() == 0) {
            character.setStrength(rollStat());
            character.setDexterity(rollStat());
            character.setConstitution(rollStat());
            character.setIntelligence(rollStat());
            character.setWisdom(rollStat());
            character.setCharisma(character.getCharisma());
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
                                this.communicationService.sendTextMessage(character, "\n\n" + room.getDescription() + "\n\n");

                                this.findAllByRoomId(room.getId()).stream().filter(c -> !c.getId().equals(character.getId())).forEach(c -> {
                                    this.communicationService.sendTextMessage(character, "\nYou see " + c.getName() + " here.");
                                });

                                this.mobileService.getMobilesInRoom(room.getId()).forEach(m -> {
                                    this.communicationService.sendTextMessage(character, "\nYou see " + m.getName() + " here.");
                                });

                                room.getItemIds().stream().forEach(itemId -> {
                                    this.itemService.getItem(itemId)
                                            .doOnNext(item ->
                                    this.communicationService.sendTextMessage(character, "\nYou see " + item.getName() + " laying here."));
                                });
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
