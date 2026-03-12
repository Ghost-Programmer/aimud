package com.aimud.aimud.service;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.commands.Command;
import com.aimud.aimud.model.Character;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CommandService {


    private final ApplicationContext context;

    private final Map<String, Command> taskMap = new HashMap<>();

    public CommandService(ApplicationContext context) {

        this.context = context;
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
            }
        }

        log.info("Registered {} commands", taskMap.size());
    }

    public Command getTask(String name) {
        return taskMap.get(name);
    }

    public Map<String, Command> getAllTasks() {
        return new HashMap<>(taskMap); // Return a copy for immutability
    }

    public void processCommand(Character character) {
        String command = character.getCommandQueue().remove(0);
        String[] commands = command.trim().split("\\s+");
        log.debug("Processing command '{}' for character '{}'", command, character.getName());

        Command task = this.getTask(commands[0]);
        if (task != null) {
            task.execute(character, command);
        } else {
            //TODO: Tell user invalid command
        }

    }
}
