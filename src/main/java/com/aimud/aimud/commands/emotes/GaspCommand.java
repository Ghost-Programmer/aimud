package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "gasp", isEmote = true)
public class GaspCommand extends BaseEmoteCommand {
    @Autowired
    public GaspCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "gasp"; }
    @Override protected String getSelfMessage() { return "You gasp in astonishment!"; }
    @Override protected String getRoomMessage() { return "gasps in astonishment!"; }
}
