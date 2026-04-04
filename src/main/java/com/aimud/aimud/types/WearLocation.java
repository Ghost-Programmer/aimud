package com.aimud.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum WearLocation {
    HEAD("Head"),
    CHEST("Chest"),
    LEGS("Legs"),
    FEET("Feet"),
    ARMS("Arms"),
    HANDS("Hands"),
    FINGER("Finger"),
    WRIST("Wrist"),
    NECK("Neck"),
    EAR("Ear"),
    FACE("Face"),
    WAIST("Waist"),
    PRIMARY("Primary"),
    OFFHAND("Offhand"),
    NONE("None");

    private final String label;

    WearLocation(String label) {
        this.label = label;
    }

    @JsonCreator
    public static WearLocation fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return Stream.of(WearLocation.values())
                .filter(wl -> wl.name().equalsIgnoreCase(value) || wl.getLabel().equalsIgnoreCase(value))
                .findFirst()
                .orElse(NONE);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
