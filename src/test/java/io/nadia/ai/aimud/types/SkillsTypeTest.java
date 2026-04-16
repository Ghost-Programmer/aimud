package io.nadia.ai.aimud.types;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class SkillsTypeTest {

    @Test
    void constants_HaveExpectedStableValues() {
        assertThat(SkillsType.CAST_MAGIC).isEqualTo("Cast Magic");
        assertThat(SkillsType.SAY_PRAYER).isEqualTo("Say Prayer");
        assertThat(SkillsType.SING_SONG).isEqualTo("Sing Song");
        assertThat(SkillsType.LIGHT_ARMOR).isEqualTo("Light Armor");
        assertThat(SkillsType.MEDIUM_ARMOR).isEqualTo("Medium Armor");
        assertThat(SkillsType.HEAVY_ARMOR).isEqualTo("Heavy Armor");
        assertThat(SkillsType.ONE_HANDED_WEAPON).isEqualTo("One Handed Weapon");
        assertThat(SkillsType.TWO_HANDED_WEAPON).isEqualTo("Two Handed Weapon");
        assertThat(SkillsType.BASH).isEqualTo("Bash");
        assertThat(SkillsType.BANDAGE).isEqualTo("Bandage");
        assertThat(SkillsType.DUAL_WIELD).isEqualTo("Dual Wield");
        assertThat(SkillsType.PARRY).isEqualTo("Parry");
        assertThat(SkillsType.SHIELD_BLOCK).isEqualTo("Shield Block");
        assertThat(SkillsType.HIDE).isEqualTo("Hide");
        assertThat(SkillsType.BACKSTAB).isEqualTo("Backstab");
        assertThat(SkillsType.PICKPOCKET).isEqualTo("Pickpocket");
        assertThat(SkillsType.DOUBLE_ATTACK).isEqualTo("Double Attack");
        assertThat(SkillsType.TRIPLE_ATTACK).isEqualTo("Triple Attack");
        assertThat(SkillsType.DISARM).isEqualTo("Disarm");
        assertThat(SkillsType.WARCRY).isEqualTo("Warcry");
        assertThat(SkillsType.INTIMIDATE).isEqualTo("Intimidate");
        assertThat(SkillsType.PROVOKE).isEqualTo("Provoke");
        assertThat(SkillsType.TAUNT).isEqualTo("Taunt");
        assertThat(SkillsType.VANISH).isEqualTo("Vanish");
    }

    @Test
    void constructor_IsPrivateUtilityConstructor() throws Exception {
        Constructor<SkillsType> constructor = SkillsType.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();

        constructor.setAccessible(true);
        SkillsType instance = constructor.newInstance();
        assertThat(instance).isNotNull();
    }
}

