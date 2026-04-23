package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the laugh social interaction.
 */
@MudCommand(name = "laugh", isEmote = true)
public class LaughCommand extends BaseEmoteCommand {
    /**
     * Constructs the laugh emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public LaughCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "laugh"; }
    @Override protected String getSelfMessage() { return "You laugh out loud."; }
    @Override protected String getRoomMessage() { return "laughs out loud."; }
}


