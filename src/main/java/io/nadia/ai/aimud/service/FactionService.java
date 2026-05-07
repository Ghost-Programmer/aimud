package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Faction;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.repository.FactionRepository;
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

    /**
     * Clears the faction rating cache.
     */
    public void clearCache() {
        log.info("Clearing faction rating cache");
        ratingCache.clear();
    }

    /**
     * Retrieves a faction by its ID.
     *
     * @param id the ID of the faction
     * @return a {@link Mono} containing the faction, if found
     */
    public Mono<Faction> getFaction(Long id) {
        if (id == null) return Mono.empty();
        return factionRepository.findById(id);
    }

    /**
     * Retrieves the faction rating between a mobile entity and a target faction asynchronously.
     *
     * @param mobile          the character checking their standing
     * @param targetFactionId the ID of the target faction
     * @return a {@link Mono} containing the faction rating (0-100)
     */
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

        return factionRepository.findById(targetFactionId)
                .flatMap(faction -> {
                    if ("ADMIN".equals(faction.getName()) && mobile.getUserId() != null) {
                        return databaseClient.sql("SELECT role FROM users WHERE id = :userId")
                                .bind("userId", mobile.getUserId())
                                .map((row, metadata) -> row.get("role", String.class))
                                .first()
                                .map(role -> "MUD_ADMIN".equals(role) ? 100 : -1)
                                .defaultIfEmpty(-1);
                    }
                    return Mono.just(-1);
                })
                .defaultIfEmpty(-1)
                .flatMap(adminCheck -> {
                    if (adminCheck != -1) {
                        return Mono.just(adminCheck);
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
                });
    }

    /**
     * Retrieves the faction rating synchronously, falling back to a blocking database call if necessary.
     *
     * @param mobile          the character checking their standing
     * @param targetFactionId the ID of the target faction
     * @return the numerical faction rating
     */
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
            Faction targetFaction = factionRepository.findById(targetFactionId).block();
            if (targetFaction != null && "ADMIN".equals(targetFaction.getName()) && mobile.getUserId() != null) {
                String role = databaseClient.sql("SELECT role FROM users WHERE id = :userId")
                        .bind("userId", mobile.getUserId())
                        .map((row, metadata) -> row.get("role", String.class))
                        .first()
                        .block();
                if ("MUD_ADMIN".equals(role)) {
                    return 100;
                }
            }

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

    /**
     * Retrieves all available factions.
     *
     * @return a {@link Flux} emitting all factions
     */
    public reactor.core.publisher.Flux<Faction> findAllFactions() {
        return factionRepository.findAll();
    }

    /**
     * Creates a new faction.
     *
     * @param faction the faction to create
     * @return a {@link Mono} containing the saved faction
     */
    public Mono<Faction> createFaction(Faction faction) {
        return factionRepository.save(faction);
    }

    /**
     * Updates an existing faction.
     *
     * @param id      the ID of the faction to update
     * @param faction the updated faction details
     * @return a {@link Mono} containing the updated faction, if found
     */
    public Mono<Faction> updateFaction(Long id, Faction faction) {
        return factionRepository.findById(id)
                .flatMap(existing -> {
                    existing.setName(faction.getName());
                    existing.setDescription(faction.getDescription());
                    return factionRepository.save(existing);
                });
    }

    /**
     * Deletes a faction by its ID and removes it from any mobiles.
     *
     * @param id the ID of the faction to delete
     * @return a {@link Mono} indicating completion
     */
    public Mono<Void> deleteFaction(Long id) {
        return databaseClient.sql("UPDATE mobiles SET faction_id = NULL WHERE faction_id = :id")
                .bind("id", id)
                .then()
                .then(factionRepository.deleteById(id));
    }

    /**
     * Retrieves a map of all specific faction ratings for a given mobile ID.
     *
     * @param mobileId the ID of the mobile entity
     * @return a {@link Mono} containing a map of faction IDs to integer ratings
     */
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

    /**
     * Updates one or more faction ratings for a mobile entity.
     *
     * @param mobileId the ID of the mobile entity
     * @param ratings  a map of target faction IDs to their new ratings
     * @return a {@link Mono} indicating completion
     */
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

    /**
     * Modifies an existing faction rating by a specified amount (adding or subtracting).
     *
     * @param mobile          the character experiencing the reputation change
     * @param targetFactionId the faction being influenced
     * @param amount          the numerical modifier to apply
     * @return a {@link Mono} indicating completion
     */
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

    /**
     * Hands out a reputation penalty for killing a member of a given faction.
     *
     * @param attacker the entity that dealt the killing blow
     * @param victim   the entity that was killed
     * @return a {@link Mono} indicating completion
     */
    public Mono<Void> handleKillPenalty(Mobile attacker, Mobile victim) {
        if (victim.getFactionId() != null) {
            return modifyFactionRating(attacker, victim.getFactionId(), -5); 
        }
        return Mono.empty();
    }
}
