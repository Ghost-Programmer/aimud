package com.aimud.aimud.model;

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
    RIGHT_FINGER("Right Finger"),
    LEFT_FINGER("Left Finger"),
    RIGHT_WRIST("Right Wrist"),
    LEFT_WRIST("Left Wrist"),
    NECK("Neck"),
    LEFT_EAR("Left Ear"),
    RIGHT_EAR("Right Ear"),
    FACE("Face"),
    WAIST("Waist"),
    PRIMARY("Primary"),
    OFFHAND("Offhand"),
    NONE("None");

    private final String label;

    WearLocation(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
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
}
