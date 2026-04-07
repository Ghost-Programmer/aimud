package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@MudCommand(name = "group")
public class GroupChatCommand implements Command {

    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final MobileService mobileService;

    public GroupChatCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.characterService = context.getBean(CharacterService.class);
        this.mobileService = context.getBean(MobileService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nWhat do you want to say to the group?");
            return Mono.empty();
        }

        if (mobile.getPartyLeaderId() == null) {
            communicationService.sendTextMessage(mobile, "\n\nYou are not in a group.");
            return Mono.empty();
        }

        String text = parts[1];
        communicationService.sendTextMessage(mobile, "\nYou tell the group, '" + text + "'");
        
        List<Mobile> allOnline = new ArrayList<>();
        allOnline.addAll(characterService.getAvailableCharacters());
        allOnline.addAll(mobileService.getActiveMobiles());

        for (Mobile c : allOnline) {
             if (mobile.getPartyLeaderId().equals(c.getPartyLeaderId()) && !c.getId().equals(mobile.getId())) {
                 communicationService.sendTextMessage(c, "\n[" + mobile.getName() + "] tells the group, '" + text + "'");
             }
        }

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Speak to everyone in your group.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: group <message>\n\nSends a message privately that only members of your current party can hear, regardless of if they are in your room or not.";
    }
}
