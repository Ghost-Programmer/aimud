package io.nadia.ai.aimud;

import lombok.Getter;

import java.util.Random;

/**
 * Core utility class for generating randomized numeric values based on traditional tabletop
 * Dungeons &amp; Dragons dice mechanics. Specifically computes NDX (e.g., 2d6 or 1d20) rolls.
 */
public class Dice {

    private final Random random = new Random();

    /**
     * The finalized computed sum of all dice rolled.
     */
    @Getter
    int total = 0;

    /**
     * Rolls a specific number of multi-sided dice and caches the cumulative sum in the total field.
     *
     * @param number the total quantity of dice to roll
     * @param size   the number of numeric sides per die (e.g., 6 for a d6)
     * @throws IllegalArgumentException if the dice number or size is less than 1
     */
    public Dice(int number, int size) {
        if (number <= 0) {
            throw new IllegalArgumentException("Number of dice must be positive");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("Dice size must be positive");
        }

        for (int i = 0; i < number; i++) {
            total += random.nextInt(size) + 1;
        }
    }

}
