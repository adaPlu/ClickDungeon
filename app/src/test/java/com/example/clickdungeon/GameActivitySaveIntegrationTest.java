package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import com.example.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class GameActivitySaveIntegrationTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "SaveSlot0").edit().clear().commit();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
    }

    @Test
    public void loadAndPersist_withSaveSlot_roundTripsThroughSaveManager() throws Exception {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Nova", PlayerClass.WIZARD);
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

        int currentFloor = (int) getFieldValue(activity, "currentFloor");
        int currentGold = (int) getFieldValue(activity, "currentGold");
        int currentPlatinum = (int) getFieldValue(activity, "currentPlatinum");
        Tile[][] activeGrid = (Tile[][]) getFieldValue(activity, "dungeonGrid");

        assertEquals(4, currentFloor);
        assertEquals(77, currentGold);
        assertEquals(15, currentPlatinum);
        assertNotNull(activeGrid);
        assertEquals(TileType.ENEMY, activeGrid[0][1].getType());

        setFieldValue(activity, "currentGold", 125);
        setFieldValue(activity, "currentPlatinum", 35);

        controller.pause();

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
