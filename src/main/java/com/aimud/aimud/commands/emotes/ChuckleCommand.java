package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
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
