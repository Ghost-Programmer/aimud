package com.aimud.aimud.service;

import com.aimud.aimud.model.Faction;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.repository.FactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactionService {

    private final FactionRepository factionRepository;
    private final DatabaseClient databaseClient;

    // Cache: mobileId -> Map<targetFactionId, rating>
    private final Map<Long, Map<Long, Integer>> ratingCache = new ConcurrentHashMap<>();

    public Mono<Faction> getFaction(Long id) {
        if (id == null) return Mono.empty();
        return factionRepository.findById(id);
    }

    public Mono<Integer> getFactionRating(Mobile mobile, Long targetFactionId) {
        if (targetFactionId == null || mobile.getFactionId() == null) {
            return Mono.just(50); // Neutral default
        }
        
        if (targetFactionId.equals(mobile.getFactionId())) {
            return Mono.just(100);
        }
        
        Long mId = mobile.getId();
        if (ratingCache.containsKey(mId) && ratingCache.get(mId).containsKey(targetFactionId)) {
            return Mono.just(ratingCache.get(mId).get(targetFactionId));
        }

        return databaseClient.sql("SELECT rating FROM mobile_factions WHERE mobile_id = :mobileId AND faction_id = :factionId")
                .bind("mobileId", mId)
                .bind("factionId", targetFactionId)
                .map((row, rowMetadata) -> row.get("rating", Integer.class))
                .first()
                .defaultIfEmpty(50)
                .doOnNext(rating -> {
                    ratingCache.computeIfAbsent(mId, k -> new ConcurrentHashMap<>()).put(targetFactionId, rating);
                });
    }

    public int getFactionRatingSync(Mobile mobile, Long targetFactionId) {
        if (targetFactionId == null || mobile.getFactionId() == null) {
            return 50; 
        }
        if (targetFactionId.equals(mobile.getFactionId())) {
            return 100;
        }
        
        Long mId = mobile.getId();
        if (ratingCache.containsKey(mId) && ratingCache.get(mId).containsKey(targetFactionId)) {
            return ratingCache.get(mId).get(targetFactionId);
        }
        
        // Blocking fallback - should ideally rarely happen inside tight loops due to preemptive loading
        try {
            Integer rating = databaseClient.sql("SELECT rating FROM mobile_factions WHERE mobile_id = :mobileId AND faction_id = :factionId")
                .bind("mobileId", mId)
                .bind("factionId", targetFactionId)
                .map((row, rowMetadata) -> row.get("rating", Integer.class))
                .first()
                .defaultIfEmpty(50)
                .block();
            
            int r = rating != null ? rating : 50;
            ratingCache.computeIfAbsent(mId, k -> new ConcurrentHashMap<>()).put(targetFactionId, r);
            return r;
        } catch (Exception e) {
            log.error("Error blocking for faction rating", e);
            return 50;
        }
    }

    public reactor.core.publisher.Flux<Faction> findAllFactions() {
        return factionRepository.findAll();
    }

    public Mono<Map<Long, Integer>> getMobileRatings(Long mobileId) {
        if (mobileId == null) return Mono.just(Map.of());
        return databaseClient.sql("SELECT faction_id, rating FROM mobile_factions WHERE mobile_id = :mobileId")
                .bind("mobileId", mobileId)
                .map((row, rowMetadata) -> java.util.Map.entry(
                        row.get("faction_id", Long.class),
                        row.get("rating", Integer.class)
                ))
                .all()
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public Mono<Void> updateMobileRatings(Long mobileId, Map<Long, Integer> ratings) {
        if (mobileId == null || ratings == null || ratings.isEmpty()) return Mono.empty();
        
        return reactor.core.publisher.Flux.fromIterable(ratings.entrySet())
                .flatMap(entry -> {
                    Long targetFactionId = entry.getKey();
                    int newRating = Math.max(1, Math.min(100, entry.getValue()));
                    
                    ratingCache.computeIfAbsent(mobileId, k -> new ConcurrentHashMap<>()).put(targetFactionId, newRating);
                    
                    return databaseClient.sql("""
                            INSERT INTO mobile_factions (mobile_id, faction_id, rating) 
                            VALUES (:mobileId, :factionId, :rating) 
                            ON CONFLICT (mobile_id, faction_id) 
                            DO UPDATE SET rating = :rating
                            """)
                            .bind("mobileId", mobileId)
                            .bind("factionId", targetFactionId)
                            .bind("rating", newRating)
                            .then();
                })
                .then();
    }

    public Mono<Void> modifyFactionRating(Mobile mobile, Long targetFactionId, int amount) {
        if (targetFactionId == null || mobile.getId() == null) {
            return Mono.empty();
        }

        return getFactionRating(mobile, targetFactionId)
                .flatMap(currentRating -> {
                    int newRating = Math.max(1, Math.min(100, currentRating + amount));
                    
                    ratingCache.computeIfAbsent(mobile.getId(), k -> new ConcurrentHashMap<>()).put(targetFactionId, newRating);
                    
                    return databaseClient.sql("""
                            INSERT INTO mobile_factions (mobile_id, faction_id, rating) 
                            VALUES (:mobileId, :factionId, :rating) 
                            ON CONFLICT (mobile_id, faction_id) 
                            DO UPDATE SET rating = :rating
                            """)
                            .bind("mobileId", mobile.getId())
                            .bind("factionId", targetFactionId)
                            .bind("rating", newRating)
                            .then();
                });
    }

    public Mono<Void> handleKillPenalty(Mobile attacker, Mobile victim) {
        if (victim.getFactionId() != null) {
            return modifyFactionRating(attacker, victim.getFactionId(), -5); 
        }
        return Mono.empty();
    }
}
