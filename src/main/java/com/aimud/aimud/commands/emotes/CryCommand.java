package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "cry", isEmote = true)
public class CryCommand extends BaseEmoteCommand {
    @Autowired
    public CryCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "cry"; }
    @Override protected String getSelfMessage() { return "You burst into tears."; }
    @Override protected String getRoomMessage() { return "bursts into tears."; }
}
