package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;

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
            SecurePreferences.get(context, "SaveSlot" + i)
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
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, "CAVERN");
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
        assertEquals("CAVERN", state.metadata.currentTerrain);

        saveManager.deleteSave(1);
        assertNull(saveManager.loadGame(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSlotIndexThrows() {
        SaveManager saveManager = new SaveManager(context);
        saveManager.isSlotOccupied(99);
    }

    @Test
    public void corruptSaveRestoresLastKnownGood() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Lia", PlayerClass.THIEF);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);

        saveManager.saveGame(0, profile, 2, 10, 1, grid, metadata);
        saveManager.saveGame(0, profile, 3, 15, 2, grid, metadata);

        SharedPreferences prefs = SecurePreferences.get(context, "SaveSlot0");
        prefs.edit()
                .putString("SaveBlob", "corrupt")
                .apply();

        SaveManager.GameState restored = saveManager.loadGame(0);
        assertNotNull(restored);
        assertEquals(2, restored.currentFloor);
        assertEquals(10, restored.currentGold);
        assertEquals(1, restored.currentPlatinum);
    }

    @Test
    public void schemaMismatchReturnsOutcome() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Lia", PlayerClass.THIEF);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);
        saveManager.saveGame(0, profile, 2, 10, 1, grid, metadata);

        SharedPreferences prefs = SecurePreferences.get(context, "SaveSlot0");
        prefs.edit()
                .putInt("SaveBlob" + PersistedBlobStore.SCHEMA_SUFFIX, 99)
                .apply();

        SaveManager.LoadOutcome outcome = saveManager.loadGameWithStatus(0);
        assertEquals(SaveManager.LoadStatus.SCHEMA_MISMATCH, outcome.status);
        assertNotNull(outcome.gameState);
    }

    @Test
    public void saveSnapshot_usesImmutableCopyOfState() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Nia", PlayerClass.KNIGHT);
        profile.setCurrentHP(5);
        Tile[][] grid = new Tile[2][2];
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        grid[0][0] = new Tile(TileType.ENEMY, new Monster("Slime", 5, 2, 0, "S"));
        grid[0][0].reveal();
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(1, 1, false, 0, -1, -1, 2, "CAVERN");

        SaveManager.SaveSnapshot snapshot = saveManager.buildSnapshot(
                profile, 2, 50, 3, grid, metadata);

        profile.setName("Mutated");
        profile.setCurrentHP(1);
        grid[0][0].setType(TileType.EMPTY);
        grid[0][0].setMonster(null);
        grid[1][1].setType(TileType.TRAP_FIRE);

        saveManager.saveSnapshot(0, snapshot);

        SaveManager.GameState state = saveManager.loadGame(0);
        assertNotNull(state);
        assertEquals("Nia", state.profile.getName());
        assertEquals(5, state.profile.getCurrentHP());
        assertEquals(2, state.currentFloor);
        assertEquals(50, state.currentGold);
        assertEquals(3, state.currentPlatinum);
        assertEquals(TileType.ENEMY, state.dungeonGrid[0][0].getType());
        assertTrue(state.dungeonGrid[0][0].hasMonster());
        assertEquals(TileType.EMPTY, state.dungeonGrid[1][1].getType());
    }

    @Test
    public void restoreBackup_returnsTrueWhenValidBackupExists() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Ria", PlayerClass.WIZARD);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);

        saveManager.saveGame(0, profile, 2, 10, 1, grid, metadata);
        saveManager.saveGame(0, profile, 3, 22, 4, grid, metadata);

        SharedPreferences prefs = SecurePreferences.get(context, "SaveSlot0");
        prefs.edit().putString("SaveBlob", "tampered").apply();

        assertTrue(saveManager.restoreBackup(0));
        SaveManager.GameState state = saveManager.loadGame(0);
        assertNotNull(state);
        assertEquals(2, state.currentFloor);
    }

    @Test
    public void restoreBackup_returnsFalseWhenNoBackupAvailable() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Ria", PlayerClass.WIZARD);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);

        saveManager.saveGame(0, profile, 2, 10, 1, grid, metadata);

        assertTrue(!saveManager.restoreBackup(0));
    }

    @Test
    public void migrateSave_afterSchemaMismatchRestoresLoadableState() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Mia", PlayerClass.THIEF);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(1, 2, true, 4, 1, 2, 3, "CAVERN");
        saveManager.saveGame(0, profile, 5, 55, 9, grid, metadata);

        SharedPreferences prefs = SecurePreferences.get(context, "SaveSlot0");
        prefs.edit()
                .putInt("SaveBlob" + PersistedBlobStore.SCHEMA_SUFFIX, 99)
                .apply();

        SaveManager.LoadOutcome mismatch = saveManager.loadGameWithStatus(0);
        assertEquals(SaveManager.LoadStatus.SCHEMA_MISMATCH, mismatch.status);
        assertNotNull(mismatch.gameState);

        saveManager.migrateSave(0, mismatch.gameState);

        SaveManager.LoadOutcome restored = saveManager.loadGameWithStatus(0);
        assertEquals(SaveManager.LoadStatus.OK, restored.status);
        assertNotNull(restored.gameState);
        assertEquals(5, restored.gameState.currentFloor);
    }

    @Test
    public void malformedBlob_returnsNullGameStateWithoutCrash() {
        SaveManager saveManager = new SaveManager(context);
        PersistedBlobStore.save(context, "SaveSlot0", "SaveBlob", 1, "{\"junk\":true}");

        SaveManager.LoadOutcome outcome = saveManager.loadGameWithStatus(0);
        assertEquals(SaveManager.LoadStatus.OK, outcome.status);
        assertNull(outcome.gameState);
    }
}
