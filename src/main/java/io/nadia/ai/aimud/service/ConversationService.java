package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.CharacterClass;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.model.Race;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.spells.Spell;
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
    private final CommandService commandService;
    private final FactionService factionService;
    private final ConfigService configService;
    private final SpellService spellService;
    private final SongService songService;
    private final PrayerService prayerService;
    private final org.springframework.ai.vectorstore.VectorStore vectorStore;
    private final org.springframework.ai.chat.client.ChatClient chatClient;

    /**
     * Constructs a new ConversationService.
     *
     * @param mobileService        the mobile service
     * @param characterService     the character service
     * @param roomService          the room service
     * @param communicationService the communication service
     * @param aiService            the AI conversation service
     * @param commandService       the command processing service
     * @param factionService       the faction relation service
     * @param configService        the configuration service for races/classes
     * @param spellService         the spell service
     * @param songService          the song service
     * @param prayerService        the prayer service
     * @param vectorStore          the VectorStore
     * @param chatModel            the Ollama chat model
     */
    public ConversationService(MobileService mobileService, CharacterService characterService, RoomService roomService,
            CommunicationService communicationService, CommandService commandService,
            FactionService factionService, ConfigService configService, SpellService spellService,
            SongService songService, PrayerService prayerService,
            org.springframework.ai.vectorstore.VectorStore vectorStore,
            org.springframework.ai.ollama.OllamaChatModel chatModel) {
        this.mobileService = mobileService;
        this.characterService = characterService;
        this.roomService = roomService;
        this.communicationService = communicationService;
        this.commandService = commandService;
        this.factionService = factionService;
        this.configService = configService;
        this.spellService = spellService;
        this.songService = songService;
        this.prayerService = prayerService;
        this.vectorStore = vectorStore;
        this.chatClient = org.springframework.ai.chat.client.ChatClient.builder(chatModel).build();
    }

    private final java.util.concurrent.ConcurrentHashMap<Long, java.time.Instant> lastEvaluationTime = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Set<Long> processingNpcs = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * Scheduled task that periodically processes conversations for idle NPCs.
     * Evaluates room triggers based on time elapsed since the last evaluation.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 10000)
    public void processIdleConversations() {
        log.info("Processing idle conversations...");
        List<Mobile> npcs = mobileService.getActiveMobiles().stream().filter(m -> m.isUsesAi())
                .collect(Collectors.toList());
        if (npcs == null || npcs.isEmpty()) {
            log.info("No Mobiles with AI Chat enabled found.");
            return;
        }

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

    /**
     * Immediately triggers conversation processing for all AI-enabled NPCs in a
     * specific room.
     *
     * @param roomId the ID of the room to process conversations in
     */
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
            log.info("No Mobiles with AI Chat enabled found in room " + roomId);
            return;
        }

        for (Mobile npc : npcs) {
            // Skip dead, actively fighting, or currently processing NPCs
            if (npc == null || npc.getCurrentHp() <= 0 || npc.getTarget() != null
                    || processingNpcs.contains(npc.getId())) {
                log.info(String.format("Skipping NPC %s - dead: %b, fighting: %b, processing: %b",
                        npc.getName(), npc.getCurrentHp() <= 0, npc.getTarget() != null,
                        processingNpcs.contains(npc.getId())));
                continue;
            }

            roomService.getRoom(roomId).subscribe(room -> {
                if (room != null) {
                    processingNpcs.add(npc.getId());
                    reactor.core.publisher.Mono.zip(
                            configService.getAllRaces().filter(r -> r.getId().equals(npc.getRaceId())).next()
                                    .map(Race::getName).defaultIfEmpty("Unknown"),
                            configService.getAllCharacterClasses().filter(c -> c.getId().equals(npc.getClassId()))
                                    .next().map(CharacterClass::getName)
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

    /**
     * Internal method to evaluate local actions, build a contextual prompt, and
     * send it to the AI for an NPC.
     *
     * @param npc       the NPC entity determining its next action
     * @param room      the room the NPC is located in
     * @param raceName  the descriptive name of the NPC's race
     * @param className the descriptive name of the NPC's class
     */
    private void evaluateNpcConversation(Mobile npc, Room room, String raceName, String className) {
        // Collect history and parse if anyone is actually around physically
        List<String> history = communicationService.getRoomHistory(room.getId());

        List<Mobile> players = characterService.findAllByRoomId(room.getId()).stream()
                .filter(c -> c.getUserId() != null)
                .collect(Collectors.toList());

        // Skip inference engine entirely if there are zero players in the room!
        if (players.isEmpty()) {
            log.info("No players in room " + room.getId());
            processingNpcs.remove(npc.getId());
            return;
        }

        // Prevent infinite self-talking loops. If the NPC was the last one to speak,
        // it has a 20% chance to trigger again to keep convo flowing.
        if (!history.isEmpty()) {
            String lastMsg = history.get(history.size() - 1);
            if (lastMsg.startsWith(npc.getName())
                    && (lastMsg.contains("says") || lastMsg.contains("shouts") || lastMsg.contains("whispers"))) {
                if (Math.random() > 0.20) {
                    log.info("NPC " + npc.getName() + " was the last to speak, skipping");
                    processingNpcs.remove(npc.getId());
                    return;
                } else {
                    log.info("NPC " + npc.getName() + " was the last to speak, but hit 20% chance to speak again!");
                }
            }
        }

        List<Mobile> npcsInRoom = mobileService.getMobilesInRoom(room.getId()).stream()
                .filter(m -> !m.getId().equals(npc.getId()))
                .collect(Collectors.toList());

        List<MobileAction> availableActions = new java.util.ArrayList<>();
        if (npc.getActions() != null) {
            availableActions.addAll(npc.getActions());
        }

        if (npc.getSkills() != null) {
            for (java.util.Map.Entry<String, Spell> entry : spellService.getSpellMap().entrySet()) {
                String spellName = entry.getKey();
                String skillName = entry.getValue().getSpellSkillName();
                boolean knowsSpell = npc.getSkills().stream()
                        .anyMatch(s -> s.getRank() > 0 && s.getName().equalsIgnoreCase(skillName));
                if (knowsSpell) {
                    MobileAction act = new MobileAction();
                    act.setDescription("Cast the spell '" + spellName + "'");
                    act.setActionCommand("cast " + spellName);
                    availableActions.add(act);
                }
            }
            for (java.util.Map.Entry<String, Prayer> entry : prayerService.getPrayerMap().entrySet()) {
                String prayerName = entry.getKey();
                String skillName = entry.getValue().getPrayerSkillName();
                boolean knowsPrayer = npc.getSkills().stream()
                        .anyMatch(s -> s.getRank() > 0 && s.getName().equalsIgnoreCase(skillName));
                if (knowsPrayer) {
                    MobileAction act = new MobileAction();
                    act.setDescription("Pray for '" + prayerName + "'");
                    act.setActionCommand("pray " + prayerName);
                    availableActions.add(act);
                }
            }
            for (java.util.Map.Entry<String, Song> entry : songService.getSongMap().entrySet()) {
                String songName = entry.getKey();
                String skillName = entry.getValue().getSongSkillName();
                boolean knowsSong = npc.getSkills().stream()
                        .anyMatch(s -> s.getRank() > 0 && s.getName().equalsIgnoreCase(skillName));
                if (knowsSong) {
                    MobileAction act = new MobileAction();
                    act.setDescription("Sing the song '" + songName + "'");
                    act.setActionCommand("sing " + songName);
                    availableActions.add(act);
                }
            }
        }

        boolean hasBash = npc.getSkills() != null
                && npc.getSkills().stream().anyMatch(s -> s.getRank() > 0 && s.getName().equalsIgnoreCase("Bash"));
        boolean hasDisarm = npc.getSkills() != null
                && npc.getSkills().stream().anyMatch(s -> s.getRank() > 0 && s.getName().equalsIgnoreCase("Disarm"));

        for (Mobile p : players) {
            MobileAction atkAct = new MobileAction();
            atkAct.setDescription("Attack " + p.getName());
            atkAct.setActionCommand("attack " + p.getName());
            availableActions.add(atkAct);

            if (hasBash) {
                MobileAction bashAct = new MobileAction();
                bashAct.setDescription("Bash " + p.getName());
                bashAct.setActionCommand("bash " + p.getName());
                availableActions.add(bashAct);
            }

            if (hasDisarm) {
                MobileAction disarmAct = new MobileAction();
                disarmAct.setDescription("Disarm " + p.getName());
                disarmAct.setActionCommand("disarm " + p.getName());
                availableActions.add(disarmAct);
            }
        }

        // Add Emotes
        for (String emote : commandService.getEmoteCommands()) {
            MobileAction emoteAct = new MobileAction();
            emoteAct.setDescription("Emote: " + emote);
            emoteAct.setActionCommand(emote);
            availableActions.add(emoteAct);

            // Allow targeted emotes to players
            for (Mobile p : players) {
                MobileAction targetEmoteAct = new MobileAction();
                targetEmoteAct.setDescription("Emote: " + emote + " at " + p.getName());
                targetEmoteAct.setActionCommand(emote + " " + p.getName());
                availableActions.add(targetEmoteAct);
            }
        }

        log.info("Evaluating NPC conversation for {}. Found {} available actions.", npc.getName(),
                availableActions.size());
        if (availableActions.isEmpty()) {
            processingNpcs.remove(npc.getId());
            return;
        }

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

        prompt.append("\nAvailable Actions:\n");
        for (int i = 0; i < availableActions.size(); i++) {
            prompt.append((i + 1)).append(". ").append(availableActions.get(i).getDescription()).append("\n");
        }

        prompt.append("\nYour Decision Rules:\n");
        prompt.append("1. Roleplay strictly. You are completely immersed in a high-fantasy world.\n");
        prompt.append("2. READ the Chat History carefully to understand the context.\n");
        prompt.append("3. Choose ONE OR MORE of the Available Actions based on the context.\n");
        prompt.append("4. ONLY output a comma-separated list of the numbers of the actions you want to take.\n");
        prompt.append("5. For example: 1,3\n");
        prompt.append("6. If there is absolutely nothing to do, output EXACTLY ONE WORD: IGNORE\n");
        prompt.append("7. DO NOT output internal thoughts, JSON, quotes, or markdown.\n");

        org.springframework.ai.ollama.api.OllamaOptions options = new org.springframework.ai.ollama.api.OllamaOptions();
        options.setTemperature(0.95);
        options.setModel("hermes3");

        org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor ragAdvisor = new org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor(
                vectorStore,
                org.springframework.ai.vectorstore.SearchRequest.builder()
                        .topK(3)
                        .filterExpression("npcId == " + npc.getId())
                        .build());

        log.info("Processing conversation for NPC: {}", npc.getName());
        log.info("Prompt: \n {}", prompt.toString());

        configService.getAllAgents().collectList().flatMapMany(agents -> {
            StringBuilder systemText = new StringBuilder();
            for (io.nadia.ai.aimud.model.Agent agent : agents) {
                systemText.append("Agent: ").append(agent.title()).append("\n").append(agent.content()).append("\n\n");
            }
            return chatClient.prompt()
                    .system(systemText.toString())
                    .user(prompt.toString())
                    .options(options)
                    .advisors(ragAdvisor)
                    .stream().content()
                    .filter(text -> text != null && !text.isEmpty());
        })
                .reduce("", String::concat)
                .subscribe(
                        response -> processAiResponse(npc, availableActions, response),
                        error -> {
                            log.error("Error generating conversation for NPC {}", npc.getName(), error);
                            processingNpcs.remove(npc.getId());
                        });
    }

    /**
     * Parses the string response generated by the AI model and executes the chosen
     * actions.
     *
     * @param npc              the NPC taking action
     * @param availableActions the list of all possible actions the NPC could have
     *                         taken
     * @param response         the raw textual response from the AI
     */
    private void processAiResponse(Mobile npc, List<MobileAction> availableActions, String response) {
        try {
            if (response == null)
                return;
            String text = response.trim();
            log.info("NPC AI Response: {}", text);

            if (text.isEmpty() || text.equalsIgnoreCase("IGNORE") || text.contains("IGNORE")) {
                return;
            }

            // Clean up any Ollama wrapping hallucinations
            text = text.replaceAll("[a-zA-Z`\\[\\]\\{\\}\"\\n]", "").trim();
            log.info("NPC AI Response: {}", text);
            String[] indices = text.split(",");
            for (String indexStr : indices) {
                indexStr = indexStr.trim();
                if (!indexStr.isEmpty()) {
                    try {
                        int index = Integer.parseInt(indexStr) - 1;
                        if (index >= 0 && index < availableActions.size()) {
                            String command = availableActions.get(index).getActionCommand();
                            log.info("NPC AI Action Execution: {}", command);
                            npc.getCommandQueue().add(command);
                            commandService.processCommand(npc).subscribe();
                        }
                    } catch (NumberFormatException e) {
                        log.warn("NPC AI returned invalid index: {}", indexStr);
                    }
                }
            }
        } finally {
            processingNpcs.remove(npc.getId());
        }
    }
}
