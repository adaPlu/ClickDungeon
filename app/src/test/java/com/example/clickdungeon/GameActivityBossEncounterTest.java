package com.example.clickdungeon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.util.AchievementManager;
import com.example.clickdungeon.util.BossCatalog;
import com.example.clickdungeon.util.SecurePreferences;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class GameActivityBossEncounterTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Test", PlayerClass.KNIGHT)))
                .commit();
    }

    @Test
    public void bossFloorPlacesBossMonster() {
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        ReflectionHelpers.setField(activity, "currentFloor", 5);
        ReflectionHelpers.callInstanceMethod(activity, "generateDungeon");

        Tile[][] grid = ReflectionHelpers.getField(activity, "dungeonGrid");
        boolean foundBoss = false;
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile != null && tile.hasMonster() && tile.getMonster().isBoss()) {
                    foundBoss = true;
                }
            }
        }
        assertTrue(foundBoss);
    }

    @Test
    public void bossVictoryUnlocksAchievement() {
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        Monster boss = BossCatalog.createBossForFloor(5);
        Tile bossTile = new Tile(com.example.clickdungeon.model.TileType.ENEMY, boss);

        ReflectionHelpers.setField(activity, "activeCombatTile", bossTile);
        ReflectionHelpers.setField(activity, "pendingCombatGoldReward", 0);
        ReflectionHelpers.setField(activity, "pendingCombatXpReward", 0);

        activity.onCombatVictory(boss);

        assertTrue(AchievementManager.isUnlocked(activity, activity.getString(R.string.achievement_boss_slayer_title)));
    }

    @Test
    public void bossDefeatDoesNotUnlockAchievement() {
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        Monster boss = BossCatalog.createBossForFloor(5);
        assertFalse(AchievementManager.isUnlocked(activity, activity.getString(R.string.achievement_boss_slayer_title)));
        activity.onCombatDefeat();
        assertFalse(AchievementManager.isUnlocked(activity, activity.getString(R.string.achievement_boss_slayer_title)));
    }
}
