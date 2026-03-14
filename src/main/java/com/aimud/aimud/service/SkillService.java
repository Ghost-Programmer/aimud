package com.aimud.aimud.service;

import com.aimud.aimud.model.Character;
import com.aimud.aimud.model.Skill;
import com.aimud.aimud.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {
    private final SkillRepository skillRepository;
    private final Random random = new Random();

    private static final double GLOBAL_GROWTH_RATE = 0.05; // Base chance multiplier
    private static final int MIN_CR_DELTA = -5;            // Too easy to learn from
    private static final double FAIL_LEARN_BONUS = 1.2;    // Learn faster from failure

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

    /**
     * Get Skill - Given a character and skill name, return the rank.
     */
    public int getSkillRank(Character character, String skillName) {
        return character.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .map(Skill::getRank)
                .findFirst()
                .orElse(0);
    }

    /**
     * Check Skill - Test if skill value should be increased by 1.
     * This method evaluates if the skill should improve and increments it if so.
     *
     * @param character   The character using the skill
     * @param skillName   The name of the skill
     * @param targetCr    The Challenge Rating of the target (NPC/Challenge)
     * @param wasSuccess  Whether the skill attempt succeeded in-game
     * @return Mono<Skill> The updated or original skill
     */
    public Mono<Skill> checkSkill(Character character, String skillName, int targetCr, boolean wasSuccess) {
        return character.getSkills().stream()
                .filter(s -> s.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .map(skill -> {
                    int playerLevel = (int) character.getChallengeRating();
                    if (shouldSkillImprove(skill.getRank(), playerLevel, targetCr, wasSuccess)) {
                        skill.setRank(skill.getRank() + 1);
                        log.info("Skill {} for character {} improved to rank {}", skillName, character.getId(), skill.getRank());
                        return skillRepository.save(skill);
                    }
                    return Mono.just(skill);
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
    public boolean shouldSkillImprove(int currentSkillLevel, int playerLevel, int targetCr, boolean wasSuccess) {
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
}
