package com.example.clickdungeon.util;

import androidx.annotation.NonNull;

import com.example.clickdungeon.model.Monster;

import java.util.Random;

/**
 * Centralizes reward calculations and encounter pacing so the same tuning is applied everywhere.
 */
public final class GameBalance {

    private static final int GOLD_VARIANCE_BOUND = 3;
    private static final int XP_VARIANCE_BOUND = 2;

    private GameBalance() {
    }

    public static int calculateGoldReward(@NonNull Monster monster,
                                          int floor,
                                          SettingsManager.Difficulty difficulty,
                                          Random random) {
        int base = Math.max(2, (monster.getMaxHP() / 3) + floor + (monster.getAttack() / 2));
        base += random.nextInt(GOLD_VARIANCE_BOUND);
        if (difficulty != null) {
            base = difficulty.scaleGoldReward(base);
        }
        return Math.max(1, base);
    }

    public static int calculateXpReward(@NonNull Monster monster,
                                        int floor,
                                        SettingsManager.Difficulty difficulty,
                                        Random random) {
        int base = Math.max(1, (monster.getMaxHP() / 2) + (floor / 2));
        base += random.nextInt(XP_VARIANCE_BOUND + 1);
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return Math.max(1, base);
    }

    public static int calculateGoldPile(int floor,
                                        SettingsManager.Difficulty difficulty,
                                        Random random) {
        int base = Math.max(1, floor + 1);
        base += random.nextInt(GOLD_VARIANCE_BOUND);
        if (difficulty == SettingsManager.Difficulty.HARDCORE) {
            base += 1; // Reward riskier play with slightly richer drops.
        } else if (difficulty == SettingsManager.Difficulty.CASUAL) {
            base = Math.max(1, base - 1);
        }
        return Math.max(1, base);
    }
}
