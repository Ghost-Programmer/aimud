package com.aimud.aimud.commands.emotes;

import com.aimud.aimud.commands.Command;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class BaseEmoteCommand implements Command {
    protected final CommunicationService communicationService;
    protected final CharacterService characterService;
    protected final MobileService mobileService;

    protected abstract String getEmoteName();
    protected abstract String getSelfMessage();
    protected abstract String getRoomMessage();

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length > 1) {
            String targetQuery = parts[1].toLowerCase();
            Mobile targetMob = null;
            if (mobile.getCurrentRoomId() != null) {
                for (Mobile c : characterService.findAllByRoomId(mobile.getCurrentRoomId())) {
                    if (!c.isHidden() && !c.isInvisible() && c.getName().toLowerCase().contains(targetQuery)) {
                        targetMob = c;
                        break;
                    }
                }
                if (targetMob == null) {
                    for (Mobile m : mobileService.getMobilesInRoom(mobile.getCurrentRoomId())) {
                        if (!m.isHidden() && !m.isInvisible() && m.getName().toLowerCase().contains(targetQuery)) {
                            targetMob = m;
                            break;
                        }
                    }
                }
            }

            if (targetMob == null) {
                communicationService.sendTextMessage(mobile, "\n\nYou don't see anyone by that name here.");
                return Mono.empty();
            }
            String properName = targetMob.getName();
            communicationService.sendTextMessage(mobile, "\n\nYou " + getEmoteName() + " at " + properName + ".");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " " + getEmoteName() + "s at " + properName + ".");
        } else {
            communicationService.sendTextMessage(mobile, "\n\n" + getSelfMessage());
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " " + getRoomMessage());
        }
        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Emote: " + getEmoteName();
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: " + getEmoteName() + " [target]\n\nDisplay an emote to the room or a specific target.";
    }
}
