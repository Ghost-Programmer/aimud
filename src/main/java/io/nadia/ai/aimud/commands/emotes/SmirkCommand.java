package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
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
