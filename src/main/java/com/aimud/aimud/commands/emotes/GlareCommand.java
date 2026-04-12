package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
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
