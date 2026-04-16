package io.nadia.ai.aimud.model;

import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdditionalModelEntitiesTest {

    @Test
    void effect_ConvenienceConstructorAssignsAllFields() {
        Effect effect = new Effect(EffectType.STRENGTH, 1, 2, 3, 4);

        assertThat(effect.getEffectType()).isEqualTo(EffectType.STRENGTH);
        assertThat(effect.getModifier1()).isEqualTo(1);
        assertThat(effect.getModifier2()).isEqualTo(2);
        assertThat(effect.getModifier3()).isEqualTo(3);
        assertThat(effect.getModifier4()).isEqualTo(4);
    }

    @Test
    void item_ConvenienceConstructorAssignsCoreFields() {
        Item item = new Item(ItemType.WEAPON, WearLocation.PRIMARY, "Sword", "Sharp blade");

        assertThat(item.getItemType()).isEqualTo(ItemType.WEAPON);
        assertThat(item.getWearLocation()).isEqualTo(WearLocation.PRIMARY);
        assertThat(item.getName()).isEqualTo("Sword");
        assertThat(item.getDescription()).isEqualTo("Sharp blade");
    }

    @Test
    void item_DefaultValuesAreInitialised() {
        Item item = new Item();

        assertThat(item.getEffects()).isNotNull().isEmpty();
        assertThat(item.getInventory()).isNotNull().isEmpty();
        assertThat(item.getCount()).isEqualTo(1);
        assertThat(item.isNoPickup()).isFalse();
        assertThat(item.isStackable()).isFalse();
        assertThat(item.getProperty1()).isZero();
        assertThat(item.getProperty2()).isZero();
        assertThat(item.getProperty3()).isZero();
        assertThat(item.getProperty4()).isZero();
        assertThat(item.getValue()).isZero();
    }

    @Test
    void mobileAction_SettersAndGettersRoundTripFields() {
        MobileAction action = new MobileAction();
        action.setId(1L);
        action.setMobileId(2L);
        action.setActionCommand("growl");
        action.setDescription("A threatening growl");

        assertThat(action.getId()).isEqualTo(1L);
        assertThat(action.getMobileId()).isEqualTo(2L);
        assertThat(action.getActionCommand()).isEqualTo("growl");
        assertThat(action.getDescription()).isEqualTo("A threatening growl");
    }

    @Test
    void mobileFaction_AllArgsConstructorAssignsFields() {
        MobileFaction mobileFaction = new MobileFaction(10L, 20L, 75);

        assertThat(mobileFaction.getMobileId()).isEqualTo(10L);
        assertThat(mobileFaction.getFactionId()).isEqualTo(20L);
        assertThat(mobileFaction.getRating()).isEqualTo(75);
    }

    @Test
    void mobileFaction_NoArgsConstructorAllowsMutation() {
        MobileFaction mobileFaction = new MobileFaction();
        mobileFaction.setMobileId(11L);
        mobileFaction.setFactionId(22L);
        mobileFaction.setRating(60);

        assertThat(mobileFaction.getMobileId()).isEqualTo(11L);
        assertThat(mobileFaction.getFactionId()).isEqualTo(22L);
        assertThat(mobileFaction.getRating()).isEqualTo(60);
    }

    @Test
    void mobileMacro_SettersAndGettersRoundTripFields() {
        MobileMacro macro = new MobileMacro();
        macro.setId(1L);
        macro.setMobileId(2L);
        macro.setMacroIndex(3);
        macro.setLabel("Attack");
        macro.setCommand("kill goblin");

        assertThat(macro.getId()).isEqualTo(1L);
        assertThat(macro.getMobileId()).isEqualTo(2L);
        assertThat(macro.getMacroIndex()).isEqualTo(3);
        assertThat(macro.getLabel()).isEqualTo("Attack");
        assertThat(macro.getCommand()).isEqualTo("kill goblin");
    }

    @Test
    void mobileSkill_BuilderAssignsFields() {
        MobileSkill mobileSkill = MobileSkill.builder()
                .id(1L)
                .mobileId(2L)
                .name("Bash")
                .rank(4)
                .build();

        assertThat(mobileSkill.getId()).isEqualTo(1L);
        assertThat(mobileSkill.getMobileId()).isEqualTo(2L);
        assertThat(mobileSkill.getName()).isEqualTo("Bash");
        assertThat(mobileSkill.getRank()).isEqualTo(4);
    }

    @Test
    void mobileSkill_AllArgsConstructorAssignsFields() {
        MobileSkill mobileSkill = new MobileSkill(5L, 6L, "Parry", 7);

        assertThat(mobileSkill.getId()).isEqualTo(5L);
        assertThat(mobileSkill.getMobileId()).isEqualTo(6L);
        assertThat(mobileSkill.getName()).isEqualTo("Parry");
        assertThat(mobileSkill.getRank()).isEqualTo(7);
    }
}

