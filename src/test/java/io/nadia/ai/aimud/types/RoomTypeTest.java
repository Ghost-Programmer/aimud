package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTypeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void fromString_MatchesEnumNameCaseInsensitively() {
        assertThat(RoomType.fromString("indoors")).isEqualTo(RoomType.INDOORS);
        assertThat(RoomType.fromString("UNDERGROUND_DUNGEON")).isEqualTo(RoomType.UNDERGROUND_DUNGEON);
    }

    @Test
    void fromString_ReturnsUnknownForNullBlankAndInvalidValues() {
        assertThat(RoomType.fromString(null)).isEqualTo(RoomType.UNKNOWN);
        assertThat(RoomType.fromString("")).isEqualTo(RoomType.UNKNOWN);
        assertThat(RoomType.fromString("   ")).isEqualTo(RoomType.UNKNOWN);
        assertThat(RoomType.fromString("castle")).isEqualTo(RoomType.UNKNOWN);
    }

    @Test
    void fromString_DoesNotAcceptHumanLabelStyleWithSpaces() {
        assertThat(RoomType.fromString("Water Surface")).isEqualTo(RoomType.UNKNOWN);
        assertThat(RoomType.fromString("Underground Dungeon")).isEqualTo(RoomType.UNKNOWN);
    }

    @Test
    void fromString_DoesNotTrimPaddedValues() {
        assertThat(RoomType.fromString(" INDOORS ")).isEqualTo(RoomType.UNKNOWN);
    }

    @Test
    void jsonDeserialization_AcceptsEnumName() throws Exception {
        assertThat(objectMapper.readValue("\"FOREST\"", RoomType.class)).isEqualTo(RoomType.FOREST);
    }

    @Test
    void jsonDeserialization_InvalidValueFallsBackToUnknown() throws Exception {
        assertThat(objectMapper.readValue("\"not-a-room\"", RoomType.class)).isEqualTo(RoomType.UNKNOWN);
    }
}

