package io.nadia.ai.aimud.annontation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation to mark a class as a Bard Song.
 * Classes annotated with {@code @BardSong} are automatically registered as Spring components
 * and can be discovered dynamically at runtime.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BardSong {
    /**
     * The name of the bard song.
     *
     * @return the song name
     */
    String name();
}
