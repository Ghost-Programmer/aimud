package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;
/**
 * EmoteCommand standard implementation layer.
 * Perform a physical action or expression.
 */

@MudCommand(name = "emote")
public class EmoteCommand implements Command {

    private final CommunicationService communicationService;

    public EmoteCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
    }

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String arguments) {
        String[] parts = arguments.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nEmote what?");
            return Mono.empty();
        }

        String text = parts[1];
        
        if (text.length() > 0 && Character.isUpperCase(text.charAt(0))) {
            text = Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
        
        // Ensure standard emote grammar (Orc smiles.)
        if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
            text += ".";
        }

        String emoteMessage = "\n" + mobile.getName() + " " + text;

        communicationService.sendTextMessage(mobile, emoteMessage);
        communicationService.roomMessage(mobile, emoteMessage);

        return Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Perform a physical action or expression.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: emote <action>\n\nDisplays a custom action to everyone in the room. Example: 'emote smiles.' outputs 'YourName smiles.'";
    }
}
