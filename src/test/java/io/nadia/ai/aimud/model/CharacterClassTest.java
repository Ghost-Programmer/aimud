package io.nadia.ai.aimud.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterClassTest {

    @Test
    void constructor_AssignsCoreFields() {
        CharacterClass characterClass = new CharacterClass("Warrior", "Front line fighter", 2, 0, 0, -1, 1, 3);

        assertThat(characterClass.getName()).isEqualTo("Warrior");
        assertThat(characterClass.getDescription()).isEqualTo("Front line fighter");
        assertThat(characterClass.getStrengthMod()).isEqualTo(2);
        assertThat(characterClass.getIntelligenceMod()).isZero();
        assertThat(characterClass.getWisdomMod()).isZero();
        assertThat(characterClass.getCharismaMod()).isEqualTo(-1);
        assertThat(characterClass.getDexterityMod()).isEqualTo(1);
        assertThat(characterClass.getConstitutionMod()).isEqualTo(3);
    }

    @Test
    void getStartingItemIds_ReturnsEmptyListForNullOrEmpty() {
        CharacterClass characterClass = new CharacterClass();

        characterClass.setStartingItems(null);
        assertThat(characterClass.getStartingItemIds()).isEmpty();

        characterClass.setStartingItems("");
        assertThat(characterClass.getStartingItemIds()).isEmpty();
    }

    @Test
    void getStartingItemIds_ParsesCommaSeparatedIdsAndTrimsWhitespace() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setStartingItems("1, 2,3 , 4");

        assertThat(characterClass.getStartingItemIds()).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void getStartingItemIds_IgnoresInvalidTokensAndPreservesValidOrder() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setStartingItems("10, nope, 20, , 30x, 40");

        assertThat(characterClass.getStartingItemIds()).containsExactly(10L, 20L, 40L);
    }

    @Test
    void getStartingItemIds_PreservesDuplicateValues() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setStartingItems("5,5,6");

        assertThat(characterClass.getStartingItemIds()).containsExactly(5L, 5L, 6L);
    }

    @Test
    void getStartingSkillNames_ReturnsEmptyListForNullOrEmpty() {
        CharacterClass characterClass = new CharacterClass();

        characterClass.setStartingSkills(null);
        assertThat(characterClass.getStartingSkillNames()).isEmpty();

        characterClass.setStartingSkills("");
        assertThat(characterClass.getStartingSkillNames()).isEmpty();
    }

    @Test
    void getStartingSkillNames_ParsesAndTrimsSkillNames() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setStartingSkills(" Bash,  Parry ,Dual Wield ");

        assertThat(characterClass.getStartingSkillNames()).containsExactly("Bash", "Parry", "Dual Wield");
    }

    @Test
    void getStartingSkillNames_IgnoresBlankEntries() {
        CharacterClass characterClass = new CharacterClass();
        characterClass.setStartingSkills("Bash, ,   ,Parry");

        assertThat(characterClass.getStartingSkillNames()).containsExactly("Bash", "Parry");
    }
}

