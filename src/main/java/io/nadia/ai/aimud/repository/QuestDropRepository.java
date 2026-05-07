package io.nadia.ai.aimud.repository;

import io.nadia.ai.aimud.model.QuestDrop;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface QuestDropRepository extends ReactiveCrudRepository<QuestDrop, Long> {
    @Query("SELECT * FROM quest_drops WHERE target_mobile_id = :mobileId OR target_faction_id = :factionId OR target_race_id = :raceId")
    Flux<QuestDrop> findRelevantDrops(Long mobileId, Long factionId, Long raceId);
    
    Flux<QuestDrop> findByQuestId(Long questId);
}
