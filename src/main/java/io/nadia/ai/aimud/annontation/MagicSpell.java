package io.nadia.ai.aimud.annontation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as a Magic Spell.
 * Classes annotated with {@code @MagicSpell} are automatically registered as Spring components
 * and can be discovered dynamically at runtime.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MagicSpell {
    /**
     * The name of the magic spell.
     *
     * @return the spell name
     */
    String name();
}
