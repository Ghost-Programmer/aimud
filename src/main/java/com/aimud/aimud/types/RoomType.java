package com.aimud.aimud.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;

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
