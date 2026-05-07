package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.repository.CharacterQuestRepository;
import io.nadia.ai.aimud.repository.QuestRepository;
import io.nadia.ai.aimud.repository.QuestStepRepository;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.types.QuestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@MudCommand(name = "quest")
@Slf4j
public class QuestCommand implements Command {

    private final CharacterQuestRepository characterQuestRepository;
    private final QuestRepository questRepository;
    private final QuestStepRepository questStepRepository;
    private final CommunicationService communicationService;

    public QuestCommand(CharacterQuestRepository characterQuestRepository, QuestRepository questRepository, QuestStepRepository questStepRepository, CommunicationService communicationService) {
        this.characterQuestRepository = characterQuestRepository;
        this.questRepository = questRepository;
        this.questStepRepository = questStepRepository;
        this.communicationService = communicationService;
    }

    @Override
    public Mono<Void> execute(Mobile player, String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length == 1 || parts[1].equalsIgnoreCase("log")) {
            return characterQuestRepository.findByCharacterIdAndStatus(player.getId(), QuestStatus.ACTIVE)
                    .flatMap(cq -> questRepository.findById(cq.getQuestId())
                            .zipWith(questStepRepository.findById(cq.getCurrentStepId()))
                            .map(tuple -> String.format("- [%d] %s: %s (Progress: %d/%d)", 
                                    cq.getQuestId(), 
                                    tuple.getT1().getName(), 
                                    tuple.getT2().getInstructions(), 
                                    cq.getProgressCount(), 
                                    tuple.getT2().getTargetCount())))
                    .collectList()
                    .flatMap(lines -> {
                        if (lines.isEmpty()) {
                            communicationService.sendTextMessage(player, "You have no active quests.");
                        } else {
                            communicationService.sendTextMessage(player, "\n*** Active Quests ***\n" + String.join("\n", lines));
                        }
                        return Mono.empty();
                    });
        } else if (parts.length == 3 && parts[1].equalsIgnoreCase("abandon")) {
            try {
                Long questId = Long.parseLong(parts[2]);
                return characterQuestRepository.findByCharacterIdAndQuestId(player.getId(), questId)
                        .filter(cq -> cq.getStatus() == QuestStatus.ACTIVE)
                        .flatMap(cq -> {
                            cq.setStatus(QuestStatus.FAILED);
                            return characterQuestRepository.save(cq)
                                .doOnSuccess(saved -> communicationService.sendTextMessage(player, "Quest abandoned."));
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            communicationService.sendTextMessage(player, "You don't have an active quest with that ID.");
                            return Mono.empty();
                        }))
                        .then();
            } catch (NumberFormatException e) {
                communicationService.sendTextMessage(player, "Invalid quest ID.");
                return Mono.empty();
            }
        } else {
            communicationService.sendTextMessage(player, "Syntax: quest [log|abandon <id>]");
            return Mono.empty();
        }
    }

    @Override
    public String getDescription() {
        return "View active quests and manage your quest log.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: quest\n        quest log\n        quest abandon <id>\n\nUse this command to view your active quest progress or abandon a quest you no longer wish to complete.";
    }
}
