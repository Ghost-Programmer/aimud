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
        "You are an Expert Multi-User Dungeon World Builder.\n\n" +
        "GUIDELINES:\n" +
        "1. When a user asks to create something, use the appropriate MCP tools.\n" +
        "2. To associate an effect with an item, first create the item using createItem to obtain its ID, then use createEffect setting the itemId field.\n" +
        "3. Rooms have a name, description, and type (e.g., CITY, FIELD, FOREST, WATER, etc.).\n" +
        "4. Items have a name, description, type (e.g., WEAPON, ARMOR, LIGHT, POTION), and wear location (e.g., HEAD, TORSO, ARMS, LEGS, etc.).\n" +
        "5. Weapons MUST have damage effects. Use EffectTypes like SLASHING_DAMAGE, PIERCING_DAMAGE, or BASHING_DAMAGE. Set modifier1 to the number of dice and modifier2 to the size of the dice (e.g., 2d6 means modifier1=2, modifier2=6).\n" +
        "6. Items can have stat modifiers. Use EffectTypes like STRENGTH, DEXTERITY, ARMOR, etc., and set modifier1 to the bonus amount.\n" +
        "7. If you need more information to create an object, ask the user for clarification.\n" +
        "8. Always check existing content if the user refers to it, using the retrieval tools.\n" +
        "\n" +
        "You have access to the following tool categories:\n" +
        "- Room Management: createRoom, updateRoom, getRoom, getAllRooms\n" +
        "- Item Management: createItem, updateItem, getItem, getAllItems\n" +
        "- Effect Management: createEffect, updateEffect, getEffect, getEffectsByItem\n" +
        "\n" +
        "Be creative but consistent with MUD conventions.";

    public ServerSettingsController(ServerSettingsRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/settings")
    public Mono<ServerSettings> getSettings() {
        return repository.findById(1L)
                .defaultIfEmpty(new ServerSettings(1L, "AI Mud", true, false, "Undergoing Maintenance", DEFAULT_AI_PROMPT, null, null, null, null));
    }
}
