package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WearLocationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void fromString_MatchesEnumNameCaseInsensitively() {
        assertThat(WearLocation.fromString("head")).isEqualTo(WearLocation.HEAD);
        assertThat(WearLocation.fromString("OFFHAND")).isEqualTo(WearLocation.OFFHAND);
    }

    @Test
    void fromString_MatchesHumanLabelCaseInsensitively() {
        assertThat(WearLocation.fromString("Head")).isEqualTo(WearLocation.HEAD);
        assertThat(WearLocation.fromString("Offhand")).isEqualTo(WearLocation.OFFHAND);
    }

    @Test
    void fromString_ReturnsNoneForNullBlankAndInvalidValues() {
        assertThat(WearLocation.fromString(null)).isEqualTo(WearLocation.NONE);
        assertThat(WearLocation.fromString("")).isEqualTo(WearLocation.NONE);
        assertThat(WearLocation.fromString("   ")).isEqualTo(WearLocation.NONE);
        assertThat(WearLocation.fromString("shoulder")).isEqualTo(WearLocation.NONE);
    }

    @Test
    void fromString_DoesNotTrimPaddedValues() {
        assertThat(WearLocation.fromString(" Head ")).isEqualTo(WearLocation.NONE);
    }

    @Test
    void getLabel_ReturnsHumanReadableLabel() {
        assertThat(WearLocation.PRIMARY.getLabel()).isEqualTo("Primary");
        assertThat(WearLocation.FINGER.getLabel()).isEqualTo("Finger");
    }

    @Test
    void jsonSerialization_UsesLabel() throws Exception {
        String json = objectMapper.writeValueAsString(WearLocation.PRIMARY);
        assertThat(json).isEqualTo("\"Primary\"");
    }

    @Test
    void jsonDeserialization_AcceptsEnumNameAndLabel() throws Exception {
        assertThat(objectMapper.readValue("\"PRIMARY\"", WearLocation.class)).isEqualTo(WearLocation.PRIMARY);
        assertThat(objectMapper.readValue("\"Primary\"", WearLocation.class)).isEqualTo(WearLocation.PRIMARY);
    }
}

