package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.beans.factory.annotation.Autowired;

@MudCommand(name = "smirk", isEmote = true)
public class SmirkCommand extends BaseEmoteCommand {
    @Autowired
    public SmirkCommand(CommunicationService communicationService, CharacterService characterService, MobileService mobileService) {
        super(communicationService, characterService, mobileService);
    }
    @Override protected String getEmoteName() { return "smirk"; }
    @Override protected String getSelfMessage() { return "You smirk knowingly."; }
    @Override protected String getRoomMessage() { return "smirks knowingly."; }
}
