package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "cheer", isEmote = true)
public class CheerCommand extends BaseEmoteCommand {
    @Autowired
    public CheerCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "cheer"; }
    @Override protected String getSelfMessage() { return "You cheer enthusiastically!"; }
    @Override protected String getRoomMessage() { return "cheers enthusiastically!"; }
}
