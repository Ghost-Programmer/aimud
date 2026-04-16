package io.nadia.ai.aimud.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nadia.ai.aimud.types.EffectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MobileTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void addHate_IgnoresNullAttacker() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);

        mobile.addHate(null, 10);

        assertThat(mobile.getHateList()).isEmpty();
    }

    @Test
    void addHate_IgnoresSelfTarget() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);

        mobile.addHate(1L, 10);

        assertThat(mobile.getHateList()).isEmpty();
    }

    @Test
    void addHate_AddsAndAccumulatesHate() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);

        mobile.addHate(2L, 10);
        mobile.addHate(2L, 5);
        mobile.addHate(3L, 7);

        assertThat(mobile.getHateList()).containsEntry(2L, 15).containsEntry(3L, 7);
    }

    @Test
    void removeHate_IgnoresNullAndRemovesExistingEntry() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.addHate(2L, 10);
        mobile.addHate(3L, 20);

        mobile.removeHate(null);
        assertThat(mobile.getHateList()).containsKeys(2L, 3L);

        mobile.removeHate(2L);
        assertThat(mobile.getHateList()).doesNotContainKey(2L).containsKey(3L);
    }

    @Test
    void getHighestHateTargetId_ReturnsNullForEmptyList() {
        Mobile mobile = new Mobile();
        assertThat(mobile.getHighestHateTargetId()).isNull();
    }

    @Test
    void getHighestHateTargetId_ReturnsEntryWithHighestValue() {
        Mobile mobile = new Mobile();
        mobile.setId(1L);
        mobile.addHate(2L, 5);
        mobile.addHate(3L, 25);
        mobile.addHate(4L, 10);

        assertThat(mobile.getHighestHateTargetId()).isEqualTo(3L);
    }

    @Test
    void equipmentSetter_SynchronizesHeadAndHeadId() {
        Mobile mobile = new Mobile();
        Item item = new Item();
        item.setId(100L);

        mobile.setHead(item);
        assertThat(mobile.getHead()).isSameAs(item);
        assertThat(mobile.getHeadId()).isEqualTo(100L);

        mobile.setHead(null);
        assertThat(mobile.getHead()).isNull();
        assertThat(mobile.getHeadId()).isNull();
    }

    @Test
    void equipmentSetter_SynchronizesPrimaryAndPrimaryId() {
        Mobile mobile = new Mobile();
        Item weapon = new Item();
        weapon.setId(200L);

        mobile.setPrimary(weapon);
        assertThat(mobile.getPrimary()).isSameAs(weapon);
        assertThat(mobile.getPrimaryId()).isEqualTo(200L);

        mobile.setPrimary(null);
        assertThat(mobile.getPrimary()).isNull();
        assertThat(mobile.getPrimaryId()).isNull();
    }

    @Test
    void equipmentSetter_SynchronizesOffhandAndOffhandId() {
        Mobile mobile = new Mobile();
        Item offhand = new Item();
        offhand.setId(300L);

        mobile.setOffhand(offhand);
        assertThat(mobile.getOffhand()).isSameAs(offhand);
        assertThat(mobile.getOffhandId()).isEqualTo(300L);
    }

    @Test
    void isHidden_ReturnsFalseWhenSpellEffectsNull() {
        Mobile mobile = new Mobile();
        mobile.setSpellEffects(null);

        assertThat(mobile.isHidden()).isFalse();
    }

    @Test
    void isHidden_ReturnsTrueWhenAnySpellEffectIsHidden() {
        Mobile mobile = new Mobile();
        CharacterEffect hiddenEffect = new CharacterEffect();
        hiddenEffect.setEffect(new Effect(EffectType.HIDDEN, 0, 0, 0, 0));

        mobile.getSpellEffects().add(hiddenEffect);

        assertThat(mobile.isHidden()).isTrue();
    }

    @Test
    void isHidden_ReturnsFalseWhenEffectsDoNotContainHidden() {
        Mobile mobile = new Mobile();
        CharacterEffect visibleEffect = new CharacterEffect();
        visibleEffect.setEffect(new Effect(EffectType.STRENGTH, 1, 0, 0, 0));

        mobile.getSpellEffects().add(visibleEffect);

        assertThat(mobile.isHidden()).isFalse();
    }

    @Test
    void isInvisible_ReturnsFalseWhenSpellEffectsNull() {
        Mobile mobile = new Mobile();
        mobile.setSpellEffects(null);

        assertThat(mobile.isInvisible()).isFalse();
    }

    @Test
    void isInvisible_ReturnsTrueWhenAnySpellEffectIsInvisible() {
        Mobile mobile = new Mobile();
        CharacterEffect invisibleEffect = new CharacterEffect();
        invisibleEffect.setEffect(new Effect(EffectType.INVISIBLE, 0, 0, 0, 0));

        mobile.getSpellEffects().add(invisibleEffect);

        assertThat(mobile.isInvisible()).isTrue();
    }

    @Test
    void isInvisible_ReturnsFalseWhenEffectsDoNotContainInvisible() {
        Mobile mobile = new Mobile();
        CharacterEffect otherEffect = new CharacterEffect();
        otherEffect.setEffect(new Effect(EffectType.HIDDEN, 0, 0, 0, 0));

        mobile.getSpellEffects().add(otherEffect);

        assertThat(mobile.isInvisible()).isFalse();
    }

    @Test
    void jsonSerialization_IgnoresTargetField() throws Exception {
        Mobile attacker = new Mobile();
        attacker.setId(1L);
        attacker.setName("Attacker");

        Mobile target = new Mobile();
        target.setId(2L);
        target.setName("Target");

        attacker.setTarget(target);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(attacker));
        assertThat(json.has("target")).isFalse();
    }
}

