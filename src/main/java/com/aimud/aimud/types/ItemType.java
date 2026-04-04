package com.aimud.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum ItemType {
    WEAPON("Weapon"),
    TWO_HANDED_WEAPON("Two Handed Weapon"),
    RANGED_WEAPON("Ranged Weapon"),
    LIGHT_ARMOR("Light Armor"),
    MEDIUM_ARMOR("Medium Armor"),
    HEAVY_ARMOR("Heavy Armor"),
    FOOD("Food"),
    DRINK("Drink"),
    POTION("Potion"),
    BOOK("Book"),
    SCROLL("Scroll"),
    MONEY("Money"),
    WAND("Wand"),
    QUEST("Quest"),
    KEY("Key"),
    LIGHT("Light"),
    CONTAINER("Container"),
    TRASH("Trash"),
    MISC("Miscellaneous"),
    CORPSE("Corpse"),
    NONE("None");

    private final String label;

    ItemType(String label) {
        this.label = label;
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
