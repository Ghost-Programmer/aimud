package com.aimud.aimud.service;

import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Room;
import lombok.extern.slf4j.Slf4j;
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
    private final FactionService factionService;
    private final ConfigService configService;

    public ConversationService(MobileService mobileService, CharacterService characterService, RoomService roomService,
            CommunicationService communicationService, AiService aiService, CommandService commandService,
            FactionService factionService, ConfigService configService) {
        this.mobileService = mobileService;
        this.characterService = characterService;
        this.roomService = roomService;
        this.communicationService = communicationService;
        this.aiService = aiService;
        this.commandService = commandService;
        this.factionService = factionService;
        this.configService = configService;
    }

    private final java.util.concurrent.ConcurrentHashMap<Long, java.time.Instant> lastEvaluationTime = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Set<Long> processingNpcs = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30000)
    public void processIdleConversations() {
        List<Mobile> npcs = mobileService.getActiveMobiles().stream().filter(m -> m.isUsesAi())
                .collect(Collectors.toList());
        if (npcs == null)
            return;

        for (Mobile npc : npcs) {
            // Skip dead, actively fighting, unplaced, or currently thinking NPCs
            if (npc == null || npc.getCurrentHp() <= 0 || npc.getTarget() != null
                    || processingNpcs.contains(npc.getId())) {
                continue;
            }

            Long roomId = npc.getCurrentRoomId();
            if (roomId == null)
                continue;

            // Only trigger ambient chatter if the room hasn't been evaluated recently
            java.time.Instant lastEval = lastEvaluationTime.get(roomId);
            if (lastEval == null || java.time.Duration.between(lastEval, java.time.Instant.now()).toSeconds() > 30) {
                triggerRoomConversations(roomId);
            }
        }
    }

    public void triggerRoomConversations(Long roomId) {
        // Throttle evaluation to max once per 2 seconds per room to prevent rapid
        // triggering while processing async
        java.time.Instant lastEval = lastEvaluationTime.get(roomId);
        if (lastEval != null && java.time.Duration.between(lastEval, java.time.Instant.now()).toSeconds() < 2) {
            return;
        }
        lastEvaluationTime.put(roomId, java.time.Instant.now());

        List<Mobile> npcs = mobileService.getMobilesInRoom(roomId).stream().filter(m -> m.isUsesAi())
                .collect(Collectors.toList());
        if (npcs == null || npcs.isEmpty()) {
            return;
        }

        for (Mobile npc : npcs) {
            // Skip dead, actively fighting, or currently processing NPCs
            if (npc == null || npc.getCurrentHp() <= 0 || npc.getTarget() != null
                    || processingNpcs.contains(npc.getId())) {
                continue;
            }

            roomService.getRoom(roomId).subscribe(room -> {
                if (room != null) {
                    processingNpcs.add(npc.getId());
                    reactor.core.publisher.Mono.zip(
                            configService.getAllRaces().filter(r -> r.getId().equals(npc.getRaceId())).next()
                                    .map(com.aimud.aimud.model.Race::getName).defaultIfEmpty("Unknown"),
                            configService.getAllCharacterClasses().filter(c -> c.getId().equals(npc.getClassId()))
                                    .next().map(com.aimud.aimud.model.CharacterClass::getName)
                                    .defaultIfEmpty("Unknown"))
                            .subscribe(tuple -> {
                                evaluateNpcConversation(npc, room, tuple.getT1(), tuple.getT2());
                            }, error -> {
                                processingNpcs.remove(npc.getId());
                                log.error("Failed looking up race/class for NPC", error);
                            });
                }
            });
        }
    }

    private void evaluateNpcConversation(Mobile npc, Room room, String raceName, String className) {
        // Collect history and parse if anyone is actually around physically
        List<String> history = communicationService.getRoomHistory(room.getId());

        List<Mobile> players = characterService.findAllByRoomId(room.getId()).stream()
                .filter(c -> c.getUserId() != null)
                .collect(Collectors.toList());

        // Skip inference engine entirely if there are zero players in the room!
        if (players.isEmpty()) {
            processingNpcs.remove(npc.getId());
            return;
        }

        // Prevent infinite self-talking loops. If the NPC was the last one to speak, do
        // not trigger again.
        if (!history.isEmpty()) {
            String lastMsg = history.get(history.size() - 1);
            if (lastMsg.startsWith(npc.getName() + " says") ||
                    lastMsg.startsWith(npc.getName() + " yells") ||
                    lastMsg.startsWith(npc.getName() + " shouts")) {
                processingNpcs.remove(npc.getId());
                return;
            }
        }

        List<Mobile> npcsInRoom = mobileService.getMobilesInRoom(room.getId()).stream()
                .filter(m -> !m.getId().equals(npc.getId()))
                .collect(Collectors.toList());

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an NPC in a Multi-User Dungeon (MUD).\n");
        prompt.append("You are currently in: ").append(room.getName()).append("\n");
        prompt.append("Room Description: ").append(room.getDescription()).append("\n\n");

        prompt.append("Your Identity & Stats:\n");
        prompt.append("- Name: ").append(npc.getName()).append("\n");
        prompt.append("- Race: ").append(raceName).append("\n");
        prompt.append("- Class: ").append(className).append("\n");
        prompt.append("- Level (Challenge Rating): ").append((int) npc.getChallengeRating()).append("\n");
        prompt.append("- Intelligence: ").append(npc.getIntelligence())
                .append(" (High = articulate/smart, Low = simple/dumb)\n");
        prompt.append("- Wisdom: ").append(npc.getWisdom())
                .append(" (High = insightful/calm, Low = unobservant/foolish)\n");
        prompt.append("- Charisma: ").append(npc.getCharisma())
                .append(" (High = charming/persuasive, Low = rude/abrasive)\n\n");

        prompt.append("Other entities present in the room:\n");
        for (Mobile p : players) {
            int rating = factionService.getFactionRatingSync(npc, p.getFactionId());
            prompt.append("- ").append(p.getName()).append(" (Player) [Faction Rating to you: ").append(rating)
                    .append("]\n");
            if (p.getTarget() != null) {
                prompt.append("  * Currently attacking: ").append(p.getTarget().getName()).append("\n");
            }
        }
        for (Mobile n : npcsInRoom) {
            int rating = factionService.getFactionRatingSync(npc, n.getFactionId());
            prompt.append("- ").append(n.getName()).append(" (NPC) [Faction Rating to you: ").append(rating)
                    .append("]\n");
            if (n.getTarget() != null) {
                prompt.append("  * Currently attacking: ").append(n.getTarget().getName()).append("\n");
            }
        }
        prompt.append(
                "\n* Note: Faction rating 80-100 is allied/friendly. 21-79 is neutral. 0-20 is hostile/hating.\n");

        prompt.append("\nRecent Chat History in this room:\n");
        if (history.isEmpty()) {
            prompt.append("(Quiet)\n");
        } else {
            for (String msg : history) {
                prompt.append(msg).append("\n");
            }
        }

        prompt.append("\nYour Dialogue Rules:\n");
        prompt.append("1. Roleplay strictly. You are completely immersed in a high-fantasy world.\n");
        prompt.append(
                "2. You have ABSOLUTELY NO knowledge of computers, AI, servers, patches, MUDs, coding, or the real world. NEVER mention them.\n");
        prompt.append("3. You are an NPC entity living your life. You are not a player or an assistant.\n");
        prompt.append("4. Adjust your vocabulary based on your Stats: Int=" + npc.getIntelligence() + ", Wis="
                + npc.getWisdom() + ", Cha=" + npc.getCharisma() + ".\n");
        prompt.append("5. Tone your response based on Faction Ratings (80-100=Allied, 21-79=Neutral, 0-20=Hostile).\n");
        prompt.append("6. Acknowledge your health (HP) and magic (MP) if severely injured.\n");
        prompt.append("7. READ the Chat History carefully. The very last line is what you must react to now.\n");
        prompt.append(
                "8. DO NOT REPEAT YOURSELF. If you have already said something in the history, say something completely different and new.\n");
        prompt.append("9. Incorporate your Race (" + raceName + ") and Class (" + className
                + ") into how you speak and what you know.\n");
        prompt.append(
                "10. Push the conversation forward. Ask questions, make observations, or demand things based on the players' actions.\n");
        prompt.append("11. ONLY output your action command. DO NOT output internal thoughts, JSON, or markdown.\n");

        prompt.append("\nIf there is absolutely nothing to say, output EXACTLY ONE WORD: IGNORE\n");
        prompt.append("Otherwise, output your action using EXACTLY ONE of these formats:\n");
        prompt.append("say <message>\n");
        prompt.append("yell <message>\n");
        prompt.append("shout <message>\n");
        prompt.append("emote <action>\n");

        prompt.append(
                "\nCRITICAL SYNTAX RULE: DO NOT include your own name or the word 'says' in the message! The server does that automatically.\n");
        prompt.append("BAD: say " + npc.getName() + " says, 'Hello there!'\n");
        prompt.append("GOOD: say Hello there!\n");
        prompt.append("BAD: emote *looks around*\n");
        prompt.append("GOOD: emote looks around.\n");
        prompt.append("DO NOT output quotes around your message unless you literally want to quote something.\n");

        org.springframework.ai.ollama.api.OllamaOptions options = new org.springframework.ai.ollama.api.OllamaOptions();
        options.setTemperature(0.95); // Increase temperature drastically
        options.setModel("hermes3"); // Isolate the NPC dialogue purely to the hermes3 NLP model

        // Spring AI Ollama uses stream under the hood returning Flux<String>. We MUST
        // concat the chunks.
        aiService.processPromptNoTools(prompt.toString(), options)
                .reduce("", String::concat)
                .subscribe(
                        response -> processAiResponse(npc, response),
                        error -> log.error("Error generating conversation for NPC {}", npc.getName(), error));
    }

    private void processAiResponse(Mobile npc, String response) {
        try {
            if (response == null)
                return;
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

            // 1. If AI output exactly "Orc says, 'Hello'", swap it to "say Hello"
            if (text.toLowerCase().startsWith(npc.getName().toLowerCase() + " say") ||
                    text.toLowerCase().startsWith(npc.getName().toLowerCase() + " yell") ||
                    text.toLowerCase().startsWith(npc.getName().toLowerCase() + " shout")) {
                text = "say " + text.replaceFirst("(?i)^" + java.util.regex.Pattern.quote(npc.getName())
                        + "\\s*(says|yells|shouts|say|yell|shout)[\\s:,]*['\"]?", "");
            }

            // 2. If AI output "say Orc says, 'Hello'", swap it to "say Hello"
            text = text.replaceFirst("(?i)^(say|yell|shout)\\s+" + java.util.regex.Pattern.quote(npc.getName())
                    + "\\s*(says|yells|shouts|say|yell|shout)[\\s:,]*['\"]?", "$1 ");

            // 3. Clean up hanging leading/trailing quotes applied incorrectly to the
            // message
            text = text.replaceFirst("(?i)^(say|yell|shout)\\s+['\"]", "$1 ");
            if (text.endsWith("'") || text.endsWith("\"")) {
                text = text.substring(0, text.length() - 1);
            }

            // Destroy emote narration blocks inside asterisks if they forgot the emote
            // syntax
            text = text.replaceAll("\\*.*?\\*", "").trim();

            // Enforce the command prefix to have exactly one space after it, ignoring
            // commas/colons/dashes
            String lower = text.toLowerCase();
            if (lower.startsWith("say") || lower.startsWith("yell") || lower.startsWith("shout")
                    || lower.startsWith("emote") || lower.startsWith("me")) {
                text = text.replaceFirst("(?i)^(say|yell|shout|emote|me)\\s*[:,\\-]?\\s*", "$1 ");
            }

            lower = text.toLowerCase();
            if (lower.startsWith("say ") || lower.startsWith("yell ") || lower.startsWith("shout ")
                    || lower.startsWith("emote ") || lower.startsWith("me ")) {
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
        } finally {
            processingNpcs.remove(npc.getId());
        }
    }
}
