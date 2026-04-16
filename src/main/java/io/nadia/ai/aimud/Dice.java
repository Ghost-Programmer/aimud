package io.nadia.ai.aimud;

import lombok.Getter;

import java.util.Random;

public class Dice {

    private final Random random = new Random();

    @Getter
    int total = 0;

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
