package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the dance social interaction.
 */
@MudCommand(name = "dance", isEmote = true)
public class DanceCommand extends BaseEmoteCommand {
    /**
     * Constructs the dance emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public DanceCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "dance"; }
    @Override protected String getSelfMessage() { return "You do a little dance."; }
    @Override protected String getRoomMessage() { return "does a little dance."; }
}


