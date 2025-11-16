package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.SaveManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class GameActivitySaveIntegrationTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("SaveSlot0", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void loadAndPersist_withSaveSlot_roundTripsThroughSaveManager() throws Exception {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Nova", PlayerClass.WIZARD);
        Tile[][] grid = new Tile[][]{
                {new Tile(TileType.EMPTY), new Tile(TileType.MONSTER)},
                {new Tile(TileType.TREASURE), new Tile(TileType.TRAP)}
        };
        saveManager.saveGame(0, profile, 4, 77, grid);

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();

        int currentFloor = (int) getFieldValue(activity, "currentFloor");
        int currentGold = (int) getFieldValue(activity, "currentGold");
        Tile[][] activeGrid = (Tile[][]) getFieldValue(activity, "dungeonGrid");

        assertEquals(4, currentFloor);
        assertEquals(77, currentGold);
        assertNotNull(activeGrid);
        assertEquals(TileType.MONSTER, activeGrid[0][1].getType());

        setFieldValue(activity, "currentGold", 125);

        controller.pause();

        SaveManager.GameState updated = saveManager.loadGame(0);
        assertNotNull(updated);
        assertEquals(125, updated.currentGold);
        assertEquals(4, updated.currentFloor);
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
