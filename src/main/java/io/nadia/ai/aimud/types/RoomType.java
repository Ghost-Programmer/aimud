package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * Enumeration of general zone/room biomes and settings.
 * Impacts movement logic, rendering, or specific mechanical systems (e.g. outdoors vs indoors).
 */
public enum RoomType {
    INDOORS,
    CITY,
    FIELD,
    FOREST,
    HILLS,
    MOUNTAIN,
    DESERT,
    ARCTIC,
    SWAMP,
    WATER_SURFACE,
    UNDERWATER,
    AIR,
    UNDERGROUND_CAVE,
    UNDERGROUND_DUNGEON,
    UNKNOWN;

    /**
     * Provides a fallback-safe parser to convert a generic string to its exact RoomType.
     *
     * @param value the string text, typically serialized from JSON
     * @return the evaluated RoomType, or {@link #UNKNOWN} if unparseable
     */
    @JsonCreator
    public static RoomType fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return Stream.of(RoomType.values())
                .filter(rt -> rt.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
