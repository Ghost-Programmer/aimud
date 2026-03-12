package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommandService {

    public CommandService() {

    }

    public void processCommand(Character character) {
        String command = character.getCommandQueue().remove(0);
        log.debug("Processing command '{}' for character '{}'", command, character.getName());


    }
}
