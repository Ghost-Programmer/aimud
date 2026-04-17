package io.nadia.ai.aimud.annontation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Annotation to mark a class as a MUD Player Command.
 * Classes annotated with {@code @MudCommand} are automatically registered as Spring components
 * and processed by the CommandService for handling user input.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MudCommand {
    /**
     * The primary command string used to trigger this action (e.g., "look", "north").
     *
     * @return the command name
     */
    String name();

    /**
     * Indicates whether this command is considered an emote or social action.
     * Emotes typically broadcast a message to the room without affecting game mechanics.
     *
     * @return true if the command is an emote, false otherwise
     */
    boolean isEmote() default false;
}