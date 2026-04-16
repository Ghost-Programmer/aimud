package io.nadia.ai.aimud.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTypeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void fromString_MatchesEnumNameCaseInsensitively() {
        assertThat(ItemType.fromString("weapon")).isEqualTo(ItemType.WEAPON);
        assertThat(ItemType.fromString("LIGHT_ARMOR")).isEqualTo(ItemType.LIGHT_ARMOR);
    }

    @Test
    void fromString_MatchesHumanLabelCaseInsensitively() {
        assertThat(ItemType.fromString("Weapon")).isEqualTo(ItemType.WEAPON);
        assertThat(ItemType.fromString("Light Armor")).isEqualTo(ItemType.LIGHT_ARMOR);
        assertThat(ItemType.fromString("Miscellaneous")).isEqualTo(ItemType.MISC);
    }

    @Test
    void fromString_ReturnsNoneForNullBlankAndInvalidValues() {
        assertThat(ItemType.fromString(null)).isEqualTo(ItemType.NONE);
        assertThat(ItemType.fromString("")).isEqualTo(ItemType.NONE);
        assertThat(ItemType.fromString("   ")).isEqualTo(ItemType.NONE);
        assertThat(ItemType.fromString("not-a-real-item-type")).isEqualTo(ItemType.NONE);
    }

    @Test
    void fromString_DoesNotTrimPaddedValues() {
        assertThat(ItemType.fromString(" Weapon ")).isEqualTo(ItemType.NONE);
    }

    @Test
    void getLabel_ReturnsJsonValueLabel() {
        assertThat(ItemType.WEAPON.getLabel()).isEqualTo("Weapon");
        assertThat(ItemType.TWO_HANDED_WEAPON.getLabel()).isEqualTo("Two Handed Weapon");
    }

    @Test
    void propertyMetadata_IsExposedForRepresentativeTypes() {
        assertThat(ItemType.WEAPON.getProperty1Name()).isEqualTo("Damage Dice Count");
        assertThat(ItemType.WEAPON.getProperty2Name()).isEqualTo("Size of Damage Dice");
        assertThat(ItemType.WEAPON.getProperty3Name()).isEqualTo("Bonus Damage");
        assertThat(ItemType.WEAPON.getProperty4Name()).isEqualTo("Weapon Category");

        assertThat(ItemType.TRASH.getProperty1Name()).isNull();
        assertThat(ItemType.TRASH.getProperty2Name()).isNull();
        assertThat(ItemType.TRASH.getProperty3Name()).isNull();
        assertThat(ItemType.TRASH.getProperty4Name()).isNull();
    }

    @Test
    void jsonSerialization_UsesLabel() throws Exception {
        String json = objectMapper.writeValueAsString(ItemType.LIGHT_ARMOR);
        assertThat(json).isEqualTo("\"Light Armor\"");
    }

    @Test
    void jsonDeserialization_AcceptsEnumNameAndLabel() throws Exception {
        assertThat(objectMapper.readValue("\"LIGHT_ARMOR\"", ItemType.class)).isEqualTo(ItemType.LIGHT_ARMOR);
        assertThat(objectMapper.readValue("\"Light Armor\"", ItemType.class)).isEqualTo(ItemType.LIGHT_ARMOR);
    }
}

