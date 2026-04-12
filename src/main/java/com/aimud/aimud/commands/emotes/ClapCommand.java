package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "clap", isEmote = true)
public class ClapCommand extends BaseEmoteCommand {
    @Autowired
    public ClapCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "clap"; }
    @Override protected String getSelfMessage() { return "You clap your hands."; }
    @Override protected String getRoomMessage() { return "claps their hands."; }
}
