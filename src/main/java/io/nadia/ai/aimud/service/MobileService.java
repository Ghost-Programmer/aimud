package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.CharacterClass;
import io.nadia.ai.aimud.model.CharacterEffect;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Skill;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.commands.Command;
import io.nadia.ai.aimud.model.MobileMacro;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.types.ItemType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.model.MobileSkill;
import io.nadia.ai.aimud.repository.MobileActionRepository;
import io.nadia.ai.aimud.repository.MobileSkillRepository;
import io.nadia.ai.aimud.model.Room;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
/**
 * CharacterService standard implementation layer.
 * Primary processing handler mapping structural integrations natively.
 */

@Service
@Slf4j
public class MobileService {

    private final MobileRepository mobileRepository;
    private final UserRepository userRepository;
    private final StatService statService;
    private final DatabaseClient databaseClient;
    private final FactionService factionService;
    private final CharacterEffectRepository characterEffectRepository;
    private final Random random = new Random();
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final CharacterClassRepository characterClassRepository;
    private final SkillRepository skillRepository;
    private final ItemService itemService;
    private final MobileActionRepository mobileActionRepository;
    private final MobileSkillRepository mobileSkillRepository;
    private final MobileMacroRepository mobileMacroRepository;

    // In-memory storage for active/available characters
    private final ConcurrentHashMap<Long, Mobile> activeMobiles = new ConcurrentHashMap<>();

    public MobileService(MobileRepository mobileRepository, UserRepository userRepository, StatService statService,
            DatabaseClient databaseClient, CharacterEffectRepository characterEffectRepository,
            CommunicationService communicationService, RoomService roomService,
            CharacterClassRepository characterClassRepository, SkillRepository skillRepository, ItemService itemService,
            MobileActionRepository mobileActionRepository, MobileSkillRepository mobileSkillRepository, FactionService factionService, MobileMacroRepository mobileMacroRepository) {
        this.mobileRepository = mobileRepository;
        this.userRepository = userRepository;
        this.statService = statService;
        this.databaseClient = databaseClient;
        this.factionService = factionService;
        this.characterEffectRepository = characterEffectRepository;
        this.communicationService = communicationService;
        this.itemService = itemService;
        this.mobileActionRepository = mobileActionRepository;
        this.mobileSkillRepository = mobileSkillRepository;
        this.mobileMacroRepository = mobileMacroRepository;
        this.communicationService.setMobileService(this);
        this.roomService = roomService;
        this.characterClassRepository = characterClassRepository;
        this.skillRepository = skillRepository;
    }

    public Flux<MobileMacro> getCharacterMacros(Long characterId) {
        return mobileMacroRepository.findByMobileId(characterId);
    }

    public Flux<MobileMacro> saveCharacterMacros(Long characterId,
                                                 List<MobileMacro> macros) {
        return mobileMacroRepository.deleteByMobileId(characterId)
                .thenMany(Flux.fromIterable(macros))
                .flatMap(macro -> {
                    macro.setMobileId(characterId);
                    macro.setId(null);
                    return mobileMacroRepository.save(macro);
                });
    }

    public List<Mobile> findAllByRoomId(Long roomId) {
        return activeMobiles.values().stream()
                .filter(character -> character.getCurrentRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    public Mono<Void> selectCharacter(Long characterId) {
        log.info("Selecting character with id: {}", characterId);
        return getCharacterById(characterId)
                .flatMap(character -> {
                    activeMobiles.put(character.getId(), character);
                    log.info("Character {} added to available list", character.getName());
                    return this.enterRoom(character, character.getCurrentRoomId());
                })
                .then();
    }

    public Mono<Void> deselectCharacter(Long characterId) {
        log.info("Deselecting character with id: {}", characterId);
        Mobile character = activeMobiles.get(characterId);
        if (character != null) {
            this.communicationService.roomMessage(character, "\n" + character.getName() + " has left the game.");
            activeMobiles.remove(characterId);
        }
        return Mono.empty();
    }

    public boolean canTarget(Mobile target) {
        if (target == null)
            return false;
        return !target.isNonCombat();
    }

    public boolean setTarget(Mobile attacker, Mobile target) {
        if (target == null) {
            attacker.setTarget(null);
            return true;
        }

        if (canTarget(target)) {
            attacker.setTarget(target);
            return true;
        }
        return false;
    }

    public List<Mobile> getAvailableCharacters() {
        return new ArrayList<>(activeMobiles.values());
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
                                preparedCharacter.setGold(100);
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
                                                .flatMap(itemId -> databaseClient
                                                        .sql("INSERT INTO character_inventory (character_id, item_id) "
                                                                +
                                                                "SELECT :characterId, :itemId " +
                                                                "WHERE EXISTS (SELECT 1 FROM items WHERE id = :itemIdCheck)")
                                                        .bind("characterId", savedCharacter.getId())
                                                        .bind("itemId", itemId)
                                                        .bind("itemIdCheck", itemId)
                                                        .fetch()
                                                        .rowsUpdated()
                                                        .doOnNext(rowsUpdated -> {
                                                            if (rowsUpdated == 0) {
                                                                log.warn(
                                                                        "Skipping missing starting item {} for character {}",
                                                                        itemId, savedCharacter.getId());
                                                            }
                                                        })
                                                        .onErrorResume(e -> {
                                                            log.error("Failed to add starting item {} to character {}",
                                                                    itemId, savedCharacter.getId(), e);
                                                            return Mono.just(0L);
                                                        }))
                                                .then(Mono.just(savedCharacter));
                                    }

                                    return itemsMono.flatMap(c -> {
                                        Mono<Mobile> skillsMono;
                                        List<String> startingSkills = characterClass.getStartingSkillNames();
                                        if (startingSkills.isEmpty()) {
                                            skillsMono = Mono.just(c);
                                        } else {
                                            skillsMono = Flux.fromIterable(startingSkills)
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
                                        }
                                        
                                        return skillsMono.flatMap(c2 -> 
                                            databaseClient.sql("SELECT starting_effects FROM races WHERE id = :rId")
                                                    .bind("rId", c2.getRaceId())
                                                    .map((row, meta) -> {
                                                        String val = row.get(0, String.class);
                                                        return val == null ? "" : val;
                                                    })
                                                    .one()
                                                    .flatMap(effs -> {
                                                        if (effs == null || effs.isEmpty()) return Mono.just(c2);
                                                        java.util.List<Long> ids = new java.util.ArrayList<>();
                                                        for (String s : effs.split(",")) {
                                                            try { ids.add(Long.parseLong(s.trim())); } catch (Exception ignored) {}
                                                        }
                                                        return Flux.fromIterable(ids)
                                                                .flatMap(i -> characterEffectRepository.save(new CharacterEffect(c2.getId(), i, -1)))
                                                                .then(Mono.just(c2));
                                                    })
                                                    .defaultIfEmpty(c2)
                                        );
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
                            if (activeMobiles.containsKey(c.getId())) {
                                activeMobiles.put(c.getId(), c);
                            }
                        }));
    }

    public Mono<Mobile> updateInventory(Mobile character, List<Item> inventory) {
        if (inventory == null)
            return Mono.just(character);
        log.debug("Updating inventory for character: {}", character.getId());

        java.util.Map<Long, Item> uniqueItemsMap = new java.util.HashMap<>();
        java.util.List<Item> nonStackables = new java.util.ArrayList<>();
        java.util.List<Item> toProcess = new java.util.ArrayList<>(inventory);

        while (!toProcess.isEmpty()) {
            Item current = toProcess.remove(0);
            if (current != null && current.getId() != null) {
                if (current.isStackable()) {
                    if (uniqueItemsMap.containsKey(current.getId())) {
                        Item existing = uniqueItemsMap.get(current.getId());
                        existing.setCount(existing.getCount() + current.getCount());
                    } else {
                        uniqueItemsMap.put(current.getId(), current);
                    }
                } else {
                    nonStackables.add(current);
                }
            }
        }

        List<Item> finalItems = new java.util.ArrayList<>(uniqueItemsMap.values());
        finalItems.addAll(nonStackables);

        return databaseClient.sql("DELETE FROM character_inventory WHERE character_id = :characterId")
                .bind("characterId", character.getId())
                .then()
                .thenMany(Flux.fromIterable(finalItems))
                .flatMap(item -> databaseClient
                        .sql("INSERT INTO character_inventory (character_id, item_id, item_count) VALUES (:characterId, :itemId, :count)")
                        .bind("characterId", character.getId())
                        .bind("itemId", item.getId())
                        .bind("count", item.getCount())
                        .fetch()
                        .rowsUpdated())
                .then(Mono.defer(() -> {
                     character.setInventory(finalItems);
                     return Mono.just(character);
                }));
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

        // Special handling for 2H weapons: if it's a 2H weapon, it goes to primary and
        // we might need to clear offhand
        if (itemToEquip.getItemType() == ItemType.TWO_HANDED_WEAPON) {
            // If it wasn't already assigned to primary (which it should be if wear location
            // is PRIMARY)
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
                    communicationService.roomMessage(savedChar,
                            "\n" + savedChar.getName() + " drops " + itemToDrop.getName() + ".");
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
                                communicationService.sendTextMessage(savedChar,
                                        "\n\nYou take " + itemToTake.getName() + ".");
                                communicationService.roomMessage(savedChar,
                                        "\n" + savedChar.getName() + " takes " + itemToTake.getName() + ".");
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

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private CommandService commandService;

    public void addCommand(Long characterId, String command) {
        log.info("Adding command '{}' to character id {}", command, characterId);
        getAvailableCharacters().stream()
                .filter(c -> c.getId().equals(characterId))
                .findFirst()
                .ifPresent(c -> {
                    String cleanCmd = command.trim();
                    if (cleanCmd.isEmpty())
                        return;

                    String firstWord = cleanCmd.split("\\s+")[0].toLowerCase();
                    boolean isMovement = firstWord.equals("n") || firstWord.equals("s")
                            || firstWord.equals("e") || firstWord.equals("w")
                            || firstWord.equals("u") || firstWord.equals("d")
                            || firstWord.equals("north") || firstWord.equals("south")
                            || firstWord.equals("east") || firstWord.equals("west")
                            || firstWord.equals("up") || firstWord.equals("down");

                    if (isMovement && c.getCommandQueue().isEmpty()) {
                        c.setIdle(0);
                        Command task = commandService.getTask(firstWord);
                        if (task != null) {
                            task.execute(c, command).subscribe(
                                    null,
                                    e -> log.error("Error executing immediate movement command", e));
                        } else {
                            c.getCommandQueue().add(command);
                        }
                    } else {
                        c.getCommandQueue().add(command);
                        c.setIdle(0);
                    }
                });
    }

    public Mono<Mobile> save(Mobile character) {
        log.info("Saving character: {}", character.getName());
        return mobileRepository.save(character)
                .flatMap(savedCharacter -> {
                    log.debug("Updating spell effects for character: {}", savedCharacter.getId());
                    return characterEffectRepository.deleteByCharacterId(savedCharacter.getId())
                            .thenMany(Flux.fromIterable(character.getSpellEffects()))
                            .doOnNext(effect -> {
                                effect.setId(null);
                                effect.setCharacterId(savedCharacter.getId());
                            })
                            .flatMap(characterEffectRepository::save)
                            .collectList()
                            .doOnNext(savedCharacter::setSpellEffects)
                            .thenReturn(savedCharacter);
                })
                .flatMap(statService::updateCurrentStats)
                .doOnNext(c -> {
                    if (activeMobiles.containsKey(c.getId())) {
                        activeMobiles.put(c.getId(), c);
                    }
                });
    }

    private int rollStat() {
        return random.nextInt(4) + 1;
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ConversationService conversationService;

    public Mono<Void> enterRoom(Mobile character, Long roomId) {
        log.info("Entering room {} for character {}", roomId, character.getName());
        return this.roomService.getRoom(roomId)
                .flatMap(room -> {
                    if (character.getCurrentRoomId() != null) {
                        if (!character.isHidden() && !character.isInvisible()) {
                            this.communicationService.roomMessage(character,
                                    "\n" + character.getName() + " has left the room.");
                        }
                        // Trigger conversation engine in the old room they just left
                        this.conversationService.triggerRoomConversations(character.getCurrentRoomId());
                    }

                    character.setCurrentRoomId(room.getId());
                    return this.save(character)
                            .flatMap(savedChar -> roomService.calculateCurrentLightValue(room).doOnNext(baseLight -> {
                                int light = this.getEffectiveLight(savedChar, baseLight);
                                if (!savedChar.isHidden() && !savedChar.isInvisible()) {
                                    this.communicationService.roomMessage(savedChar,
                                            "\n" + savedChar.getName() + " has entered the room.");
                                }

                                if (light <= 0) {
                                    this.communicationService.sendTextMessage(character,
                                            "\n\nYou have entered " + room.getName() + ".");
                                    this.communicationService.sendTextMessage(character,
                                            "\n\nIt is pitch black. You cannot see anything.\n\n");
                                } else {
                                    this.communicationService.sendTextMessage(character,
                                            "\n\nYou have entered " + room.getName() + ".");
                                    
                                    if (light >= 5) {
                                        this.communicationService.sendTextMessage(character,
                                                "\n\n" + room.getDescription() + "\n\n");
                                    }

                                    if (light == 1) {
                                        long othersCount = this.findAllByRoomId(room.getId()).stream()
                                                .filter(m -> !m.getId().equals(character.getId()) && !m.isHidden() && !m.isInvisible()).count();
                                        boolean hasItems = !room.getItemIds().isEmpty() || !this.roomService.getTransientItemsInRoom(room.getId()).isEmpty();
                                        
                                        if (othersCount > 0 || hasItems) {
                                            this.communicationService.sendTextMessage(character, "\n\nYou sense something present in the darkness.");
                                        } else {
                                            this.communicationService.sendTextMessage(character, "\n\nIt is too dark to make out any details.");
                                        }
                                    } else if (light > 1) {
                                        this.findAllByRoomId(room.getId()).stream()
                                                .filter(m -> !m.getId().equals(character.getId()) && !m.isHidden() && !m.isInvisible())
                                                .forEach(m -> {
                                                    if (light >= 7) {
                                                        this.communicationService.sendTextMessage(character,
                                                                "\nYou see " + m.getName() + " here.");
                                                        if (m.getStoreId() != null) {
                                                            this.communicationService.sendTextMessage(character,
                                                                    "\n" + m.getName() + " appears to be running a store.");
                                                        }
                                                    } else {
                                                        this.communicationService.sendTextMessage(character,
                                                                "\nYou see a shadowy creature here.");
                                                    }
                                                });

                                        if (light >= 7) {
                                            room.getItemIds().stream().forEach(itemId -> {
                                                this.itemService.getItem(itemId)
                                                        .doOnNext(item -> this.communicationService.sendTextMessage(character,
                                                                "\nYou see " + item.getName() + " laying here."))
                                                        .subscribe();
                                            });
                                            this.roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                                    this.communicationService.sendTextMessage(character, "\nYou see " + item.getName() + " laying here."));
                                        } else {
                                            room.getItemIds().forEach(itemId -> {
                                                this.communicationService.sendTextMessage(character, "\nYou see some sort of item laying here.");
                                            });
                                            this.roomService.getTransientItemsInRoom(room.getId()).forEach(item ->
                                                this.communicationService.sendTextMessage(character, "\nYou see some sort of item laying here."));
                                        }
                                    }
                                }

                                this.conversationService.triggerRoomConversations(room.getId());

                                // Process Faction Aggressiveness
                                java.util.List<Mobile> targets = new ArrayList<>(this.findAllByRoomId(room.getId()));
                                targets.addAll(this.findAllByRoomId(room.getId()));
                                for (Mobile res : targets) {
                                    if (res.getId().equals(character.getId()) || res.isHidden() || res.isInvisible()
                                            || character.isHidden() || character.isInvisible())
                                        continue;

                                    // If character hates resident
                                    if (this.factionService.getFactionRatingSync(character, res.getFactionId()) < 20
                                            && character.getTarget() == null) {
                                        if (this.setTarget(character, res)) {
                                            this.communicationService.roomMessage(character, "\n" + character.getName()
                                                    + " attacks " + res.getName() + " on sight!");
                                            this.communicationService.sendTextMessage(character,
                                                    "\n\nYou attack " + res.getName() + " on sight!");
                                        }
                                    }
                                    // If resident hates character
                                    if (this.factionService.getFactionRatingSync(res, character.getFactionId()) < 20
                                            && res.getTarget() == null) {
                                        if (this.setTarget(res, character)) {
                                            this.communicationService.roomMessage(res,
                                                    "\n" + res.getName() + " attacks "
                                                            + character.getName() + " on sight!");
                                            this.communicationService.sendTextMessage(character,
                                                    "\n\n" + res.getName() + " attacks you on sight!");
                                        }
                                    }
                                }

                                if (light >= 4) {
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
                                    this.communicationService.sendTextMessage(character,
                                            "\n\nExits: " + String.join(", ", exits));
                                }
                            }))
                            .then();
                });
    }

    public int getEffectiveLight(Mobile character, int roomLight) {
        int effectiveLight = roomLight;
        if (character.getSpellEffects() != null) {
            for (io.nadia.ai.aimud.model.CharacterEffect ce : character.getSpellEffects()) {
                if (ce.getEffect() != null) {
                    if (ce.getEffect().getEffectType() == io.nadia.ai.aimud.types.EffectType.DARKVISION) {
                        effectiveLight += ce.getEffect().getModifier1();
                    } else if (ce.getEffect().getEffectType() == io.nadia.ai.aimud.types.EffectType.DARKNESS) {
                        effectiveLight -= ce.getEffect().getModifier1();
                    }
                }
            }
        }
        return Math.max(0, effectiveLight);
    }

    public Flux<Mobile> getAllMobiles() {
        log.info("Fetching all mobiles");
        return mobileRepository.findByUserIdIsNull().cache();
    }

    /**
     * Retrieves a mobile by its ID from the database or cache.
     *
     * @param id the ID of the mobile
     * @return a {@link Mono} containing the mobile
     */
    @Cacheable(value = "mobile", key = "#id")
    public Mono<Mobile> getMobile(Long id) {
        log.info("Fetching mobile with id: {}", id);
        return mobileRepository.findById(id).cache();
    }

    /**
     * Saves a mobile entity to the database and updates it in memory if active.
     *
     * @param mobile the mobile to save
     * @return a {@link Mono} containing the saved mobile
     */
    @CacheEvict(value = {"mobiles", "mobile", "mobiles"}, allEntries = true)
    public Mono<Mobile> saveMobile(Mobile mobile) {
        log.info("Saving mobile: {} (id: {})", mobile.getName(), mobile.getId());
        return mobileRepository.save(mobile)
                .flatMap(saved -> {
                    // Update in-memory if it's currently active
                    if (saved.getId() != null && activeMobiles.containsKey(saved.getId())) {
                        activeMobiles.put(saved.getId(), saved);
                    }
                    if (saved.getRaceId() == null) return Mono.just(saved);
                    
                    return databaseClient.sql("SELECT starting_effects FROM races WHERE id = :rId")
                            .bind("rId", saved.getRaceId())
                            .map((row, meta) -> {
                                String val = row.get(0, String.class);
                                return val == null ? "" : val;
                            })
                            .one()
                            .flatMap(effs -> {
                                if (effs.isEmpty()) return Mono.just(saved);
                                java.util.List<Long> ids = new java.util.ArrayList<>();
                                for (String s : effs.split(",")) {
                                    try { ids.add(Long.parseLong(s.trim())); } catch (Exception ignored) {}
                                }
                                return Flux.fromIterable(ids)
                                        .flatMap(effId -> databaseClient.sql("INSERT INTO character_effects (character_id, effect_id, tick_count, created_at, modified_at, created_by, modified_by) " +
                                                "SELECT :cid, :eid, -1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system' " +
                                                "WHERE NOT EXISTS (SELECT 1 FROM character_effects WHERE character_id = :cid AND effect_id = :eid AND tick_count = -1)")
                                                .bind("cid", saved.getId())
                                                .bind("eid", effId)
                                                .fetch().rowsUpdated())
                                        .then(Mono.just(saved));
                            })
                            .defaultIfEmpty(saved);
                });
    }

    /**
     * Deletes a mobile entity from the database and removes it from active memory.
     *
     * @param id the ID of the mobile to delete
     * @return a {@link Mono} indicating completion
     */
    @CacheEvict(value = {"mobiles", "mobile", "mobiles"}, allEntries = true)
    public Mono<Void> deleteMobile(Long id) {
        log.info("Deleting mobile with id: {}", id);
        return mobileRepository.deleteById(id)
                .doOnSuccess(v -> activeMobiles.remove(id));
    }

    /**
     * Retrieves all mobiles assigned to a specific room from the database.
     *
     * @param roomId the ID of the room
     * @return a {@link Flux} emitting mobiles located in the room
     */
    @Cacheable(value = "mobiles")
    public Flux<Mobile> getMobilesByRoom(Long roomId) {
        return mobileRepository.findByCurrentRoomIdAndUserIdIsNull(roomId);
    }

    /**
     * Scans the room for assigned mobile IDs, dropping them into memory and 
     * caching them with all stats and actions if not already present.
     *
     * @param room the room to spawn mobiles for
     */
    public void spawnMobilesForRoom(Room room) {
        log.info("Spawning mobiles for room: {} (id: {})", room.getName(), room.getId());
        this.getMobilesByRoom(room.getId()).subscribe(mobile -> {
            if (!activeMobiles.containsKey(mobile.getId())) {
                log.info("Spawning mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
                // Ensure the mobile knows which room it is in
                mobile.setCurrentRoomId(room.getId());
                mobile.setUserId(null);
                statService.updateMobileStats(mobile);
                
                // Fetch AI actions into transient list
                getMobileActions(mobile.getId()).collectList().subscribe(actions -> {
                    mobile.setActions(actions);
                    activeMobiles.put(mobile.getId(), mobile);
                    log.info("Spawned mobile: {} (id: {}) into room {}", mobile.getName(), mobile.getId(), room.getId());
                });
            } else {
                log.info("Mobile already spawned in room: {} (id: {})", room.getName(), room.getId());
            }
        });
    }

    /**
     * Gets all active (spawned, in-memory) mobiles currently located in a specific room.
     *
     * @param roomId the ID of the room
     * @return a list of active mobiles in the room
     */

    public Flux<MobileSkill> getMobileSkills(Long mobileId) {
        log.info("Fetching skills for mobile: {}", mobileId);
        return mobileSkillRepository.findByMobileId(mobileId);
    }

    /**
     * Assigns or updates a skill to a mobile with a specific rank.
     *
     * @param mobileId  the ID of the mobile
     * @param skillName the name of the skill
     * @param rank      the rank of the skill
     * @return a {@link Mono} containing the updated or created mobile skill
     */
    public Mono<MobileSkill> assignSkill(Long mobileId, String skillName, int rank) {
        log.info("Assigning skill '{}' rank {} to mobile {}", skillName, rank, mobileId);
        return databaseClient.sql(
                        "INSERT INTO mobile_skills (mobile_id, name, rank) VALUES (:mobileId, :name, :rank) " +
                                "ON CONFLICT (mobile_id, name) DO UPDATE SET rank = EXCLUDED.rank")
                .bind("mobileId", mobileId)
                .bind("name", skillName)
                .bind("rank", rank)
                .fetch().rowsUpdated()
                .then(mobileSkillRepository.findByMobileIdAndName(mobileId, skillName));
    }

    // --- ACTION MANAGEMENT ---

    /**
     * Retrieves the AI actions configured for a mobile.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting the mobile's available actions
     */
    public Flux<MobileAction> getMobileActions(Long mobileId) {
        log.info("Fetching actions for mobile: {}", mobileId);
        return mobileActionRepository.findByMobileId(mobileId);
    }

    /**
     * Saves a complete list of AI actions for a mobile, replacing existing ones.
     *
     * @param mobileId the ID of the mobile
     * @param actions  the list of actions to save
     * @return a {@link Flux} emitting the saved actions
     */
    public Flux<MobileAction> saveMobileActions(Long mobileId, List<MobileAction> actions) {
        log.info("Saving {} actions for mobile {}", actions.size(), mobileId);
        return databaseClient.sql("DELETE FROM mobile_actions WHERE mobile_id = :mobileId")
                .bind("mobileId", mobileId)
                .fetch().rowsUpdated()
                .thenMany(Flux.fromIterable(actions)
                        .flatMap(action -> {
                            action.setMobileId(mobileId);
                            action.setId(null); // Ensure fresh insert
                            return mobileActionRepository.save(action);
                        }));
    }

    // --- INVENTORY MANAGEMENT ---

    /**
     * Retrieves the inventory items for a mobile, populating their total counts.
     *
     * @param mobileId the ID of the mobile
     * @return a {@link Flux} emitting inventory items
     */

    public Mono<Mobile> addItemToInventory(Long mobileId, Long itemId) {
        return mobileRepository.findById(mobileId)
                .flatMap(mobile -> databaseClient.sql("INSERT INTO character_inventory (character_id, item_id, item_count) VALUES (:mobileId, :itemId, 1)")
                        .bind("mobileId", mobileId)
                        .bind("itemId", itemId)
                        .fetch().rowsUpdated()
                        .thenReturn(mobile));
    }

    /**
     * Reloads all NPCs from the database, removing existing active ones first.
     *
     * @return a Mono indicating completion
     */
    @CacheEvict(value = {"mobiles", "mobile"}, allEntries = true)
    public Mono<Void> reloadAllNPCs() {
        log.info("Reloading all NPCs from database");
        activeMobiles.entrySet().removeIf(entry -> entry.getValue().getUserId() == null);
        return roomService.getAllRooms()
                .doOnNext(this::spawnMobilesForRoom)
                .then();
    }

    /**
     * Reloads NPCs for a specific room.
     *
     * @param roomId the ID of the room
     * @return a Mono indicating completion
     */
    public Mono<Void> reloadRoomNPCs(Long roomId) {
        log.info("Reloading NPCs for room {}", roomId);
        activeMobiles.entrySet().removeIf(entry -> 
                entry.getValue().getUserId() == null && entry.getValue().getCurrentRoomId() != null && entry.getValue().getCurrentRoomId().equals(roomId));
        return roomService.getRoom(roomId)
                .doOnNext(this::spawnMobilesForRoom)
                .then();
    }
}
