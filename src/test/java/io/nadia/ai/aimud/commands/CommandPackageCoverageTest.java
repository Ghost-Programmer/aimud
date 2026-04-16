package io.nadia.ai.aimud.commands;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CommandPackageCoverageTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("commandClasses")
    void everyConcreteCommandClass_InstantiatesAndHasDescriptions(Class<? extends Command> clazz) {
        Command command = instantiate(clazz);

        assertThat(command.getDescription()).isNotBlank();
        assertThat(command.getDetailedDescription()).isNotBlank();
    }

    @Test
    void everyConcreteCommandClass_IsCoveredByDiscovery() {
        List<Class<? extends Command>> classes = commandClasses().toList();
        // Defensive floor to ensure recursive classpath discovery remains active.
        assertThat(classes).hasSizeGreaterThan(40);
    }

    @Test
    void discovery_IncludesEmoteSubpackageCommands() {
        List<String> names = commandClasses().map(Class::getName).toList();
        assertThat(names.stream().anyMatch(n -> n.contains("commands.emotes."))).isTrue();
    }

    static Stream<Class<? extends Command>> commandClasses() {
        List<Class<? extends Command>> classes = findClasses("io.nadia.ai.aimud.commands")
                .filter(Command.class::isAssignableFrom)
                .map(c -> (Class<? extends Command>) c)
                .filter(c -> c != Command.class)
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .sorted(Comparator.comparing(Class::getName))
                .collect(Collectors.toList());

        return classes.stream();
    }

    private static Stream<Class<?>> findClasses(String basePackage) {
        try {
            Path root = Paths.get(Command.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path packagePath = root.resolve(basePackage.replace('.', '/'));
            if (!Files.exists(packagePath)) {
                return Stream.empty();
            }

            try (Stream<Path> walk = Files.walk(packagePath)) {
                List<Class<?>> discovered = walk
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
                        })
                        .map(c -> (Class<?>) c)
                        .collect(Collectors.toList());
                return discovered.stream();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to discover command classes", e);
        }
    }

    private static <T extends Command> T instantiate(Class<T> clazz) {
        try {
            Constructor<?> ctor = Arrays.stream(clazz.getDeclaredConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow();
            ctor.setAccessible(true);
            Object[] args = Arrays.stream(ctor.getParameterTypes())
                    .map(CommandPackageCoverageTest::mockDependency)
                    .toArray();
            return (T) ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private static Object mockDependency(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == char.class) return '\0';
        }
        if (type == String.class) {
            return "";
        }
        return mock(type);
    }
}

