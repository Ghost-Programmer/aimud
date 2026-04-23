package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the bowing formal social interaction.
 */
@MudCommand(name = "bow", isEmote = true)
public class BowCommand extends BaseEmoteCommand {
    /**
     * Constructs the bow emote command handler relying on common autowired domain services.
     *
     * @param communicationService the underlying real-time emitter payload engine
     * @param MobileService     registry tracking available acting entities
     * @param mobileService        secondary registry routing for generic AI agents
     */
    @Autowired
    public BowCommand(CommunicationService communicationService, MobileService mobileService) {
        super(communicationService, mobileService);
    }
    @Override protected String getEmoteName() { return "bow"; }
    @Override protected String getSelfMessage() { return "You bow deeply."; }
    @Override protected String getRoomMessage() { return "bows deeply."; }
}

