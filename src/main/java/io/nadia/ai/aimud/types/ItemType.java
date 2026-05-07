package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enumeration detailing the specific classifications for items within the game
 * world.
 * Contains definitions for property mapping per item category.
 */
public enum ItemType {
    WEAPON("Weapon", 100, "Damage Dice Count", "Size of Damage Dice", "Bonus Damage", "Weapon Category"),
    TWO_HANDED_WEAPON("Two Handed Weapon", 200, "Damage Dice Count", "Size of Damage Dice", "Bonus Damage",
            "Weapon Category"),
    RANGED_WEAPON("Ranged Weapon", 150, "Damage Dice Count", "Size of Damage Dice", "Bonus Damage", "Ammunition Type"),
    LIGHT_ARMOR("Light Armor", 50, "Armor Class Value", null, null, null),
    MEDIUM_ARMOR("Medium Armor", 150, "Armor Class Value", null, null, null),
    HEAVY_ARMOR("Heavy Armor", 300, "Armor Class Value", null, null, null),
    FOOD("Food", 5, "Portions / Bites", "Hours Satiated", null, "Poisoned Flag"),
    DRINK("Drink", 2, "Liquid Volume (Sips)", "Hours Quenched", "Liquid Type", "Poisoned Flag"),
    POTION("Potion", 50, "Spell Effect ID 1", "Spell Effect ID 2", "Duration", "Spell Level"),
    BOOK("Book", 500, "Skill ID to Teach", null, null, null),
    SCROLL("Scroll", 100, "Spell Effect ID", "Required Skill ID", null, null),
    MONEY("Money", 0, "Value in Gold", null, null, null),
    WAND("Wand", 250, "Spell Effect ID", "Max Charges", "Current Charges", null),
    QUEST("Quest", 0, "Quest ID", null, null, null),
    KEY("Key", 10, "Lock ID", null, null, null),
    LIGHT("Light", 20, "Duration (Ticks)", null, null, null),
    CONTAINER("Container", 50, "Max Weight Capacity", "Max Item Count", "Lock ID", "Key ID"),
    TRASH("Trash", 0, null, null, null, null),
    MISC("Miscellaneous", 0, "Value in Gold", null, null, null),
    CORPSE("Corpse", 0, "Decay Timer (Ticks)", "Original Mobile ID", null, null),
    BANDAGE("Bandage", 5, "Healing Dice Count", "Size of Healing Dice", null, null),
    NONE("None", 0, null, null, null, null);

    private final String label;
    @Getter
    private final int baseValue;
    @Getter
    private final String property1Name;
    @Getter
    private final String property2Name;
    @Getter
    private final String property3Name;
    @Getter
    private final String property4Name;

    /**
     * Constructs a new ItemType.
     *
     * @param label         the human-readable type label
     * @param baseValue     the base value in gold for the item type
     * @param property1Name the definition for property slots 1
     * @param property2Name the definition for property slots 2
     * @param property3Name the definition for property slots 3
     * @param property4Name the definition for property slots 4
     */
    ItemType(String label, int baseValue, String property1Name, String property2Name, String property3Name,
            String property4Name) {
        this.label = label;
        this.baseValue = baseValue;
        this.property1Name = property1Name;
        this.property2Name = property2Name;
        this.property3Name = property3Name;
        this.property4Name = property4Name;
    }

    /**
     * Translates a string input into its corresponding ItemType mapping.
     *
     * @param value the string name or label
     * @return the determined ItemType, or {@link #NONE} if missing or unmatched
     */
    @JsonCreator
    public static ItemType fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return Stream.of(ItemType.values())
                .filter(it -> it.name().equalsIgnoreCase(value) || it.getLabel().equalsIgnoreCase(value))
                .findFirst()
                .orElse(NONE);
    }

    /**
     * Retrieves the human-readable string version of this generic item type.
     *
     * @return the textual label
     */
    @JsonValue
    public String getLabel() {
        return label;
    }
}
