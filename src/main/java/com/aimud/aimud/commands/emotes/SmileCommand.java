package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "smile", isEmote = true)
public class SmileCommand extends BaseEmoteCommand {
    @Autowired
    public SmileCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "smile"; }
    @Override protected String getSelfMessage() { return "You smile happily."; }
    @Override protected String getRoomMessage() { return "smiles happily."; }
}
