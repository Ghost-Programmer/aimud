package com.aimud.aimud.service;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Room;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConversationService {

    private final MobileService mobileService;
    private final CharacterService characterService;
    private final RoomService roomService;
    private final CommunicationService communicationService;
    private final AiService aiService;
    private final CommandService commandService;

    public ConversationService(MobileService mobileService, CharacterService characterService, RoomService roomService, CommunicationService communicationService, AiService aiService, CommandService commandService) {
        this.mobileService = mobileService;
        this.characterService = characterService;
        this.roomService = roomService;
        this.communicationService = communicationService;
        this.aiService = aiService;
        this.commandService = commandService;
    }

    @Scheduled(fixedRate = 15000)
    public void processConversations() {
        List<Mobile> npcs = mobileService.getActiveMobiles();
        if (npcs == null) return;
        
        for (Mobile npc : npcs) {
            // Skip dead, actively fighting, or unplaced NPCs
            if (npc == null || npc.getCurrentHp() <= 0 || npc.getTarget() != null || npc.getCurrentRoomId() == null) {
                continue; 
            }

            Long roomId = npc.getCurrentRoomId();
            roomService.getRoom(roomId).subscribe(room -> {
                if (room != null) {
                    evaluateNpcConversation(npc, room);
                }
            });
        }
    }

    private void evaluateNpcConversation(Mobile npc, Room room) {
        // Collect history and parse if anyone is actually around physically
        List<String> history = communicationService.getRoomHistory(room.getId());
        
        List<Mobile> players = characterService.findAllByRoomId(room.getId()).stream()
                .filter(c -> c.getUserId() != null)
                .collect(Collectors.toList());

        // Skip inference engine entirely if there are zero players in the room!
        if (players.isEmpty()) {
            return;
        }

        // Prevent infinite self-talking loops. If the NPC was the last one to speak, do not trigger again.
        if (!history.isEmpty()) {
            String lastMsg = history.get(history.size() - 1);
            if (lastMsg.startsWith(npc.getName() + " says") || 
                lastMsg.startsWith(npc.getName() + " yells") || 
                lastMsg.startsWith(npc.getName() + " shouts")) {
                return;
            }
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an NPC in a Multi-User Dungeon (MUD).\n");
        prompt.append("You are currently in: ").append(room.getName()).append("\n");
        prompt.append("Room Description: ").append(room.getDescription()).append("\n\n");
        
        prompt.append("Your name is: ").append(npc.getName()).append("\n");
        prompt.append("Your Level (Challenge Rating): ").append((int) npc.getChallengeRating()).append("\n");
        prompt.append("Your faction ID: ").append(npc.getFactionId()).append("\n\n");

        prompt.append("Other characters present in the room:\n");
        for (Mobile p : players) {
            prompt.append("- ").append(p.getName()).append(" (Player)\n");
        }
        
        prompt.append("\nRecent Chat History in this room:\n");
        if (history.isEmpty()) {
            prompt.append("(Quiet)\n");
        } else {
            for (String msg : history) {
                prompt.append(msg).append("\n");
            }
        }

        prompt.append("\nYour Task:\n");
        prompt.append("You are roleplaying a character. You MUST NOT break character. DO NOT output JSON, arrays, code, or metadata.\n");
        prompt.append("If it is quiet and you have no reason to speak, output EXACTLY the word: IGNORE\n");
        prompt.append("If you wish to converse, output EXACTLY ONE of the following formats (without any braces or quotes):\n");
        prompt.append("say <message>\n");
        prompt.append("yell <message>\n");
        prompt.append("shout <message>\n");
        prompt.append("DO NOT output quotes, thoughts, or formatting blocks. Only output the exact command or IGNORE.");

        // Spring AI Ollama uses stream under the hood returning Flux<String>. We MUST concat the chunks.
        aiService.processPrompt(prompt.toString())
            .reduce("", String::concat)
            .subscribe(
                response -> processAiResponse(npc, response),
                error -> log.error("Error generating conversation for NPC {}", npc.getName(), error)
            );
    }

    private void processAiResponse(Mobile npc, String response) {
        if (response == null) return;
        String text = response.trim();
        
        if (text.isEmpty() || text.equalsIgnoreCase("IGNORE") || text.contains("IGNORE")) {
            return;
        }
        
        // Strip out markdown hallucinations just in case
        if (text.startsWith("```")) {
            text = text.replaceAll("```[a-zA-Z]*", "").replaceAll("```", "").trim();
        }

        // Clean up any Ollama JSON/Array/Quote wrapping hallucinations
        text = text.replaceAll("[\\{\\}\\[\\]\"]", "");

        // Enforce the command prefix to have exactly one space after it, ignoring commas/colons/dashes
        String lower = text.toLowerCase();
        if (lower.startsWith("say") || lower.startsWith("yell") || lower.startsWith("shout")) {
            text = text.replaceFirst("(?i)^(say|yell|shout)\\s*[:,\\-]?\\s*", "$1 ");
        }

        lower = text.toLowerCase();
        if (lower.startsWith("say ") || lower.startsWith("yell ") || lower.startsWith("shout ")) {
            log.info("NPC AI Command Execution: {}", text);
            npc.getCommandQueue().add(text);
            commandService.processCommand(npc).subscribe();
        } else {
            // Block massive JSON echoes dynamically
            if (text.contains("effectType") || text.contains("modifier1")) {
                log.info("NPC AI hallucinated system json. Ignoring.");
                return;
            }
            log.info("NPC AI Fallback Say Execution: {}", text);
            npc.getCommandQueue().add("say " + text);
            commandService.processCommand(npc).subscribe();
        }
    }
}
