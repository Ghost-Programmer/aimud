package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "groan", isEmote = true)
public class GroanCommand extends BaseEmoteCommand {
    @Autowired
    public GroanCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "groan"; }
    @Override protected String getSelfMessage() { return "You groan loudly."; }
    @Override protected String getRoomMessage() { return "groans loudly."; }
}
