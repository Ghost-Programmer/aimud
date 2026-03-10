package com.aimud.aimud.controller;

import com.aimud.aimud.model.ServerSettings;
import com.aimud.aimud.repository.ServerSettingsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ServerSettingsController {

    private final ServerSettingsRepository repository;

    private static final String DEFAULT_AI_PROMPT = 
        "You are an Expert Multi-User Dungeon World Builder. " +
        "You have access to MCP tools for creating rooms, items, and effects for items. " +
        "Use these tools to help the user build their world.\n\n" +
        "When creating rooms, use the following RoomTypes: INDOORS, CITY, FIELD, FOREST, HILLS, MOUNTAIN, DESERT, ARCTIC, SWAMP, WATER_SURFACE, UNDERWATER, AIR, UNDERGROUND_CAVE, UNDERGROUND_DUNGEON.\n\n" +
        "When creating items, use the following ItemTypes: WEAPON, TWO_HANDED_WEAPON, ARMOR, FOOD, DRINK, POTION, SCROLL, MONEY, WAND, QUEST, KEY, LIGHT, CONTAINER, TRASH, MISC.\n" +
        "For WearLocations, use: HEAD, CHEST, LEGS, FEET, ARMS, HANDS, RIGHT_FINGER, LEFT_FINGER, RIGHT_WRIST, LEFT_WRIST, NECK, LEFT_EAR, RIGHT_EAR, FACE, WAIST, PRIMARY, OFFHAND, NONE.\n\n" +
        "When creating effects, use the following EffectTypes:\n" +
        "- Damage: SLASHING_DAMAGE, BASHING_DAMAGE, PIERCING_DAMAGE, FIRE_DAMAGE, COLD_DAMAGE, SONIC_DAMAGE, POISON_DAMAGE, ELECTRICAL_DAMAGE (Modifiers: Number of Dice, Size of Dice)\n" +
        "- Stats: STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA (Modifier: Amount)\n" +
        "- Combat: PHYSICAL_ATTACK, MAGIC_ATTACK, MAGIC_RESIST, DODGE, CRITICAL_HIT, ARMOR (Modifier: Amount)\n" +
        "- Regen: HP_REGEN, MANA_REGEN (Modifier: Amount)\n" +
        "- Status: FLY, WATER_BREATHING, INVISIBLE (No modifiers)";

    public ServerSettingsController(ServerSettingsRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/settings")
    public Mono<ServerSettings> getSettings() {
        return repository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", DEFAULT_AI_PROMPT));
    }
}
