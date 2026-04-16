package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Skill;
import io.nadia.ai.aimud.model.SkillRegistry;
import io.nadia.ai.aimud.repository.SkillRegistryRepository;
import io.nadia.ai.aimud.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {
    private static final double GLOBAL_GROWTH_RATE = 0.05; // Base chance multiplier
    private static final int MIN_CR_DELTA = -5;            // Too easy to learn from
    private static final double FAIL_LEARN_BONUS = 1.2;    // Learn faster from failure
    private static final Duration SKILL_LOOKUP_TIMEOUT = Duration.ofSeconds(5);
    private final SkillRepository skillRepository;
    private final SkillRegistryRepository skillRegistryRepository;
    private final Random random = new Random();
    private final DatabaseClient databaseClient;

    /**
     * Adds a new skill to a character with a starting rank of 1, if they don't already have it.
     *
     * @param character the mobile character
     * @param skillName the name of the skill to add
     * @return a {@link Mono} containing the newly created or existing skill
     */
    public Mono<Skill> addSkill(Mobile character, String skillName) {
        return character.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .map(Mono::just)
                .orElseGet(() -> {
                    Skill newSkill = Skill.builder()
                            .characterId(character.getId())
                            .name(skillName)
                            .rank(1)
                            .build();
                    log.info("Adding new skill {} for character {}", skillName, character.getId());
                    return skillRepository.save(newSkill)
                            .doOnNext(savedSkill -> character.getSkills().add(savedSkill));
                });
    }

    /**
     * Retrieves the string name of a skill by its database ID.
     *
     * @param skillId the ID of the skill
     * @return a {@link Mono} emitting the skill name
     */
    public Mono<String> getSkillNameById(Long skillId) {
        if (skillId == null) {
            return Mono.empty();
        }

        return skillRegistryRepository.findById(skillId)
                .map(SkillRegistry::getName);
    }


    /**
     * Retrieves the current rank of a specific skill for a character.
     *
     * @param mobile    the mobile character
     * @param skillName the name of the skill
     * @return the rank of the skill, or 0 if not possessed
     */
    public int getSkillRank(Mobile mobile, String skillName) {
        if (mobile.getSkills() == null) return 0;
        return mobile.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .map(Skill::getRank)
                .findFirst()
                .orElse(0);
    }

    /**
     * Tests if a skill value should be increased, and increments it if successful.
     *
     * @param mobile     the mobile using the skill
     * @param skillName  the name of the skill
     * @param targetCr   the Challenge Rating of the target (NPC/Challenge)
     * @param wasSuccess whether the skill attempt succeeded in-game
     * @return a {@link Mono} emitting the updated skill if it improved, or empty Mono if not
     */
    public Mono<Skill> checkSkill(Mobile mobile, String skillName, float targetCr, boolean wasSuccess) {
        if (mobile.getSkills() == null) return Mono.empty();

        return mobile.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .map(skill -> {
                    float playerLevel = mobile.getChallengeRating();
                    if (shouldSkillImprove(skill.getRank(), playerLevel, targetCr, wasSuccess)) {
                        skill.setRank(skill.getRank() + 1);
                        log.info("Skill {} for mobile {} improved to rank {}", skillName, mobile.getName(), skill.getRank());
                        if (mobile.getUserId() != null) {
                            return skillRepository.save(skill);
                        }
                        return Mono.just(skill);
                    }
                    return Mono.<Skill>empty();
                })
                .orElse(Mono.empty());
    }

    /**
     * Determines if a skill should improve based on usage probability math.
     *
     * @param currentSkillLevel the player's current proficiency
     * @param playerLevel       the player's character level (using CR as proxy)
     * @param targetCr          the Challenge Rating of the NPC
     * @param wasSuccess        whether the skill attempt actually succeeded in-game
     * @return true if the skill improved by 1 rank
     */
    public boolean shouldSkillImprove(int currentSkillLevel, float playerLevel, float targetCr, boolean wasSuccess) {
        // 1. Trivial Challenge Check
        // If the target is too weak relative to the player, no growth occurs.
        if (targetCr < (playerLevel + MIN_CR_DELTA)) {
            return false;
        }

        // 2. Already Capped
        if (currentSkillLevel >= 100) {
            return false;
        }

        // 3. Calculate Improvement Probability
        // Logic: Lower skills improve faster; Higher CR targets increase the window.
        double baseChance = (double) (targetCr + 10) / (currentSkillLevel + 10);

        // Apply Global Tuning
        double finalProbability = baseChance * GLOBAL_GROWTH_RATE;

        // 4. "Learn from Mistakes" Modifier
        // Failing a skill check against a tough opponent often teaches you more.
        if (!wasSuccess) {
            finalProbability *= FAIL_LEARN_BONUS;
        }

        // Apply Diminishing Returns
        finalProbability *= getDiminishingReturnMultiplier(currentSkillLevel);

        // 5. Hard Cap the probability to reasonable limits (e.g., 25% max chance per use)
        finalProbability = Math.min(finalProbability, 0.25);

        // 6. Roll the dice
        return random.nextDouble() < finalProbability;
    }

    /**
     * Logic for applying diminishing returns at very high skill levels.
     *
     * @param skillLevel the current skill proficiency level
     * @return the probability multiplier
     */
    public double getDiminishingReturnMultiplier(int skillLevel) {
        if (skillLevel < 75) return 1.0;
        if (skillLevel < 90) return 0.5;
        return 0.1; // Hardest to gain from 90 to 100
    }

    /**
     * Synchronously creates or updates a skill in the global registry table.
     *
     * @param spellSkillName the name of the skill
     * @param skillId        the database ID for the skill
     */
    public void createSkill(String spellSkillName, Long skillId) {
        Long rowsUpdated = await(databaseClient.sql("""
                        INSERT INTO skills_registry (id, name)
                        VALUES (:id, :name)
                        ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
                        """)
                .bind("id", skillId)
                .bind("name", spellSkillName)
                .fetch()
                .rowsUpdated(), "register skill in registry");

        log.info("Registered or updated skill in registry: {} (id={}, rowsUpdated={})", spellSkillName, skillId, rowsUpdated);
    }

    /**
     * Synchronously checks if a skill exists in the global registry.
     *
     * @param spellSkillName the name of the skill
     * @return true if the skill exists, false otherwise
     */
    public boolean existsSkill(String spellSkillName) {
        return Boolean.TRUE.equals(await(skillRegistryRepository.existsByName(spellSkillName), "check skill existence"));
    }

    /**
     * Internal helper to synchronously await a Mono, used for startup initialization tasks.
     *
     * @param mono      the Mono to subscribe to
     * @param operation the name of the operation for logging
     * @param <T>       the type of the Mono
     * @return the resolved result
     */
    private <T> T await(Mono<T> mono, String operation) {
        try {
            return mono.subscribeOn(Schedulers.boundedElastic())
                    .toFuture()
                    .get(SKILL_LOOKUP_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Skill operation interrupted while trying to " + operation, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Skill operation failed while trying to " + operation, cause);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Skill operation timed out while trying to " + operation, e);
        }
    }
}
