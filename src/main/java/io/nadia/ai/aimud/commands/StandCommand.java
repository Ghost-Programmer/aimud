package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.types.MobileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@Slf4j
@MudCommand(name = "stand", role = "MUD_USER")
public class StandCommand implements Command {

    private final CommunicationService communicationService;

    public StandCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        if (mobile.getStatus() == MobileStatus.STANDING) {
            communicationService.sendTextMessage(mobile, "\n\nYou are already standing.");
            return Mono.empty();
        }
        
        mobile.setStatus(MobileStatus.STANDING);
        communicationService.sendTextMessage(mobile, "\n\nYou stand up.");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " stands up.");
        communicationService.sendCharacterUpdate(mobile);
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Stand up from resting or sitting.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: stand\n\nChanges your stance to standing. This returns your health and mana regeneration to normal.";
    }
}
