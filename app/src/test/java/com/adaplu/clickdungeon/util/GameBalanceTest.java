package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.Monster;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Random;

@RunWith(RobolectricTestRunner.class)
public class GameBalanceTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("shop_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

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

    @Test
    public void loadShopItemsRespectsPersistedStock() {
        GameBalance.persistShopStock(context, "Healing Potion", 1);

        java.util.List<com.adaplu.clickdungeon.model.ShopItem> items = GameBalance.loadShopItems(context);
        assertNotNull(items);
        com.adaplu.clickdungeon.model.ShopItem potion = null;
        for (com.adaplu.clickdungeon.model.ShopItem item : items) {
            if ("Healing Potion".equals(item.getName())) {
                potion = item;
                break;
            }
        }

        assertNotNull(potion);
        assertEquals(1, potion.getStock());
    }

    @Test
    public void floorDifficultyScaleIncreasesByBand() {
        assertEquals(1f, GameBalance.getFloorDifficultyScale(0), 0.0001f);
        assertTrue(GameBalance.getFloorDifficultyScale(3) > 1f);
        assertTrue(GameBalance.getFloorDifficultyScale(8) > GameBalance.getFloorDifficultyScale(5));
        assertTrue(GameBalance.getFloorDifficultyScale(12) > GameBalance.getFloorDifficultyScale(10));
    }
}
