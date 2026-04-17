package io.nadia.ai.aimud.annontation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as a Divine Prayer.
 * Classes annotated with {@code @DivinePrayer} are automatically registered as Spring components
 * and can be discovered dynamically at runtime.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DivinePrayer {
    /**
     * The name of the divine prayer.
     *
     * @return the prayer name
     */
    String name();
}
