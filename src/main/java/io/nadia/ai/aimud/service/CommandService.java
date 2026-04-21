package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.commands.Command;
import io.nadia.ai.aimud.model.Mobile;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommandService {


    private final ApplicationContext context;
    private final CommunicationService communicationService;
    private final io.nadia.ai.aimud.repository.UserRepository userRepository;

    private final Map<String, Command> taskMap = new HashMap<>();
    private final java.util.List<String> emoteCommands = new java.util.ArrayList<>();

    /**
     * Constructs a new CommandService.
     *
     * @param context              the Spring application context used to discover command beans
     * @param communicationService the communication service to send messages to characters
     * @param userRepository       the user repository to check permissions
     */
    public CommandService(ApplicationContext context, CommunicationService communicationService, io.nadia.ai.aimud.repository.UserRepository userRepository) {
        this.context = context;
        this.communicationService = communicationService;
        this.userRepository = userRepository;
    }

    /**
     * Initializes the command map by discovering all beans implementing the {@link Command} interface
     * and annotated with {@link MudCommand}. Also tracks which commands are emotes.
     */
    @PostConstruct
    public void registerTasks() {
        // Retrieve all beans that implement the specific interface
        Map<String, Command> beans = context.getBeansOfType(Command.class);

        for (Command bean : beans.values()) {
            // Use AnnotationUtils to handle Spring proxies/AOP correctly
            MudCommand annotation = AnnotationUtils.findAnnotation(bean.getClass(), MudCommand.class);

            if (annotation != null) {
                String key = annotation.name();
                taskMap.put(key, bean);
                if (annotation.isEmote()) {
                    emoteCommands.add(key);
                }
            }
        }

        log.info("Registered {} commands: {}", taskMap.size(), taskMap.keySet().stream().collect(Collectors.joining(", ")));
    }

    /**
     * Retrieves a registered command by its name.
     *
     * @param name the name of the command
     * @return the {@link Command} instance, or null if not found
     */
    public Command getTask(String name) {
        return taskMap.get(name);
    }

    /**
     * Retrieves all registered commands.
     *
     * @return a map of command names to their corresponding {@link Command} instances
     */
    public Map<String, Command> getAllTasks() {
        return new HashMap<>(taskMap); // Return a copy for immutability
    }

    /**
     * Retrieves a list of all commands that are marked as emotes.
     *
     * @return an unmodifiable list of emote command names
     */
    public java.util.List<String> getEmoteCommands() {
        return java.util.Collections.unmodifiableList(emoteCommands);
    }

    /**
     * Checks if a character has permission to execute a specific command.
     *
     * @param character the mobile entity
     * @param task      the command to execute
     * @return a {@link Mono} emitting true if allowed, false otherwise
     */
    public Mono<Boolean> hasPermission(Mobile character, Command task) {
        MudCommand annotation = AnnotationUtils.findAnnotation(task.getClass(), MudCommand.class);
        String requiredRole = (annotation != null) ? annotation.role() : "MUD_USER";

        if (character.getUserId() == null) {
            return Mono.just("MUD_USER".equals(requiredRole));
        }

        return userRepository.findById(character.getUserId())
                .map(user -> {
                    String userRole = user.getRole();
                    if (userRole == null || userRole.trim().isEmpty()) {
                        userRole = "MUD_USER";
                    }
                    if ("MUD_ADMIN".equals(userRole)) {
                        return true;
                    }
                    return requiredRole.equals(userRole);
                })
                .defaultIfEmpty("MUD_USER".equals(requiredRole));
    }

    /**
     * Processes the next command in the character's command queue.
     *
     * @param character the mobile entity executing the command
     * @return a {@link Mono} representing the completion of command execution
     */
    public Mono<Void> processCommand(Mobile character) {
        if (character.getCommandQueue().isEmpty()) {
            return Mono.empty();
        }

        if (character.isFrozen()) {
            character.getCommandQueue().clear();
            communicationService.sendTextMessage(character, "\n\nYou are frozen and cannot perform any actions.");
            return Mono.empty();
        }

        String command = character.getCommandQueue().remove(0);
        String[] commands = command.trim().split("\\s+");
        log.info("Processing command '{}' for character '{}'", command, character.getName());

        Command task = this.getTask(commands[0]);
        if (task != null) {
            return hasPermission(character, task)
                    .flatMap(hasPerm -> {
                        if (hasPerm) {
                            return task.execute(character, command);
                        } else {
                            communicationService.sendTextMessage(character, "You do not have permission to use that command.");
                            return Mono.empty();
                        }
                    });
        } else {
            communicationService.sendTextMessage(character, "Invalid command: " + commands[0]);
            return Mono.empty();
        }

    }
}
