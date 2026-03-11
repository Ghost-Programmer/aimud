package com.aimud.aimud.service;

import com.aimud.aimud.model.CharacterClass;
import com.aimud.aimud.model.Race;
import com.aimud.aimud.model.ServerSettings;
import com.aimud.aimud.repository.CharacterClassRepository;
import com.aimud.aimud.repository.RaceRepository;
import com.aimud.aimud.repository.ServerSettingsRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ConfigService {

    private final ServerSettingsRepository serverSettingsRepository;
    private final RaceRepository raceRepository;
    private final CharacterClassRepository characterClassRepository;

    private static final String DEFAULT_AI_PROMPT =
            """
                    You are an Expert Multi-User Dungeon World Builder. You have access to MCP tools for creating rooms, items, and effects for items. Use these tools to help the user build their world.
                    
                    GUIDELINES:
                    1. When a user asks to create something, use the appropriate MCP tools.
                    2. To associate an effect with an item, first create the item using createItem to obtain its ID, then use createEffect setting the itemId field.
                    3. Rooms have a name, description, and type (e.g., CITY, FIELD, FOREST, WATER, etc.).
                    4. Items have a name, description, type (e.g., WEAPON, ARMOR, LIGHT, POTION), and wear location (e.g., HEAD, TORSO, ARMS, LEGS, etc.).
                    5. Weapons MUST have damage effects. Use EffectTypes like SLASHING_DAMAGE, PIERCING_DAMAGE, or BASHING_DAMAGE. Set modifier1 to the number of dice and modifier2 to the size of the dice (e.g., 2d6 means modifier1=2, modifier2=6).
                    6. Items can have stat modifiers. Use EffectTypes like STRENGTH, DEXTERITY, ARMOR, etc., and set modifier1 to the bonus amount.
                    7. If you need more information to create an object, ask the user for clarification.
                    8. Always check existing content if the user refers to it, using the retrieval tools.
                    
                    You have access to the following tool categories:
                    - Room Management: createRoom, updateRoom, getRoom, getAllRooms
                    - Item Management: createItem, updateItem, getItem, getAllItems
                    - Effect Management: createEffect, updateEffect, getEffect, getEffectsByItem
                    
                    
                    When creating rooms, use the following RoomTypes: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON.
                    
                    When creating items, use the following ItemTypes: WEAPON, TWO_HANDED_WEAPON, ARMOR, FOOD, DRINK, POTION, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC.
                    For WearLocations, use: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, RIGHT_FINGER, LEFT_FINGER, RIGHT_WRIST, LEFT_WRIST, NECK, LEFT_EAR, RIGHT_EAR, FACE, WAIST, PRIMARY, OFFHAND, NONE.
                    
                    When creating effects, use the following EffectTypes:
                    - Damage: SLASHING_DAMAGE, BASHING_DAMAGE, PIERCING_DAMAGE, FIRE_DAMAGE, COLD_DAMAGE, SONIC_DAMAGE, POISON_DAMAGE, ELECTRICAL_DAMAGE (Modifiers: Number of Dice, Size of Dice)
                    - Stats: STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA (Modifier: Amount)
                    - Combat: PHYSICAL_ATTACK, MAGIC_ATTACK, MAGIC_RESIST, DODGE, CRITICAL_HIT, ARMOR (Modifier: Amount)
                    - Regen: HP_REGEN, MANA_REGEN (Modifier: Amount)
                    - Status: FLY, WATER_BREATHING, INVISIBLE (No modifiers)
                   """;

    public ConfigService(ServerSettingsRepository serverSettingsRepository, RaceRepository raceRepository, CharacterClassRepository characterClassRepository) {
        this.serverSettingsRepository = serverSettingsRepository;
        this.raceRepository = raceRepository;
        this.characterClassRepository = characterClassRepository;
    }

    // Server Settings
    @Cacheable(value = "serverSettings", key = "1")
    public Mono<ServerSettings> getServerSettings() {
        return serverSettingsRepository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", DEFAULT_AI_PROMPT, null, null, null, null));
    }

    @CachePut(value = "serverSettings", key = "1")
    public Mono<ServerSettings> updateServerSettings(ServerSettings settings) {
        return serverSettingsRepository.findById(1L)
                .flatMap(existingSettings -> {
                    ServerSettings settingsToSave = new ServerSettings(
                            1L,
                            settings.serverName(),
                            settings.allowNewUser(),
                            settings.maintenance(),
                            settings.maintenanceText(),
                            settings.aiSystemPrompt(),
                            existingSettings.createdAt(),
                            existingSettings.modifiedAt(),
                            existingSettings.createdBy(),
                            existingSettings.modifiedBy()
                    );
                    return serverSettingsRepository.save(settingsToSave);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    ServerSettings newSettings = new ServerSettings(
                            1L,
                            settings.serverName(),
                            settings.allowNewUser(),
                            settings.maintenance(),
                            settings.maintenanceText(),
                            settings.aiSystemPrompt(),
                            null, null, null, null
                    );
                    return serverSettingsRepository.save(newSettings);
                }));
    }

    // Races
    @Cacheable(value = "races")
    public Flux<Race> getAllRaces() {
        return raceRepository.findAll().filter(race -> !race.isDeleted());
    }

    @CacheEvict(value = "races", allEntries = true)
    public Mono<Race> createRace(Race race) {
        return raceRepository.save(race);
    }

    @CacheEvict(value = "races", allEntries = true)
    public Mono<Race> updateRace(Long id, Race race) {
        return raceRepository.findById(id)
                .flatMap(existingRace -> {
                    existingRace.setName(race.getName());
                    existingRace.setDescription(race.getDescription());
                    existingRace.setStrengthMod(race.getStrengthMod());
                    existingRace.setDexterityMod(race.getDexterityMod());
                    existingRace.setConstitutionMod(race.getConstitutionMod());
                    existingRace.setIntelligenceMod(race.getIntelligenceMod());
                    existingRace.setWisdomMod(race.getWisdomMod());
                    existingRace.setCharismaMod(race.getCharismaMod());
                    return raceRepository.save(existingRace);
                });
    }

    @CacheEvict(value = "races", allEntries = true)
    public Mono<Void> deleteRace(Long id) {
        return raceRepository.findById(id)
                .flatMap(race -> {
                    race.setDeleted(true);
                    return raceRepository.save(race);
                })
                .then();
    }

    // Character Classes
    @Cacheable(value = "characterClasses")
    public Flux<CharacterClass> getAllCharacterClasses() {
        return characterClassRepository.findAll().filter(cc -> !cc.isDeleted());
    }

    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<CharacterClass> createCharacterClass(CharacterClass characterClass) {
        return characterClassRepository.save(characterClass);
    }

    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<CharacterClass> updateCharacterClass(Long id, CharacterClass characterClass) {
        return characterClassRepository.findById(id)
                .flatMap(existingClass -> {
                    existingClass.setName(characterClass.getName());
                    existingClass.setDescription(characterClass.getDescription());
                    existingClass.setStrengthMod(characterClass.getStrengthMod());
                    existingClass.setDexterityMod(characterClass.getDexterityMod());
                    existingClass.setConstitutionMod(characterClass.getConstitutionMod());
                    existingClass.setIntelligenceMod(characterClass.getIntelligenceMod());
                    existingClass.setWisdomMod(characterClass.getWisdomMod());
                    existingClass.setCharismaMod(characterClass.getCharismaMod());
                    return characterClassRepository.save(existingClass);
                });
    }

    @CacheEvict(value = "characterClasses", allEntries = true)
    public Mono<Void> deleteCharacterClass(Long id) {
        return characterClassRepository.findById(id)
                .flatMap(cc -> {
                    cc.setDeleted(true);
                    return characterClassRepository.save(cc);
                })
                .then();
    }
}
