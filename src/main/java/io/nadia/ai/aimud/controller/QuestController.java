package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Quest;
import io.nadia.ai.aimud.model.QuestStep;
import io.nadia.ai.aimud.model.QuestDrop;
import io.nadia.ai.aimud.repository.QuestRepository;
import io.nadia.ai.aimud.repository.QuestStepRepository;
import io.nadia.ai.aimud.repository.QuestDropRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestRepository questRepository;
    private final QuestStepRepository questStepRepository;
    private final QuestDropRepository questDropRepository;

    public QuestController(QuestRepository questRepository, QuestStepRepository questStepRepository, QuestDropRepository questDropRepository) {
        this.questRepository = questRepository;
        this.questStepRepository = questStepRepository;
        this.questDropRepository = questDropRepository;
    }

    @GetMapping
    public Flux<Quest> getAllQuests() {
        return questRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Quest> getQuest(@PathVariable Long id) {
        return questRepository.findById(id);
    }

    @PostMapping
    public Mono<Quest> createQuest(@RequestBody Quest quest) {
        quest.setCreatedAt(LocalDateTime.now());
        quest.setModifiedAt(LocalDateTime.now());
        return questRepository.save(quest);
    }

    @PutMapping("/{id}")
    public Mono<Quest> updateQuest(@PathVariable Long id, @RequestBody Quest quest) {
        return questRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(quest.getName());
                    existing.setDescription(quest.getDescription());
                    existing.setRewardItemId(quest.getRewardItemId());
                    existing.setLevel(quest.getLevel());
                    existing.setModifiedAt(LocalDateTime.now());
                    return questRepository.save(existing);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteQuest(@PathVariable Long id) {
        return questRepository.deleteById(id);
    }

    // Quest Steps
    @GetMapping("/{questId}/steps")
    public Flux<QuestStep> getQuestSteps(@PathVariable Long questId) {
        return questStepRepository.findByQuestIdOrderByStepNumberAsc(questId);
    }

    @PostMapping("/{questId}/steps")
    public Mono<QuestStep> createQuestStep(@PathVariable Long questId, @RequestBody QuestStep step) {
        step.setQuestId(questId);
        step.setCreatedAt(LocalDateTime.now());
        step.setModifiedAt(LocalDateTime.now());
        return questStepRepository.save(step);
    }

    @DeleteMapping("/steps/{id}")
    public Mono<Void> deleteQuestStep(@PathVariable Long id) {
        return questStepRepository.deleteById(id);
    }

    // Quest Drops
    @GetMapping("/{questId}/drops")
    public Flux<QuestDrop> getQuestDrops(@PathVariable Long questId) {
        return questDropRepository.findByQuestId(questId);
    }

    @PostMapping("/{questId}/drops")
    public Mono<QuestDrop> createQuestDrop(@PathVariable Long questId, @RequestBody QuestDrop drop) {
        drop.setQuestId(questId);
        drop.setCreatedAt(LocalDateTime.now());
        drop.setModifiedAt(LocalDateTime.now());
        return questDropRepository.save(drop);
    }

    @DeleteMapping("/drops/{id}")
    public Mono<Void> deleteQuestDrop(@PathVariable Long id) {
        return questDropRepository.deleteById(id);
    }
}
