package com.aimud.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

public enum EffectType {
    BASHING_DAMAGE("Bashing Damage", "Number of Dice", "Size of the Dice", null, null),
    PIERCING_DAMAGE("Piercing Damage", "Number of Dice", "Size of the Dice", null, null),
    STRENGTH("Strength", "Amount", null, null, null),
    DEXTERITY("Dexterity", "Amount", null, null, null),
    CONSTITUTION("Constitution", "Amount", null, null, null),
    INTELLIGENCE("Intelligence", "Amount", null, null, null),
    WISDOM("Wisdom", "Amount", null, null, null),
    CHARISMA("Charisma", "Amount", null, null, null),
    ARMOR("Armor", "Amount", null, null, null),
    HP_REGEN("HP Regen", "Amount", null, null, null),
    MANA_REGEN("Mana Regen", "Amount", null, null, null),
    PHYSICAL_ATTACK("Physical Attack", "Amount", null, null, null),
    MAGIC_ATTACK("Magic Attack", "Amount", null, null, null),
    MAGIC_RESIST("Magic Resist", "Amount", null, null, null),
    PHYSICAL_RESIST("Physical Resist", "Amount", null, null, null),
    DODGE("Dodge", "Amount", null, null, null),
    SLASHING_DAMAGE("Slashing Damage", "Number of Dice", "Size of the Dice", null, null),
    CRITICAL_HIT("Critical Hit", "Amount", null, null, null),
    FIRE_DAMAGE("Fire Damage", "Number of Dice", "Size of the Dice", null, null),
    COLD_DAMAGE("Cold Damage", "Number of Dice", "Size of the Dice", null, null),
    SONIC_DAMAGE("Sonic Damage", "Number of Dice", "Size of the Dice", null, null),
    POISON_DAMAGE("Poison Damage", "Number of Dice", "Size of the Dice", null, null),
    ELECTRICAL_DAMAGE("Electrical Damage", "Number of Dice", "Size of the Dice", null, null),
    FLY("Fly", null, null, null, null),
    WATER_BREATHING("Water Breathing", null, null, null, null),
    INVISIBLE("Invisible", null, null, null, null),
    UNKNOWN("Unknown", null, null, null, null);

    private final String label;
    @Getter
    private final String modifier1Name;
    @Getter
    private final String modifier2Name;
    @Getter
    private final String modifier3Name;
    @Getter
    private final String modifier4Name;

    EffectType(String label, String modifier1Name, String modifier2Name, String modifier3Name, String modifier4Name) {
        this.label = label;
        this.modifier1Name = modifier1Name;
        this.modifier2Name = modifier2Name;
        this.modifier3Name = modifier3Name;
        this.modifier4Name = modifier4Name;
    }

    @JsonCreator
    public static EffectType fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return Stream.of(EffectType.values())
                .filter(et -> et.name().equalsIgnoreCase(value) || et.getLabel().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

}
