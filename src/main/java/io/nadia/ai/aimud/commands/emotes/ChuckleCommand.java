package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "chuckle", isEmote = true)
public class ChuckleCommand extends BaseEmoteCommand {
    @Autowired
    public ChuckleCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "chuckle"; }
    @Override protected String getSelfMessage() { return "You chuckle politely."; }
    @Override protected String getRoomMessage() { return "chuckles politely."; }
}
