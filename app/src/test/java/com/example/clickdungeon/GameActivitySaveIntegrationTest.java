package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.widget.GridLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.model.TerrainType;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.SaveManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;
import org.robolectric.util.ReflectionHelpers;
import com.example.clickdungeon.util.SecurePreferences;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class GameActivitySaveIntegrationTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "SaveSlot0").edit().clear().commit();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
        SaveManager.setTestSaveListener(null);
    }

    @Test
    public void loadAndPersist_withSaveSlot_roundTripsThroughSaveManager() throws Exception {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Nova", PlayerClass.WIZARD);
        profile.addExperience(120);
        profile.increaseIntelligence(1);
        Tile[][] grid = new Tile[][]{
                {new Tile(TileType.EMPTY), new Tile(TileType.ENEMY)},
                {new Tile(TileType.GOLD), new Tile(TileType.TRAP_FIRE)}
        };
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 4, null);
        saveManager.saveGame(0, profile, 4, 77, 15, grid, metadata);

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();
        activity.setSaveExecutorForTest(Runnable::run);
        activity.setSaveDebounceMsForTest(0);

        int currentFloor = (int) getFieldValue(activity, "currentFloor");
        int currentGold = (int) getFieldValue(activity, "currentGold");
        int currentPlatinum = (int) getFieldValue(activity, "currentPlatinum");
        Tile[][] activeGrid = (Tile[][]) getFieldValue(activity, "dungeonGrid");

        assertEquals(4, currentFloor);
        assertEquals(77, currentGold);
        assertEquals(15, currentPlatinum);
        assertEquals(1, ((CharacterProfile) getFieldValue(activity, "profile")).getAvailableStatPoints());
        assertNotNull(activeGrid);
        assertEquals(TileType.ENEMY, activeGrid[0][1].getType());

        setFieldValue(activity, "currentGold", 125);
        setFieldValue(activity, "currentPlatinum", 35);

        controller.pause();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        SaveManager.GameState updated = saveManager.loadGame(0);
        assertNotNull(updated);
        assertEquals(125, updated.currentGold);
        assertEquals(35, updated.currentPlatinum);
        assertEquals(4, updated.currentFloor);
    }

    @Test
    public void clickingKeyTileAddsKeyToInventory() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("KeyHunter", PlayerClass.THIEF);
        Tile[][] grid = buildGridWithKey(TileType.SMALL_KEY, "Small Key");
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);
        saveManager.saveGame(0, profile, 1, 0, 0, grid, metadata);

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();

        GridLayout gridLayout = activity.findViewById(R.id.gridDungeon);
        View firstTile = gridLayout.getChildAt(0);
        activity.runOnUiThread(firstTile::performClick);
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertEquals(1, InventoryManager.getItemQuantity(activity, "SMALL KEY"));
    }

    @Test
    public void tileClicks_debounceSavesToSingleCommit() throws Exception {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Saver", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);
        saveManager.saveGame(0, profile, 1, 0, 0, grid, metadata);

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();
        activity.setSaveExecutorForTest(Runnable::run);
        activity.setSaveDebounceMsForTest(50);

        AtomicInteger saveCount = new AtomicInteger(0);
        SaveManager.setTestSaveListener(slotIndex -> saveCount.incrementAndGet());
        saveCount.set(0);

        setFieldValue(activity, "dungeonGrid", grid);
        setFieldValue(activity, "safeTilesToReveal", 25);
        setFieldValue(activity, "revealedSafeTiles", 0);
        ReflectionHelpers.callInstanceMethod(activity, "renderGrid");
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        GridLayout gridLayout = activity.findViewById(R.id.gridDungeon);
        View firstTile = gridLayout.getChildAt(0);
        View secondTile = gridLayout.getChildAt(1);
        activity.runOnUiThread(firstTile::performClick);
        activity.runOnUiThread(secondTile::performClick);

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(60, TimeUnit.MILLISECONDS);

        assertEquals(1, saveCount.get());
        SaveManager.setTestSaveListener(null);
        controller.pause();
    }

    @Test
    public void requestSave_deferredCoalescesRepeatedCalls() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        GameActivity activity = controller.setup().get();
        activity.setSaveExecutorForTest(Runnable::run);
        activity.setSaveDebounceMsForTest(50);

        AtomicInteger saveCount = new AtomicInteger(0);
        SaveManager.setTestSaveListener(slotIndex -> saveCount.incrementAndGet());
        saveCount.set(0);

        invokeRequestSave(activity, "OTHER");
        invokeRequestSave(activity, "OTHER");
        invokeRequestSave(activity, "OTHER");

        assertEquals(0, saveCount.get());
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(60, TimeUnit.MILLISECONDS);
        assertEquals(1, saveCount.get());

        SaveManager.setTestSaveListener(null);
        controller.pause();
    }

    @Test
    public void requestSave_criticalSavesImmediately() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        GameActivity activity = controller.setup().get();
        activity.setSaveExecutorForTest(Runnable::run);
        activity.setSaveDebounceMsForTest(1000);

        AtomicInteger saveCount = new AtomicInteger(0);
        SaveManager.setTestSaveListener(slotIndex -> saveCount.incrementAndGet());
        saveCount.set(0);

        invokeRequestSave(activity, "PAUSE");

        assertEquals(1, saveCount.get());

        SaveManager.setTestSaveListener(null);
        controller.pause();
    }

    @Test
    public void enqueueSave_persistsRunMetadataSnapshot() throws Exception {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Meta", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        SaveManager.RunMetadata metadata =
                new SaveManager.RunMetadata(-1, -1, false, 0, -1, -1, 1, null);
        saveManager.saveGame(0, profile, 1, 10, 5, grid, metadata);

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();
        activity.setSaveExecutorForTest(Runnable::run);

        setFieldValue(activity, "playerRow", 2);
        setFieldValue(activity, "playerCol", 3);
        setFieldValue(activity, "knightShieldActive", true);
        setFieldValue(activity, "knightShieldStrength", 7);
        setFieldValue(activity, "knightShieldRow", 1);
        setFieldValue(activity, "knightShieldCol", 4);
        setFieldValue(activity, "nextAbilityAvailableFloor", 6);
        setFieldValue(activity, "currentTerrain", TerrainType.ARCANE_NEXUS);
        setFieldValue(activity, "currentFloor", 8);
        setFieldValue(activity, "currentGold", 123);
        setFieldValue(activity, "currentPlatinum", 44);

        invokeRequestSave(activity, "PAUSE");

        SaveManager.GameState updated = saveManager.loadGame(0);
        assertNotNull(updated);
        assertNotNull(updated.metadata);
        assertEquals(2, updated.metadata.playerRow);
        assertEquals(3, updated.metadata.playerCol);
        assertTrue(updated.metadata.knightShieldActive);
        assertEquals(7, updated.metadata.knightShieldStrength);
        assertEquals(1, updated.metadata.knightShieldRow);
        assertEquals(4, updated.metadata.knightShieldCol);
        assertEquals(6, updated.metadata.nextAbilityAvailableFloor);
        assertEquals("ARCANE_NEXUS", updated.metadata.currentTerrain);
        assertEquals(8, updated.currentFloor);
        assertEquals(123, updated.currentGold);
        assertEquals(44, updated.currentPlatinum);

        controller.pause();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void invokeRequestSave(GameActivity activity, String reasonName) {
        try {
            Class reasonClass = Class.forName("com.example.clickdungeon.GameActivity$SaveReason");
            Object reason = Enum.valueOf(reasonClass, reasonName);
            ReflectionHelpers.callInstanceMethod(activity, "requestSave",
                    ReflectionHelpers.ClassParameter.from(reasonClass, reason));
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }

    private Tile[][] buildGridWithKey(TileType keyType, String customName) {
        Tile[][] grid = new Tile[5][5];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = new Tile(TileType.EMPTY);
            }
        }
        grid[0][0] = new Tile(keyType, customName);
        return grid;
    }

    private Object getFieldValue(GameActivity activity, String name) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
    }

    private void setFieldValue(GameActivity activity, String name, Object value) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }
}
