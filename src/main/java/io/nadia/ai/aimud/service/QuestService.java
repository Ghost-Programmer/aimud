package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.types.ObjectiveType;
import io.nadia.ai.aimud.types.QuestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class QuestService {

    private final QuestRepository questRepository;
    private final QuestStepRepository questStepRepository;
    private final CharacterQuestRepository characterQuestRepository;
    private final QuestDropRepository questDropRepository;
    private final ItemService itemService;
    private final CommunicationService communicationService;
    private final GameLogService gameLogService;

    public QuestService(QuestRepository questRepository,
                        QuestStepRepository questStepRepository,
                        CharacterQuestRepository characterQuestRepository,
                        QuestDropRepository questDropRepository,
                        ItemService itemService,
                        CommunicationService communicationService,
                        GameLogService gameLogService) {
        this.questRepository = questRepository;
        this.questStepRepository = questStepRepository;
        this.characterQuestRepository = characterQuestRepository;
        this.questDropRepository = questDropRepository;
        this.itemService = itemService;
        this.communicationService = communicationService;
        this.gameLogService = gameLogService;
    }

    public Mono<Void> checkTalkObjective(Mobile player, Mobile targetNpc) {
        if (player.getUserId() == null || targetNpc == null) return Mono.empty();
        return characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                .flatMap(cq -> questStepRepository.findById(cq.getCurrentStepId())
                        .filter(step -> step.getObjectiveType() == ObjectiveType.TALK_TO_NPC)
                        .filter(step -> step.getTargetMobileId() != null && step.getTargetMobileId().equals(targetNpc.getId()))
                        .flatMap(step -> advanceQuest(player, cq, step, targetNpc)))
                .then();
    }

    public Mono<Void> checkKillObjective(Mobile player, Mobile killedNpc) {
        if (player.getUserId() == null || killedNpc == null) return Mono.empty();
        return characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                .flatMap(cq -> questStepRepository.findById(cq.getCurrentStepId())
                        .filter(step -> {
                            if (step.getObjectiveType() == ObjectiveType.KILL_NPC) {
                                return step.getTargetMobileId() != null && step.getTargetMobileId().equals(killedNpc.getId());
                            } else if (step.getObjectiveType() == ObjectiveType.KILL_NPC_TYPE) {
                                boolean matchFaction = step.getTargetFactionId() == null || step.getTargetFactionId().equals(killedNpc.getFactionId());
                                boolean matchRace = step.getTargetRaceId() == null || step.getTargetRaceId().equals(killedNpc.getRaceId());
                                return matchFaction && matchRace;
                            }
                            return false;
                        })
                        .flatMap(step -> incrementProgress(player, cq, step)))
                .then();
    }

    public Mono<Boolean> checkGiveObjective(Mobile player, Mobile targetNpc, Item givenItem) {
        if (player.getUserId() == null || targetNpc == null || givenItem == null) return Mono.just(false);
        return characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                .flatMap(cq -> questStepRepository.findById(cq.getCurrentStepId())
                        .filter(step -> step.getObjectiveType() == ObjectiveType.GIVE_ITEM)
                        .filter(step -> step.getTargetMobileId() != null && step.getTargetMobileId().equals(targetNpc.getId()))
                        .filter(step -> step.getTargetItemId() != null && step.getTargetItemId().equals(givenItem.getId()))
                        .flatMap(step -> incrementProgress(player, cq, step).thenReturn(true))
                ).next().defaultIfEmpty(false);
    }

    private Mono<Void> incrementProgress(Mobile player, CharacterQuest cq, QuestStep step) {
        cq.setProgressCount(cq.getProgressCount() + 1);
        if (cq.getProgressCount() >= step.getTargetCount()) {
            return advanceQuest(player, cq, step, null);
        } else {
            return characterQuestRepository.save(cq)
                    .doOnNext(saved -> {
                        communicationService.sendTextMessage(player, "\n\nQuest Progress: " + saved.getProgressCount() + " / " + step.getTargetCount());
                    })
                    .then();
        }
    }

    private Mono<Void> advanceQuest(Mobile player, CharacterQuest cq, QuestStep currentStep, Mobile interactingNpc) {
        if (interactingNpc != null && currentStep.getInstructions() != null) {
            communicationService.sendTextMessage(player, "\n\n" + interactingNpc.getName() + " says, \"" + currentStep.getInstructions() + "\"");
        }

        return questStepRepository.findByQuestIdOrderByStepNumberAsc(cq.getQuestId())
                .filter(step -> step.getStepNumber() > currentStep.getStepNumber())
                .next()
                .flatMap(nextStep -> {
                    cq.setCurrentStepId(nextStep.getId());
                    cq.setProgressCount(0);
                    communicationService.sendTextMessage(player, "\n\nQuest Objective Updated!");
                    if (nextStep.getInstructions() != null && interactingNpc == null) {
                         communicationService.sendTextMessage(player, "Instructions: " + nextStep.getInstructions());
                    }
                    return characterQuestRepository.save(cq).then();
                })
                .switchIfEmpty(Mono.defer(() -> completeQuest(player, cq, interactingNpc)));
    }

    private Mono<Void> completeQuest(Mobile player, CharacterQuest cq, Mobile interactingNpc) {
        cq.setStatus(QuestStatus.COMPLETED);
        cq.setProgressCount(0);
        return characterQuestRepository.save(cq)
                .then(questRepository.findById(cq.getQuestId()))
                .flatMap(quest -> {
                    communicationService.sendTextMessage(player, "\n\n*** Quest Completed: " + quest.getName() + " ***");
                    
                    // Record Logs
                    Mono<Void> logMono = gameLogService.recordLog(player.getId(), false, player.getName() + " completed the quest: " + quest.getName() + ".").then();
                    if (quest.isWorldEvent() || (interactingNpc != null && interactingNpc.isWorldLog())) {
                        logMono = logMono.then(gameLogService.recordLog(player.getId(), true, player.getName() + " completed the epic quest: " + quest.getName() + ".").then());
                    }

                    Mono<Void> rewardMono = Mono.empty();
                    if (quest.getRewardItemId() != null) {
                        rewardMono = itemService.getItem(quest.getRewardItemId())
                                .flatMap(item -> {
                                    if (player.getInventory() != null) {
                                        player.getInventory().add(item);
                                    }
                                    communicationService.sendTextMessage(player, "You receive a reward: " + item.getName());
                                    return Mono.empty();
                                });
                    }
                    return logMono.then(rewardMono);
                });
    }

    public Mono<Void> startQuest(Mobile player, Long questId) {
        return characterQuestRepository.findByCharacterIdAndQuestId(player.getId(), questId)
                .switchIfEmpty(Mono.defer(() -> {
                    return questStepRepository.findByQuestIdOrderByStepNumberAsc(questId)
                            .next()
                            .flatMap(firstStep -> {
                                CharacterQuest cq = new CharacterQuest();
                                cq.setCharacterId(player.getId());
                                cq.setQuestId(questId);
                                cq.setCurrentStepId(firstStep.getId());
                                cq.setStatus(QuestStatus.ACTIVE);
                                cq.setProgressCount(0);
                                return characterQuestRepository.save(cq)
                                        .doOnNext(saved -> {
                                            communicationService.sendTextMessage(player, "\n\n*** New Quest Started! ***");
                                            if (firstStep.getInstructions() != null) {
                                                communicationService.sendTextMessage(player, "Instructions: " + firstStep.getInstructions());
                                            }
                                        });
                            });
                }))
                .then();
    }
    
    public Flux<Item> rollAndGetDrops(Mobile deceased) {
        Long npcId = deceased.getUserId() == null ? deceased.getId() : null; // Only NPCs drop quest items
        if (npcId == null) return Flux.empty();
        return questDropRepository.findRelevantDrops(npcId, deceased.getFactionId(), deceased.getRaceId())
                .filter(drop -> Math.random() <= drop.getDropChance())
                .flatMap(drop -> itemService.getItem(drop.getItemId()))
                .onErrorResume(e -> Flux.empty());
    }

    public Flux<String> getActiveQuestContextForNpc(Mobile npc, java.util.List<Mobile> playersInRoom) {
        if (playersInRoom == null || playersInRoom.isEmpty()) return Flux.empty();
        
        return Flux.fromIterable(playersInRoom)
            .flatMap(player -> characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                .flatMap(cq -> questStepRepository.findById(cq.getCurrentStepId())
                    .filter(step -> step.getObjectiveType() == ObjectiveType.TALK_TO_NPC)
                    .filter(step -> step.getTargetMobileId() != null && step.getTargetMobileId().equals(npc.getId()))
                    .flatMap(step -> questRepository.findById(cq.getQuestId())
                        .map(quest -> String.format(
                            "- Player %s is on quest '%s'. To advance their quest, you must speak to them and mention: \"%s\"", 
                            player.getName(), quest.getName(), step.getInstructions()))
                    )
                )
            );
    }

    public Mono<Void> processNpcSpeech(Mobile npc, java.util.List<Mobile> playersInRoom, String speech) {
        if (playersInRoom == null || playersInRoom.isEmpty() || speech == null) return Mono.empty();
        
        return Flux.fromIterable(playersInRoom)
            .flatMap(player -> characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                .flatMap(cq -> questStepRepository.findById(cq.getCurrentStepId())
                    .filter(step -> step.getObjectiveType() == ObjectiveType.TALK_TO_NPC)
                    .filter(step -> step.getTargetMobileId() != null && step.getTargetMobileId().equals(npc.getId()))
                    .filter(step -> step.getInstructions() != null && speech.toLowerCase().contains(step.getInstructions().toLowerCase()))
                    .flatMap(step -> advanceQuest(player, cq, step, npc))
                )
            ).then();
    }
}
