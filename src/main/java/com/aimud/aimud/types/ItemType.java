package com.aimud.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

public enum ItemType {
    WEAPON("Weapon", "Damage Dice Count", "Size of Damage Dice", "Bonus Damage", "Weapon Category"),
    TWO_HANDED_WEAPON("Two Handed Weapon", "Damage Dice Count", "Size of Damage Dice", "Bonus Damage", "Weapon Category"),
    RANGED_WEAPON("Ranged Weapon", "Damage Dice Count", "Size of Damage Dice", "Bonus Damage", "Ammunition Type"),
    LIGHT_ARMOR("Light Armor", "Armor Class Value", null, null, null),
    MEDIUM_ARMOR("Medium Armor", "Armor Class Value", null, null, null),
    HEAVY_ARMOR("Heavy Armor", "Armor Class Value", null, null, null),
    FOOD("Food", "Portions / Bites", "Hours Satiated", null, "Poisoned Flag"),
    DRINK("Drink", "Liquid Volume (Sips)", "Hours Quenched", "Liquid Type", "Poisoned Flag"),
    POTION("Potion", "Spell Effect ID 1", "Spell Effect ID 2", "Spell Effect ID 3", "Spell Level"),
    BOOK("Book", "Skill ID to Teach", null, null, null),
    SCROLL("Scroll", "Spell Effect ID", "Required Skill ID", null, null),
    MONEY("Money", "Value in Gold", "Value in Silver", "Value in Copper", null),
    WAND("Wand", "Spell Effect ID", "Max Charges", "Current Charges", null),
    QUEST("Quest", "Quest ID", null, null, null),
    KEY("Key", "Lock ID", null, null, null),
    LIGHT("Light", "Duration (Ticks)", null, null, null),
    CONTAINER("Container", "Max Weight Capacity", "Max Item Count", "Lock ID", "Key ID"),
    TRASH("Trash", null, null, null, null),
    MISC("Miscellaneous", null, null, null, null),
    CORPSE("Corpse", "Decay Timer (Ticks)", "Original Mobile ID", null, null),
    BANDAGE("Bandage", "Healing Dice Count", "Size of Healing Dice", null, null),
    NONE("None", null, null, null, null);

    private final String label;
    @Getter
    private final String property1Name;
    @Getter
    private final String property2Name;
    @Getter
    private final String property3Name;
    @Getter
    private final String property4Name;

    ItemType(String label, String property1Name, String property2Name, String property3Name, String property4Name) {
        this.label = label;
        this.property1Name = property1Name;
        this.property2Name = property2Name;
        this.property3Name = property3Name;
        this.property4Name = property4Name;
    }

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

    @JsonValue
    public String getLabel() {
        return label;
    }
}
