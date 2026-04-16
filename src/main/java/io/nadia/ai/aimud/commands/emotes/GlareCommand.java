package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "glare", isEmote = true)
public class GlareCommand extends BaseEmoteCommand {
    @Autowired
    public GlareCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "glare"; }
    @Override protected String getSelfMessage() { return "You glare around you."; }
    @Override protected String getRoomMessage() { return "glares around them."; }
}
