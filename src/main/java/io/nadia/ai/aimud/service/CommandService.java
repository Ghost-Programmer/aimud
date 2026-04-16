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

    private final Map<String, Command> taskMap = new HashMap<>();
    private final java.util.List<String> emoteCommands = new java.util.ArrayList<>();

    public CommandService(ApplicationContext context, CommunicationService communicationService) {
        this.context = context;
        this.communicationService = communicationService;
    }

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

    public Command getTask(String name) {
        return taskMap.get(name);
    }

    public Map<String, Command> getAllTasks() {
        return new HashMap<>(taskMap); // Return a copy for immutability
    }

    public java.util.List<String> getEmoteCommands() {
        return java.util.Collections.unmodifiableList(emoteCommands);
    }

    public Mono<Void> processCommand(Mobile character) {
        String command = character.getCommandQueue().remove(0);
        String[] commands = command.trim().split("\\s+");
        log.info("Processing command '{}' for character '{}'", command, character.getName());

        Command task = this.getTask(commands[0]);
        if (task != null) {
            return task.execute(character, command);
        } else {
            communicationService.sendTextMessage(character, "Invalid command: " + commands[0]);
            return Mono.empty();
        }

    }
}
