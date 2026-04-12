package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
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
