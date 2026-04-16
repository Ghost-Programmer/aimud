package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EffectTypeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void fromString_MatchesEnumNameCaseInsensitively() {
        assertThat(EffectType.fromString("strength")).isEqualTo(EffectType.STRENGTH);
        assertThat(EffectType.fromString("MAGIC_ATTACK")).isEqualTo(EffectType.MAGIC_ATTACK);
    }

    @Test
    void fromString_MatchesHumanLabelCaseInsensitively() {
        assertThat(EffectType.fromString("Strength")).isEqualTo(EffectType.STRENGTH);
        assertThat(EffectType.fromString("Magic Attack")).isEqualTo(EffectType.MAGIC_ATTACK);
        assertThat(EffectType.fromString("Water Breathing")).isEqualTo(EffectType.WATER_BREATHING);
    }

    @Test
    void fromString_ReturnsUnknownForNullBlankAndInvalidValues() {
        assertThat(EffectType.fromString(null)).isEqualTo(EffectType.UNKNOWN);
        assertThat(EffectType.fromString("")).isEqualTo(EffectType.UNKNOWN);
        assertThat(EffectType.fromString("   ")).isEqualTo(EffectType.UNKNOWN);
        assertThat(EffectType.fromString("unknown-effect-type")).isEqualTo(EffectType.UNKNOWN);
    }

    @Test
    void fromString_DoesNotTrimPaddedValues() {
        assertThat(EffectType.fromString(" Strength ")).isEqualTo(EffectType.UNKNOWN);
    }

    @Test
    void getLabel_ReturnsHumanReadableLabel() {
        assertThat(EffectType.HP_REGEN.getLabel()).isEqualTo("HP Regen");
        assertThat(EffectType.BASHING_DAMAGE.getLabel()).isEqualTo("Bashing Damage");
    }

    @Test
    void modifierMetadata_IsExposedForRepresentativeEffects() {
        assertThat(EffectType.STRENGTH.getModifier1Name()).isEqualTo("Amount");
        assertThat(EffectType.STRENGTH.getModifier2Name()).isNull();
        assertThat(EffectType.BASHING_DAMAGE.getModifier1Name()).isEqualTo("Number of Dice");
        assertThat(EffectType.BASHING_DAMAGE.getModifier2Name()).isEqualTo("Size of the Dice");
        assertThat(EffectType.INVISIBLE.getModifier1Name()).isNull();
    }

    @Test
    void jsonSerialization_UsesLabel() throws Exception {
        String json = objectMapper.writeValueAsString(EffectType.MAGIC_ATTACK);
        assertThat(json).isEqualTo("\"Magic Attack\"");
    }

    @Test
    void jsonDeserialization_AcceptsEnumNameAndLabel() throws Exception {
        assertThat(objectMapper.readValue("\"MAGIC_ATTACK\"", EffectType.class)).isEqualTo(EffectType.MAGIC_ATTACK);
        assertThat(objectMapper.readValue("\"Magic Attack\"", EffectType.class)).isEqualTo(EffectType.MAGIC_ATTACK);
    }
}

