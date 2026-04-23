package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the shrug social interaction.
 */
@MudCommand(name = "shrug", isEmote = true)
public class ShrugCommand extends BaseEmoteCommand {
    /**
     * Constructs the shrug emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public ShrugCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "shrug"; }
    @Override protected String getSelfMessage() { return "You shrug helplessly."; }
    @Override protected String getRoomMessage() { return "shrugs helplessly."; }
}


