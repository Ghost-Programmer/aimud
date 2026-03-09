package com.aimud.aimud.model;

public enum EffectType {
    SLASHING_DAMAGE("Slashing Damage"),
    BASHING_DAMAGE("Bashing Damage"),
    PIERCING_DAMAGE("Piercing Damage"),
    STRENGTH("Strength"),
    DEXTERITY("Dexterity"),
    CONSTITUTION("Constitution"),
    INTELLIGENCE("Intelligence"),
    WISDOM("Wisdom"),
    CHARISMA("Charisma"),
    ARMOR("Armor"),
    HP_REGEN("HP Regen"),
    MANA_REGEN("Mana Regen"),
    PHYSICAL_ATTACK("Physical Attack"),
    MAGIC_ATTACK("Magic Attack"),
    MAGIC_RESIST("Magic Resist"),
    DODGE("Dodge"),
    CRITICAL_HIT("Critical Hit"),
    FIRE_DAMAGE("Fire Damage"),
    COLD_DAMAGE("Cold Damage"),
    SONIC_DAMAGE("Sonic Damage"),
    POISON_DAMAGE("Poison Damage"),
    ELECTRICAL_DAMAGE("Electrical Damage"),
    FLY("Fly"),
    WATER_BREATHING("Water Breathing"),
    INVISIBLE("Invisible");

    private final String label;

    EffectType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
