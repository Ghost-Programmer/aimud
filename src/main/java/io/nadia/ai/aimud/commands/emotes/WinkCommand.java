package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "wink", isEmote = true)
public class WinkCommand extends BaseEmoteCommand {
    @Autowired
    public WinkCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "wink"; }
    @Override protected String getSelfMessage() { return "You wink suggestively."; }
    @Override protected String getRoomMessage() { return "winks suggestively."; }
}
