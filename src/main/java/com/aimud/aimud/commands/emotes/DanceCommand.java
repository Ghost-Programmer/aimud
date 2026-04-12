package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "dance", isEmote = true)
public class DanceCommand extends BaseEmoteCommand {
    @Autowired
    public DanceCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "dance"; }
    @Override protected String getSelfMessage() { return "You do a little dance."; }
    @Override protected String getRoomMessage() { return "does a little dance."; }
}
