package io.nadia.ai.aimud.commands.emotes;

import io.nadia.ai.aimud.commands.Command;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Foundational abstract command handler for all interactive social actions (emotes).
 * Encapsulates the core entity targeting logic and contextual message broadcasting.
 */
@RequiredArgsConstructor
public abstract class BaseEmoteCommand implements Command {
    protected final CommunicationService communicationService;
    
    protected final MobileService mobileService;

    /**
     * Determines the root verb string utilized to trigger the emote.
     *
     * @return the primary command name
     */
    protected abstract String getEmoteName();
    /**
     * Constructs the message rendered exclusively to the player performing the emote (no target).
     *
     * @return the self-facing action text
     */
    protected abstract String getSelfMessage();
    /**
     * Constructs the message broadcasted to all other observers in the room (no target).
     *
     * @return the room-facing action text
     */
    protected abstract String getRoomMessage();

    /**
     * Executes the overarching room broadcast logic, handling target resolution and line parting.
     *
     * @param mobile      the entity attempting the social action
     * @param commandLine the unparsed, full terminal string submitted
     * @return a completed active reactive Mono state
     */
    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length > 1) {
            String targetQuery = parts[1].toLowerCase();
            Mobile targetMob = null;
            if (mobile.getCurrentRoomId() != null) {
                for (Mobile c : mobileService.findAllByRoomId(mobile.getCurrentRoomId())) {
                    if (!c.isHidden() && !c.isInvisible() && c.getName().toLowerCase().contains(targetQuery)) {
                        targetMob = c;
                        break;
                    }
                }
                if (targetMob == null) {
                    for (Mobile m : mobileService.findAllByRoomId(mobile.getCurrentRoomId())) {
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

    /**
     * Retrieves the high-level summary listing for the given emote in the help dictionary.
     *
     * @return short action description
     */
    @Override
    public String getDescription() {
        return "Emote: " + getEmoteName();
    }

    /**
     * Retrieves the structural payload and explicit syntax instructions for the emote command.
     *
     * @return long multi-line help documentation
     */
    @Override
    public String getDetailedDescription() {
        return "Syntax: " + getEmoteName() + " [target]\n\nDisplay an emote to the room or a specific target.";
    }
}

