package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "assist")
public class AssistCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Character character, String commandLine) {
        log.info("Executing assist command for character: {}", character.getName());
        
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(character, "\n\nAssist who?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        List<Character> charactersInRoom = characterService.findAllByRoomId(character.getCurrentRoomId());
        Character assistTarget = charactersInRoom.stream()
                .filter(c -> !c.getId().equals(character.getId()) && c.getName().toLowerCase().contains(targetName))
                .findFirst()
                .orElse(null);

        if (assistTarget == null) {
            communicationService.sendTextMessage(character, "\n\nThey aren't here.");
            return Mono.empty();
        }

        Mobile targetOfTarget = assistTarget.getTarget();
        if (targetOfTarget == null) {
            communicationService.sendTextMessage(character, "\n\n" + assistTarget.getName() + " is not fighting anyone.");
            return Mono.empty();
        }

        character.setTarget(targetOfTarget);
        communicationService.sendTextMessage(character, "\n\nYou jump in to assist " + assistTarget.getName() + " in the fight!");
        communicationService.roomMessage(character, "\n" + character.getName() + " jumps in to assist " + assistTarget.getName() + "!");
        
        return Mono.empty();
    }
}
