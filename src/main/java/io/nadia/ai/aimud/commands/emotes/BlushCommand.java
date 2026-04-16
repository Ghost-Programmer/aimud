package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "blush", isEmote = true)
public class BlushCommand extends BaseEmoteCommand {
    @Autowired
    public BlushCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "blush"; }
    @Override protected String getSelfMessage() { return "Your cheeks burn red."; }
    @Override protected String getRoomMessage() { return "blushes."; }
}
