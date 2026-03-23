package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.repository.SkillRegistryRepository;
import com.aimud.aimud.repository.SkillRepository;
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
    private final SkillRepository skillRepository;
    private final SkillRegistryRepository skillRegistryRepository;
    private final Random random = new Random();

    private static final double GLOBAL_GROWTH_RATE = 0.05; // Base chance multiplier
    private static final int MIN_CR_DELTA = -5;            // Too easy to learn from
    private static final double FAIL_LEARN_BONUS = 1.2;    // Learn faster from failure
    private final DatabaseClient databaseClient;

    private static final Duration SKILL_LOOKUP_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Add skill - Create a new skill assigned to a character with a value of 1,
     * if the character does not have the skill.
     */
    public Mono<Skill> addSkill(Character character, String skillName) {
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

    public Mono<String> getSkillNameById(Long skillId) {
        if (skillId == null) {
            return Mono.empty();
        }

        return skillRegistryRepository.findById(skillId)
                .map(com.aimud.aimud.model.SkillRegistry::getName);
    }


    /**
     * Get Skill - Given a mobile and skill name, return the rank.
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
     * Check Skill - Test if skill value should be increased by 1.
     * This method evaluates if the skill should improve and increments it if so.
     *
     * @param mobile      The mobile using the skill
     * @param skillName   The name of the skill
     * @param targetCr    The Challenge Rating of the target (NPC/Challenge)
     * @param wasSuccess  Whether the skill attempt succeeded in-game
     * @return Mono<Skill> The updated skill if it improved, or empty Mono if not.
     */
    public Mono<Skill> checkSkill(Mobile mobile, String skillName, float targetCr, boolean wasSuccess) {
        if (mobile.getSkills() == null) return Mono.empty();

        return mobile.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .map(skill -> {
                    float playerLevel =  mobile.getChallengeRating();
                    if (shouldSkillImprove(skill.getRank(), playerLevel, targetCr, wasSuccess)) {
                        skill.setRank(skill.getRank() + 1);
                        log.info("Skill {} for mobile {} improved to rank {}", skillName, mobile.getName(), skill.getRank());
                        if (mobile instanceof Character) {
                            return skillRepository.save(skill);
                        }
                        return Mono.just(skill);
                    }
                    return Mono.<Skill>empty();
                })
                .orElse(Mono.empty());
    }

    /**
     * Determines if a skill should improve based on usage.
     *
     * @param currentSkillLevel The player's current proficiency (0-100)
     * @param playerLevel       The player's character level (using CR as proxy)
     * @param targetCr          The Challenge Rating of the NPC
     * @param wasSuccess        Whether the skill attempt actually succeeded in-game
     * @return true if the skill improved by 1%
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
     * Could be used to calculate how many "points" are needed to reach the next % rank.
     */
    public double getDiminishingReturnMultiplier(int skillLevel) {
        if (skillLevel < 75) return 1.0;
        if (skillLevel < 90) return 0.5;
        return 0.1; // Hardest to gain from 90 to 100
    }

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

    public boolean existsSkill(String spellSkillName) {
        return Boolean.TRUE.equals(await(skillRegistryRepository.existsByName(spellSkillName), "check skill existence"));
    }

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
