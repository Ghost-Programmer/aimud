package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.CharacterClass;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.model.Race;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.model.Store;
import io.nadia.ai.aimud.model.StoreItem;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.ServerSettings;
import io.nadia.ai.aimud.prayers.Prayer;
import io.nadia.ai.aimud.songs.Song;
import io.nadia.ai.aimud.spells.Spell;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class ConversationService {

    private final MobileService mobileService;
    private final RoomService roomService;

    private final CommandService commandService;
    private final FactionService factionService;
    private final ConfigService configService;
    private final SpellService spellService;
    private final SongService songService;
    private final PrayerService prayerService;
    private final QuestService questService;
    private final ObjectMapper objectMapper;
    private final org.springframework.ai.vectorstore.VectorStore vectorStore;
    private final org.springframework.ai.chat.client.ChatClient chatClient;
    private final StoreService storeService;

    /**
     * Constructs a new ConversationService.
     *
     * @param mobileService  the mobile service
     * @param roomService    the room service
     * @param commandService the command processing service
     * @param factionService the faction relation service
     * @param configService  the configuration service for races/classes
     * @param spellService   the spell service
     * @param songService    the song service
     * @param prayerService  the prayer service
     * @param questService   the quest service
     * @param vectorStore    the VectorStore
     * @param chatModel      the Ollama chat model
     * @param storeService   the store service
     */
    public ConversationService(MobileService mobileService, RoomService roomService,
            CommandService commandService,
            FactionService factionService, ConfigService configService, SpellService spellService,
            SongService songService, PrayerService prayerService, QuestService questService,
            org.springframework.ai.vectorstore.VectorStore vectorStore,
            org.springframework.ai.ollama.OllamaChatModel chatModel,
            @Lazy StoreService storeService) {
        this.mobileService = mobileService;

        this.roomService = roomService;

        this.commandService = commandService;
        this.factionService = factionService;
        this.configService = configService;
        this.spellService = spellService;
        this.songService = songService;
        this.prayerService = prayerService;
        this.questService = questService;
        this.objectMapper = new ObjectMapper();
        this.vectorStore = vectorStore;
        this.chatClient = org.springframework.ai.chat.client.ChatClient.builder(chatModel).build();
        this.storeService = storeService;
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
        List<Mobile> npcs = mobileService.getAvailableMobiles().stream().filter(m -> m.isUsesAi())
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

        List<Mobile> npcs = mobileService.findAllByRoomId(roomId).stream().filter(m -> m.isUsesAi())
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
        List<Mobile> players = mobileService.findAllByRoomId(room.getId()).stream()
                .filter(c -> c.getUserId() != null)
                .collect(Collectors.toList());

        // Skip inference engine entirely if there are zero players in the room!
        if (players.isEmpty()) {
            log.info("No players in room " + room.getId());
            processingNpcs.remove(npc.getId());
            return;
        }

        List<Mobile> npcsInRoom = mobileService.findAllByRoomId(room.getId()).stream()
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

        reactor.core.publisher.Mono<ServerSettings> settingsMono = configService.getServerSettings();
        reactor.core.publisher.Mono<Store> storeMono = npc.getStoreId() != null
                ? storeService.getStore(npc.getStoreId())
                : reactor.core.publisher.Mono.empty();

        reactor.core.publisher.Mono.zip(settingsMono, storeMono.defaultIfEmpty(new Store()))
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .subscribe(tuple -> {
                    ServerSettings settings = tuple.getT1();
                    Store store = tuple.getT2();

                    StringBuilder prompt = new StringBuilder();
                    prompt.append("You are an NPC in a Multi-User Dungeon (MUD).\n");
                    prompt.append("Current Game Time: Year ").append(settings.mudYear()).append(", Month ")
                            .append(settings.mudMonth()).append(", Day ").append(settings.mudDay()).append(", Hour ")
                            .append(settings.mudHour()).append("\n");
                    prompt.append("You are currently in: ").append(room.getName()).append("\n");
                    prompt.append("Room Description: ").append(room.getDescription()).append("\n\n");

                    prompt.append("Your Identity & Stats:\n");
                    prompt.append(
                            "(Note: Stats begin at 1 and can go up to 500. A stat of 10 is considered a normal player, while 500 is God-like.)\n");
                    prompt.append("- Name: ").append(npc.getName()).append("\n");
                    prompt.append("- Race: ").append(raceName).append("\n");
                    prompt.append("- Class: ").append(className).append("\n");
                    prompt.append("- Level (Challenge Rating): ").append((int) npc.getChallengeRating()).append("\n");
                    prompt.append("- HP: ").append(npc.getCurrentHp()).append(" / ").append(npc.getMaxHp())
                            .append("\n");
                    prompt.append("- Mana: ").append(npc.getCurrentMana()).append(" / ").append(npc.getMaxMana())
                            .append("\n");
                    prompt.append("- Strength: ").append(npc.getStrength())
                            .append(" (High = strong/powerful, Low = weak/feeble)\n");
                    prompt.append("- Dexterity: ").append(npc.getDexterity())
                            .append(" (High = agile/nimble, Low = clumsy/slow)\n");
                    prompt.append("- Constitution: ").append(npc.getConstitution())
                            .append(" (High = hardy/tough, Low = frail/sickly)\n");
                    prompt.append("- Intelligence: ").append(npc.getIntelligence())
                            .append(" (High = articulate/smart, Low = simple/dumb)\n");
                    prompt.append("- Wisdom: ").append(npc.getWisdom())
                            .append(" (High = insightful/calm, Low = unobservant/foolish)\n");
                    prompt.append("- Charisma: ").append(npc.getCharisma())
                            .append(" (High = charming/persuasive, Low = rude/abrasive)\n\n");

                    if (store.getId() != null) {
                        prompt.append("Store Details:\n");
                        prompt.append("- You run a store named: ").append(store.getName()).append("\n");
                        if (store.getDescription() != null && !store.getDescription().isBlank()) {
                            prompt.append("- Store Description: ").append(store.getDescription()).append("\n");
                        }
                        prompt.append("- Items available for sale in your store:\n");
                        if (store.getItems() != null && !store.getItems().isEmpty()) {
                            for (StoreItem si : store.getItems()) {
                                if (si.getItem() != null) {
                                    Item item = si.getItem();
                                    prompt.append("  * ").append(item.getName());
                                    if (item.getDescription() != null && !item.getDescription().isBlank()) {
                                        prompt.append(" (").append(item.getDescription()).append(")");
                                    }
                                    prompt.append("\n");
                                }
                            }
                        } else {
                            prompt.append("  * (No items currently in stock)\n");
                        }
                        prompt.append("\n");
                    }

                    prompt.append("Other entities present in the room:\n");
                    for (Mobile p : players) {
                        int rating = factionService.getFactionRatingSync(npc, p.getFactionId());
                        prompt.append("- ").append(p.getName()).append(" (Player, ID: ").append(p.getId())
                                .append(") [Faction Rating to you: ").append(rating)
                                .append("]\n");
                        if (p.getTarget() != null) {
                            prompt.append("  * Currently attacking: ").append(p.getTarget().getName()).append("\n");
                        }
                    }
                    for (Mobile n : npcsInRoom) {
                        int rating = factionService.getFactionRatingSync(npc, n.getFactionId());
                        prompt.append("- ").append(n.getName()).append(" (NPC, ID: ").append(n.getId())
                                .append(") [Faction Rating to you: ").append(rating)
                                .append("]\n");
                        if (n.getTarget() != null) {
                            prompt.append("  * Currently attacking: ").append(n.getTarget().getName()).append("\n");
                        }
                    }
                    prompt.append(
                            "\n* Note: Faction rating 80-100 is allied/friendly. 21-79 is neutral. 0-20 is hostile/hating.\n");

                    prompt.append("\nAvailable Actions:\n");
                    for (int i = 0; i < availableActions.size(); i++) {
                        prompt.append((i + 1)).append(". ").append(availableActions.get(i).getDescription())
                                .append("\n");
                    }

                    prompt.append("\nYour Decision Rules:\n");
                    prompt.append("1. Roleplay strictly. You are completely immersed in a high-fantasy world.\n");
                    prompt.append("2. READ the retrieved memory context carefully to understand what is going on.\n");
                    prompt.append("3. Output your response in strict JSON format.\n");
                    prompt.append(
                            "4. The JSON must contain 'actionIndices' (an array of numbers corresponding to the available actions you want to take) and optionally 'speech' (a string of what you want to say to the room/players).\n");
                    prompt.append("5. For example: {\"actionIndices\": [1,3], \"speech\": \"Greetings travelers!\"}\n");
                    prompt.append(
                            "6. If there is absolutely nothing to do and say, output EXACTLY: {\"actionIndices\": []}\n");
                    prompt.append("7. DO NOT output any text outside of the JSON block.\n");

                    org.springframework.ai.ollama.api.OllamaChatOptions options = org.springframework.ai.ollama.api.OllamaChatOptions
                            .builder()
                            .temperature(0.95)
                            .model("hermes3")
                            .build();

                    org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor ragAdvisor = org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
                            .builder(vectorStore)
                            .searchRequest(org.springframework.ai.vectorstore.SearchRequest.builder()
                                    .topK(50)
                                    .filterExpression("npcId == " + npc.getId())
                                    .build())
                            .build();

                    reactor.core.publisher.Mono<List<io.nadia.ai.aimud.model.Agent>> agentMono = (npc.getAgent() != null
                            && !npc.getAgent().isEmpty())
                                    ? configService.getAllAgents().filter(a -> npc.getAgent().equals(a.title()))
                                            .collectList()
                                    : reactor.core.publisher.Mono.just(java.util.Collections.emptyList());

                    agentMono.<String>flatMapMany(agents -> {
                        if (!agents.isEmpty()) {
                            prompt.insert(0, agents.get(0).content() + "\n\n\n");
                        }

                        return questService.getActiveQuestContextForNpc(npc, players)
                                .collectList()
                                .flatMapMany(questContexts -> {
                                    if (!questContexts.isEmpty()) {
                                        prompt.append("\nActive Quests for present players:\n");
                                        for (String ctx : questContexts) {
                                            prompt.append(ctx).append("\n");
                                        }
                                    }

                                    log.info("Processing conversation for NPC: {}", npc.getName());
                                    log.info("Prompt: \n {}", prompt.toString());

                                    return chatClient.mutate()
                                            .defaultOptions(options)
                                            .defaultAdvisors(ragAdvisor)
                                            .build()
                                            .prompt()
                                            .user(prompt.toString())
                                            .stream().content()
                                            .filter(text -> text != null && !text.isEmpty());
                                });
                    })
                            .reduce("", (a, b) -> a + String.valueOf(b))
                            .subscribe(
                                    response -> processAiResponse(npc, availableActions, response, players),
                                    error -> {
                                        log.error("Error generating conversation for NPC {}", npc.getName(), error);
                                        processingNpcs.remove(npc.getId());
                                    });
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
    private void processAiResponse(Mobile npc, List<MobileAction> availableActions, String response,
            List<Mobile> playersInRoom) {
        try {
            if (response == null || response.trim().isEmpty())
                return;
            String text = response.trim();
            log.info("NPC AI Response: {}", text);

            if (text.equalsIgnoreCase("IGNORE") || text.contains("IGNORE")) {
                return;
            }

            try {
                if (text.contains("```json")) {
                    text = text.substring(text.indexOf("```json") + 7);
                    if (text.contains("```")) {
                        text = text.substring(0, text.indexOf("```"));
                    }
                } else if (text.contains("```")) {
                    text = text.replaceAll("```", "");
                }

                JsonNode rootNode = objectMapper.readTree(text.trim());

                if (rootNode.has("speech") && !rootNode.get("speech").isNull()) {
                    String speech = rootNode.get("speech").asText();
                    if (!speech.isEmpty()) {
                        npc.getCommandQueue().add("say " + speech);
                        questService.processNpcSpeech(npc, playersInRoom, speech).subscribe();
                    }
                }

                if (rootNode.has("actionIndices") && rootNode.get("actionIndices").isArray()) {
                    for (JsonNode indexNode : rootNode.get("actionIndices")) {
                        int index = indexNode.asInt() - 1;
                        if (index >= 0 && index < availableActions.size()) {
                            String command = availableActions.get(index).getActionCommand();
                            log.info("NPC AI Action Execution queued: {}", command);
                            npc.getCommandQueue().add(command);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse JSON from AI: {}", text, e);
            }
        } finally {
            processingNpcs.remove(npc.getId());
        }
    }
}
