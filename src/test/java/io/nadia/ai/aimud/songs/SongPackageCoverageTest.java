package io.nadia.ai.aimud.songs;

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

class SongPackageCoverageTest {

    @Test
    void baseSong_UtilityMethodsWork() {
        SkillService skillService = mock(SkillService.class);
        TestSong song = new TestSong(skillService,
                mock(MobileService.class),
                mock(CommunicationService.class),
                mock(EffectService.class));

        Mobile caster = new Mobile();
        Mobile target = new Mobile();
        caster.setTarget(target);

        when(skillService.getSkillRank(eq(caster), eq(SkillsType.SING_SONG))).thenReturn(8);
        when(skillService.getSkillRank(eq(caster), eq("Song: Test Song"))).thenReturn(4);

        assertThat(song.getSongSkillName()).isEqualTo("Song: Test Song");
        assertThat(song.getManaCost(caster)).isEqualTo(19); // 9 + 4 + (8 - 2)
        assertThat(song.getDefaultTarget(caster)).isSameAs(target);
        assertThat(song.getTarget(caster, new String[]{"sing", "test"})).isSameAs(target);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("songClasses")
    void everyConcreteSongClass_HasMetadataAndInstantiates(Class<? extends Song> clazz) {
        Song song = instantiate(clazz);

        assertThat(song.getSongName()).isNotBlank();
        assertThat(song.getSongId()).isNotNull();
        assertThat(song.getSongLevel()).isNotNull();
        assertThat(song.getDescription()).isNotNull();
    }

    @Test
    void everyConcreteSongClass_IsCoveredByDiscovery() {
        List<Class<? extends Song>> classes = songClasses().toList();
        assertThat(classes).hasSizeGreaterThan(10);
    }

    static Stream<Class<? extends Song>> songClasses() {
        java.util.List<Class<? extends Song>> classes = findClasses("io.nadia.ai.aimud.songs")
                .filter(Song.class::isAssignableFrom)
                .map(c -> (Class<? extends Song>) c)
                .filter(c -> c != Song.class)
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .collect(java.util.stream.Collectors.toList());
        classes.sort(java.util.Comparator.comparing(Class::getName));
        return classes.stream();
    }

    private static Stream<Class<?>> findClasses(String basePackage) {
        try {
            Path root = Paths.get(Song.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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

    private static <T extends Song> T instantiate(Class<T> clazz) {
        try {
            Constructor<?> ctor = Arrays.stream(clazz.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();
            ctor.setAccessible(true);
            Object[] args = Arrays.stream(ctor.getParameterTypes())
                    .map(SongPackageCoverageTest::mockDependency)
                    .toArray();
            return (T) ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private static Object mockDependency(Class<?> type) {
        return mock(type);
    }

    private static final class TestSong extends Song {
        TestSong(SkillService skillService, MobileService mobileService,
                 CommunicationService communicationService, EffectService effectService) {
            super(skillService, mobileService, communicationService, effectService);
        }

        @Override
        public String getSongName() { return "Test Song"; }

        @Override
        public Long getSongId() { return 998L; }

        @Override
        public Integer getSongLevel() { return 2; }

        @Override
        public String getDescription() { return "test"; }

        @Override
        public boolean sing(Mobile mobile, Song song, Mobile target) { return false; }
    }
}

