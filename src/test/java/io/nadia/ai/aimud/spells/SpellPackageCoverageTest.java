package io.nadia.ai.aimud.spells;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.*;
import io.nadia.ai.aimud.types.SkillsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpellPackageCoverageTest {

    @Test
    void baseSpell_UtilityMethodsWork() {
        SkillService skillService = mock(SkillService.class);
        TestSpell spell = new TestSpell(skillService,
                mock(MobileService.class),
                mock(CharacterService.class),
                mock(CommunicationService.class),
                mock(EffectService.class));

        Mobile caster = new Mobile();
        Mobile target = new Mobile();
        caster.setTarget(target);

        when(skillService.getSkillRank(eq(caster), eq(SkillsType.CAST_MAGIC))).thenReturn(10);
        when(skillService.getSkillRank(eq(caster), eq("Spell: Test Spell"))).thenReturn(5);

        assertThat(spell.getSpellSkillName()).isEqualTo("Spell: Test Spell");
        assertThat(spell.getManaCost(caster)).isEqualTo(21); // 9 + 5 + (10 - 3)
        assertThat(spell.getDefaultTarget(caster)).isSameAs(target);
        assertThat(spell.getTarget(caster, new String[]{"cast", "test"})).isSameAs(target);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("spellClasses")
    void everyConcreteSpellClass_HasMetadataAndInstantiates(Class<? extends Spell> clazz) {
        Spell spell = instantiate(clazz);

        assertThat(spell.getSpellName()).isNotBlank();
        assertThat(spell.getSpellId()).isNotNull();
        assertThat(spell.getSpellLevel()).isNotNull();
        assertThat(spell.getDescription()).isNotNull();
    }

    @Test
    void everyConcreteSpellClass_IsCoveredByDiscovery() {
        List<Class<? extends Spell>> classes = spellClasses().toList();
        // Defensive floor ensures recursive package scan stays active.
        assertThat(classes).hasSizeGreaterThan(20);
    }

    static Stream<Class<? extends Spell>> spellClasses() {
        java.util.List<Class<? extends Spell>> classes = findClasses("io.nadia.ai.aimud.spells")
                .filter(Spell.class::isAssignableFrom)
                .map(c -> (Class<? extends Spell>) c)
                .filter(c -> c != Spell.class)
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .collect(java.util.stream.Collectors.toList());
        classes.sort(java.util.Comparator.comparing(Class::getName));
        return classes.stream();
    }

    private static Stream<Class<?>> findClasses(String basePackage) {
        try {
            Path root = Paths.get(Spell.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path packagePath = root.resolve(basePackage.replace('.', '/'));
            if (!Files.exists(packagePath)) {
                return Stream.empty();
            }

            return Files.walk(packagePath)
                    .filter(p -> p.toString().endsWith(".class"))
                    .filter(p -> !p.getFileName().toString().contains("$"))
                    .map(p -> {
                        String rel = root.relativize(p).toString();
                        String className = rel.substring(0, rel.length() - 6).replace('\\', '.').replace('/', '.');
                        try {
                            return Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Spell> T instantiate(Class<T> clazz) {
        try {
            Constructor<?> ctor = Arrays.stream(clazz.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();
            ctor.setAccessible(true);
            Object[] args = Arrays.stream(ctor.getParameterTypes())
                    .map(SpellPackageCoverageTest::mockDependency)
                    .toArray();
            return (T) ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private static Object mockDependency(Class<?> type) {
        return mock(type);
    }

    private static final class TestSpell extends Spell {
        TestSpell(SkillService skillService, MobileService mopbileService, CharacterService characterService,
                  CommunicationService communicationService, EffectService effectService) {
            super(skillService, mopbileService, characterService, communicationService, effectService);
        }

        @Override
        public String getSpellName() { return "Test Spell"; }

        @Override
        public Long getSpellId() { return 999L; }

        @Override
        public Integer getSpellLevel() { return 3; }

        @Override
        public String getDescription() { return "test"; }

        @Override
        public boolean cast(Mobile mobile, Spell spell, Mobile target) { return false; }
    }
}



