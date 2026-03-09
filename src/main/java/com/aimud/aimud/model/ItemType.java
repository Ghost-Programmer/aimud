package com.aimud.aimud.model;

public enum ItemType {
    WEAPON("Weapon"),
    TWO_HANDED_WEAPON("Two Handed Weapon"),
    ARMOR("Armor"),
    FOOD("Food"),
    DRINK("Drink"),
    POTION("Potion"),
    SCROLL("Scroll"),
    MONEY("Money"),
    WAND("Wand"),
    QUEST("Quest"),
    KEY("Key"),
    LIGHT("Light"),
    CONTAINER("Container"),
    TRASH("Trash"),
    MISC("Miscellaneous");

    private final String label;

    ItemType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
