package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "shrug", isEmote = true)
public class ShrugCommand extends BaseEmoteCommand {
    @Autowired
    public ShrugCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "shrug"; }
    @Override protected String getSelfMessage() { return "You shrug helplessly."; }
    @Override protected String getRoomMessage() { return "shrugs helplessly."; }
}
