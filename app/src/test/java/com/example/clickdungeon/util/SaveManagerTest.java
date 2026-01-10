package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SaveManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 4; i++) {
            context.getSharedPreferences("SaveSlot" + i, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
        }
    }

    @Test
    public void saveLoadAndDeleteRoundTrip() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Aeris", PlayerClass.WIZARD);
        profile.addExperience(120);
        profile.increaseIntelligence(1);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        grid[0][0] = new Tile(TileType.ENEMY, new Monster("Slime", 6, 2, 0, "🟢"));

        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1);
        saveManager.saveGame(1, profile, 3, 42, 7, grid, metadata);

        assertTrue(saveManager.isSlotOccupied(1));

        SaveManager.GameState state = saveManager.loadGame(1);
        assertNotNull(state);
        assertEquals(3, state.currentFloor);
        assertEquals(42, state.currentGold);
        assertEquals(7, state.currentPlatinum);
        assertEquals("Aeris", state.profile.getName());
        assertEquals(1, state.profile.getAvailableStatPoints());
        assertEquals(4, state.profile.getIntelligence());
        assertTrue(state.dungeonGrid[0][0].hasMonster());
        assertNotNull(state.metadata);
        assertEquals(-1, state.metadata.playerRow);

        saveManager.deleteSave(1);
        assertNull(saveManager.loadGame(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSlotIndexThrows() {
        SaveManager saveManager = new SaveManager(context);
        saveManager.isSlotOccupied(99);
    }
}
