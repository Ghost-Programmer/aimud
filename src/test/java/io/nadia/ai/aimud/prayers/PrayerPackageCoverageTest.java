package io.nadia.ai.aimud.prayers;

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

class PrayerPackageCoverageTest {

    @Test
    void basePrayer_UtilityMethodsWork() {
        SkillService skillService = mock(SkillService.class);
        TestPrayer prayer = new TestPrayer(skillService,
                mock(MobileService.class),
                mock(CommunicationService.class),
                mock(EffectService.class));

        Mobile caster = new Mobile();
        Mobile target = new Mobile();
        caster.setTarget(target);

        when(skillService.getSkillRank(eq(caster), eq(SkillsType.SAY_PRAYER))).thenReturn(7);
        when(skillService.getSkillRank(eq(caster), eq("Prayer: Test Prayer"))).thenReturn(3);

        assertThat(prayer.getPrayerSkillName()).isEqualTo("Prayer: Test Prayer");
        assertThat(prayer.getManaCost(caster)).isEqualTo(17); // 9 + 3 + (7 - 2)
        assertThat(prayer.getDefaultTarget(caster)).isSameAs(target);
        assertThat(prayer.getTarget(caster, new String[]{"pray", "test"})).isSameAs(target);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prayerClasses")
    void everyConcretePrayerClass_HasMetadataAndInstantiates(Class<? extends Prayer> clazz) {
        Prayer prayer = instantiate(clazz);

        assertThat(prayer.getPrayerName()).isNotBlank();
        assertThat(prayer.getPrayerId()).isNotNull();
        assertThat(prayer.getPrayerLevel()).isNotNull();
        assertThat(prayer.getDescription()).isNotNull();
    }

    @Test
    void everyConcretePrayerClass_IsCoveredByDiscovery() {
        List<Class<? extends Prayer>> classes = prayerClasses().toList();
        assertThat(classes).hasSizeGreaterThan(20);
    }

    @SuppressWarnings("unchecked")
    static Stream<Class<? extends Prayer>> prayerClasses() {
        java.util.List<Class<? extends Prayer>> classes = findClasses("io.nadia.ai.aimud.prayers")
                .filter(Prayer.class::isAssignableFrom)
                .map(c -> (Class<? extends Prayer>) c)
                .filter(c -> c != Prayer.class)
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .collect(java.util.stream.Collectors.toList());
        classes.sort(java.util.Comparator.comparing(Class::getName));
        return classes.stream();
    }

    private static Stream<Class<?>> findClasses(String basePackage) {
        try {
            Path root = Paths.get(Prayer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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

    @SuppressWarnings("unchecked")
    private static <T extends Prayer> T instantiate(Class<T> clazz) {
        try {
            Constructor<?> ctor = Arrays.stream(clazz.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();
            ctor.setAccessible(true);
            Object[] args = Arrays.stream(ctor.getParameterTypes())
                    .map(PrayerPackageCoverageTest::mockDependency)
                    .toArray();
            return (T) ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private static Object mockDependency(Class<?> type) {
        return mock(type);
    }

    private static final class TestPrayer extends Prayer {
        TestPrayer(SkillService skillService, MobileService mobileService,
                   CommunicationService communicationService, EffectService effectService) {
            super(skillService, mobileService, communicationService, effectService);
        }

        @Override
        public String getPrayerName() { return "Test Prayer"; }

        @Override
        public Long getPrayerId() { return 997L; }

        @Override
        public Integer getPrayerLevel() { return 2; }

        @Override
        public String getDescription() { return "test"; }

        @Override
        public boolean pray(Mobile mobile, Prayer prayer, Mobile target) { return false; }
    }
}

