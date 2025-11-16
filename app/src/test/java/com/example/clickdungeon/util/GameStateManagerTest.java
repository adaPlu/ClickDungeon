package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class GameStateManagerTest {

    private static final String PREFS_NAME = "player_prefs";
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void saveAndLoadGrid_roundTripsSerializedTiles() {
        Tile[][] grid = new Tile[][] {
                { new Tile(TileType.EMPTY), new Tile(TileType.MONSTER) },
                { new Tile(TileType.TREASURE), new Tile(TileType.TRAP) }
        };

        GameStateManager.saveGrid(context, grid, 75);
        Tile[][] restored = GameStateManager.loadGrid(context);

        assertNotNull(restored);
        assertEquals(2, restored.length);
        assertEquals(TileType.EMPTY, restored[0][0].getType());
        assertEquals(TileType.MONSTER, restored[0][1].getType());
        assertEquals(TileType.TREASURE, restored[1][0].getType());
        assertEquals(TileType.TRAP, restored[1][1].getType());
        assertEquals(75, GameStateManager.loadGold(context));
    }

    @Test
    public void saveFloor_persistsSelectedDepth() {
        GameStateManager.saveFloor(context, 3);

        assertEquals(3, GameStateManager.loadFloor(context));
    }

    @Test
    public void clearState_resetsSavedProgress() {
        Tile[][] grid = new Tile[][] {{ new Tile(TileType.EMPTY) }};
        GameStateManager.saveGrid(context, grid, 20);
        GameStateManager.saveFloor(context, 4);

        GameStateManager.clearState(context);

        assertNull(GameStateManager.loadGrid(context));
        assertEquals(0, GameStateManager.loadGold(context));
        assertEquals(1, GameStateManager.loadFloor(context));
    }
}
