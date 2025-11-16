package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.clickdungeon.model.Monster;

import org.junit.Test;

import java.util.Random;

public class GameBalanceTest {

    @Test
    public void hardcoreDifficultyBoostsRewards() {
        Monster monster = new Monster("Wraith", 18, 6, 3, "👻");

        int casualGold = GameBalance.calculateGoldReward(monster, 2, SettingsManager.Difficulty.CASUAL, new Random(42));
        int hardcoreGold = GameBalance.calculateGoldReward(monster, 2, SettingsManager.Difficulty.HARDCORE, new Random(42));

        assertTrue(hardcoreGold > casualGold);

        int casualXp = GameBalance.calculateXpReward(monster, 2, SettingsManager.Difficulty.CASUAL, new Random(42));
        int hardcoreXp = GameBalance.calculateXpReward(monster, 2, SettingsManager.Difficulty.HARDCORE, new Random(42));

        assertTrue(hardcoreXp > casualXp);
    }

    @Test
    public void goldPileScalingRespondsToFloorAndDifficulty() {
        int floorOneCasual = GameBalance.calculateGoldPile(1, SettingsManager.Difficulty.CASUAL, new Random(99));
        int floorFiveHardcore = GameBalance.calculateGoldPile(5, SettingsManager.Difficulty.HARDCORE, new Random(99));

        assertTrue(floorFiveHardcore > floorOneCasual);

        int repeated = GameBalance.calculateGoldPile(3, SettingsManager.Difficulty.NORMAL, new Random(55));
        assertEquals(repeated, GameBalance.calculateGoldPile(3, SettingsManager.Difficulty.NORMAL, new Random(55)));
    }
}
