package com.aimud.aimud.service;

import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.repository.*;
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

    private final MobileRepository mobileRepository;
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
    private final ConcurrentHashMap<Long, Mobile> availableCharacters = new ConcurrentHashMap<>();

    public CharacterService(MobileRepository mobileRepository, UserRepository userRepository, StatService statService, DatabaseClient databaseClient, CharacterEffectRepository characterEffectRepository, CommunicationService communicationService, RoomService roomService, CharacterClassRepository characterClassRepository, SkillRepository skillRepository, ItemService itemService, MobileService mobileService) {
        this.mobileRepository = mobileRepository;
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

    public List<Mobile> findAllByRoomId(Long roomId) {
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
        Mobile character = availableCharacters.get(characterId);
        if (character != null) {
            this.communicationService.roomMessage(character, "\n" + character.getName() + " has left the game.");
            availableCharacters.remove(characterId);
        }
        return Mono.empty();
    }

    public List<Mobile> getAvailableCharacters() {
        return new ArrayList<>(availableCharacters.values());
    }

    public void removeAvailableCharacter(Long characterId) {
        availableCharacters.remove(characterId);
    }

    public Mono<Mobile> createCharacter(String username, Mobile character) {
        log.info("Creating character for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    character.setUserId(user.getId());
                    character.setCurrentRoomId(1L);
                    log.debug("Found user id: {} for username: {}", user.getId(), username);

                    return statService.updateCurrentStats(character)
                            .map(preparedCharacter -> {
                                preparedCharacter.setCurrentHp(preparedCharacter.getMaxHp());
                                preparedCharacter.setCurrentMana(preparedCharacter.getMaxMana());
                                return preparedCharacter;
                            })
                            .flatMap(mobileRepository::save);
                })
                .flatMap(savedCharacter -> {
                    if (savedCharacter.getClassId() != null) {
                        return characterClassRepository.findById(savedCharacter.getClassId())
                                .flatMap(characterClass -> {
                                    Mono<Mobile> itemsMono = Mono.just(savedCharacter);

                                    List<Long> startingItemIds = characterClass.getStartingItemIds();
                                    if (!startingItemIds.isEmpty()) {
                                        itemsMono = Flux.fromIterable(startingItemIds)
                                                .flatMap(itemId -> databaseClient.sql("INSERT INTO character_inventory (character_id, item_id) " +
                                                                "SELECT :characterId, :itemId " +
                                                                "WHERE EXISTS (SELECT 1 FROM items WHERE id = :itemIdCheck)")
                                                        .bind("characterId", savedCharacter.getId())
                                                        .bind("itemId", itemId)
                                                        .bind("itemIdCheck", itemId)
                                                        .fetch()
                                                        .rowsUpdated()
                                                        .doOnNext(rowsUpdated -> {
                                                            if (rowsUpdated == 0) {
                                                                log.warn("Skipping missing starting item {} for character {}", itemId, savedCharacter.getId());
                                                            }
                                                        })
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

    public Mono<List<Mobile>> getCharactersByUser(String username) {
        log.info("Fetching characters for user: {}", username);
        return userRepository.findByUsername(username)
                .flatMapMany(user -> mobileRepository.findByUserId(user.getId()))
                .flatMap(statService::updateCurrentStats)
                .collectList();
    }

    public Mono<Mobile> updateCharacter(Long id, Mobile character) {
        log.info("Updating character with id: {}", id);
        return mobileRepository.findById(id)
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
                    return mobileRepository.save(existingCharacter)
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

    private Mono<Mobile> updateInventory(Mobile character, List<Item> inventory) {
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

    public Mono<Mobile> equipItem(Mobile character, Long itemId) {
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
                .flatMap(savedChar -> updateInventory(savedChar, currentInventory))
                .flatMap(savedChar -> getCharacterById(savedChar.getId()))
                .doOnNext(savedChar -> communicationService.sendCharacterUpdate(savedChar));
    }

    public Mono<Mobile> dropItem(Mobile character, Long itemId) {
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

    public Mono<Mobile> destroyInventoryItem(Mobile character, Long itemId) {
        log.info("Destroying item {} for character {}", itemId, character.getName());
        Item itemToDestroy = character.getInventory().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (itemToDestroy == null) {
            communicationService.sendTextMessage(character, "\n\nYou don't have that item in your inventory.");
            return Mono.just(character);
        }

        List<Item> currentInventory = new ArrayList<>(character.getInventory());
        currentInventory.remove(itemToDestroy);
        character.setInventory(currentInventory);

        return this.save(character)
                .flatMap(savedChar -> updateInventory(savedChar, currentInventory))
                .flatMap(savedChar -> getCharacterById(savedChar.getId()));
    }

    public Mono<Mobile> unequipItem(Mobile character, String slot) {
        log.info("Unequipping slot '{}' for character {}", slot, character.getName());

        Item itemToUnequip;
        switch (slot.toLowerCase()) {
            case "head" -> {
                itemToUnequip = character.getHead();
                character.setHead(null);
            }
            case "chest" -> {
                itemToUnequip = character.getChest();
                character.setChest(null);
            }
            case "legs" -> {
                itemToUnequip = character.getLegs();
                character.setLegs(null);
            }
            case "feet" -> {
                itemToUnequip = character.getFeet();
                character.setFeet(null);
            }
            case "arms" -> {
                itemToUnequip = character.getArms();
                character.setArms(null);
            }
            case "hands" -> {
                itemToUnequip = character.getHands();
                character.setHands(null);
            }
            case "rightfinger" -> {
                itemToUnequip = character.getRightFinger();
                character.setRightFinger(null);
            }
            case "leftfinger" -> {
                itemToUnequip = character.getLeftFinger();
                character.setLeftFinger(null);
            }
            case "rightwrist" -> {
                itemToUnequip = character.getRightWrist();
                character.setRightWrist(null);
            }
            case "leftwrist" -> {
                itemToUnequip = character.getLeftWrist();
                character.setLeftWrist(null);
            }
            case "neck" -> {
                itemToUnequip = character.getNeck();
                character.setNeck(null);
            }
            case "leftear" -> {
                itemToUnequip = character.getLeftEar();
                character.setLeftEar(null);
            }
            case "rightear" -> {
                itemToUnequip = character.getRightEar();
                character.setRightEar(null);
            }
            case "face" -> {
                itemToUnequip = character.getFace();
                character.setFace(null);
            }
            case "waist" -> {
                itemToUnequip = character.getWaist();
                character.setWaist(null);
            }
            case "primary" -> {
                itemToUnequip = character.getPrimary();
                character.setPrimary(null);
            }
            case "offhand" -> {
                itemToUnequip = character.getOffhand();
                character.setOffhand(null);
            }
            default -> {
                communicationService.sendTextMessage(character, "\n\nUnknown equipment slot: " + slot);
                return Mono.just(character);
            }
        }

        if (itemToUnequip == null || itemToUnequip.getId() == null) {
            communicationService.sendTextMessage(character, "\n\nThat slot is empty.");
            return Mono.just(character);
        }

        final Item unequipped = itemToUnequip;
        List<Item> currentInventory = new ArrayList<>(character.getInventory());
        currentInventory.add(unequipped);
        character.setInventory(currentInventory);

        communicationService.sendTextMessage(character, "\n\nYou unequip " + unequipped.getName() + ".");

        return save(character)
                .flatMap(savedChar -> updateInventory(savedChar, currentInventory))
                .flatMap(savedChar -> getCharacterById(savedChar.getId()))
                .doOnNext(communicationService::sendCharacterUpdate);
    }

    public Mono<Mobile> takeItem(Mobile character, Long itemId) {
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

    public Mono<Mobile> clearInventoryAndEquipment(Mobile character) {
        log.info("Clearing inventory and equipment for character: {}", character.getName());
        character.setHead(null);
        character.setChest(null);
        character.setLegs(null);
        character.setFeet(null);
        character.setArms(null);
        character.setHands(null);
        character.setRightFinger(null);
        character.setLeftFinger(null);
        character.setRightWrist(null);
        character.setLeftWrist(null);
        character.setNeck(null);
        character.setLeftEar(null);
        character.setRightEar(null);
        character.setFace(null);
        character.setWaist(null);
        character.setPrimary(null);
        character.setOffhand(null);
        character.setInventory(new ArrayList<>());
        return save(character)
                .flatMap(savedChar -> updateInventory(savedChar, new ArrayList<>()));
    }

    public Mono<Mobile> generateCharacter(Mobile character) {
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

    public Mono<Mobile> getCharacterById(Long id) {
        log.info("Fetching character by id: {}", id);
        return mobileRepository.findById(id)
                .filter(mobile -> mobile.getUserId() != null)
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

    public Mono<Mobile> save(Mobile character) {
        log.info("Saving character: {}", character.getName());
        return mobileRepository.save(character)
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

    public Mono<Void> enterRoom(Mobile character, Long roomId) {
        log.info("Entering room {} for character {}", roomId, character.getName());
        return this.roomService.getRoom(roomId)
                .flatMap(room -> {
                    if (character.getCurrentRoomId() != null) {
                        if (!character.isHidden() && !character.isInvisible()) {
                            this.communicationService.roomMessage(character, "\n" + character.getName() + " has left the room.");
                        }
                    }

                    character.setCurrentRoomId(room.getId());
                    return this.save(character)
                            .doOnNext(savedChar -> {
                                if (!savedChar.isHidden() && !savedChar.isInvisible()) {
                                    this.communicationService.roomMessage(savedChar, "\n" + savedChar.getName() + " has entered the room.");
                                }

                                this.communicationService.sendTextMessage(character, "\n\nYou have entered " + room.getName() + ".");
                                this.communicationService.sendTextMessage(character, "\n\n" + room.getDescription() + "\n\n");

                                this.findAllByRoomId(room.getId()).stream().filter(c -> !c.getId().equals(character.getId())).forEach(c -> {
                                    if (!c.isHidden() && !c.isInvisible()) {
                                        this.communicationService.sendTextMessage(character, "\nYou see " + c.getName() + " here.");
                                    }
                                });

                                this.mobileService.getMobilesInRoom(room.getId()).forEach(m -> {
                                    if (!m.isHidden() && !m.isInvisible()) {
                                        this.communicationService.sendTextMessage(character, "\nYou see " + m.getName() + " here.");
                                    }
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
