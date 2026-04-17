package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * Enumeration mapping out standard bodily equipment slots.
 * Designates precisely where an Item can be equipped by a Mobile.
 */
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

    /**
     * Constructs a WearLocation.
     *
     * @param label the visual text label
     */
    WearLocation(String label) {
        this.label = label;
    }

    /**
     * Deserialization fallback function converting arbitrary string content into WearLocations.
     *
     * @param value the input string
     * @return the translated enumeration, or {@link #NONE} if unrecognized
     */
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

    /**
     * Gets the serialized label representation.
     *
     * @return the string output
     */
    @JsonValue
    public String getLabel() {
        return label;
    }
}
