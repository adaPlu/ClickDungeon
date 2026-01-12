package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.ui.CombatDialogFragment;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.Shadows;
import android.os.Looper;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class GameActivityInteractionTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences profilePrefs = context.getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        profilePrefs.edit().putString("profile",
                new Gson().toJson(new CharacterProfile("Test", PlayerClass.WIZARD)))
                .apply();
        context.getSharedPreferences("SaveSlot0", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void handleTileClick_revealsTileAndMovesPlayer() {
        GameActivity activity = buildActivityWithEmptyGrid();
        Tile[][] grid = ReflectionHelpers.getField(activity, "dungeonGrid");

        int row = 0;
        int col = 0;
        View tileView = getTileView(activity, row, col);

        ReflectionHelpers.callInstanceMethod(activity, "handleTileClick",
                ReflectionHelpers.ClassParameter.from(int.class, row),
                ReflectionHelpers.ClassParameter.from(int.class, col),
                ReflectionHelpers.ClassParameter.from(View.class, tileView));

        assertTrue(grid[row][col].isRevealed());
        int playerRow = ReflectionHelpers.getField(activity, "playerRow");
        int playerCol = ReflectionHelpers.getField(activity, "playerCol");
        assertEquals(row, playerRow);
        assertEquals(col, playerCol);
        assertEquals(1, (int) ReflectionHelpers.getField(activity, "revealedSafeTiles"));
    }

    @Test
    public void handleTileClick_enemyStartsCombatDialog() {
        GameActivity activity = buildActivityWithEmptyGrid();
        Tile[][] grid = ReflectionHelpers.getField(activity, "dungeonGrid");
        grid[1][1] = new Tile(TileType.ENEMY, new Monster("Slime", 4, 1, 0, "S"));

        ReflectionHelpers.callInstanceMethod(activity, "renderGrid");
        View tileView = getTileView(activity, 1, 1);

        ReflectionHelpers.callInstanceMethod(activity, "handleTileClick",
                ReflectionHelpers.ClassParameter.from(int.class, 1),
                ReflectionHelpers.ClassParameter.from(int.class, 1),
                ReflectionHelpers.ClassParameter.from(View.class, tileView));
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        CombatDialogFragment fragment = (CombatDialogFragment) activity.getSupportFragmentManager()
                .findFragmentByTag("CombatDialog");
        assertNotNull(fragment);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void classAbilityButtonCancelsTargetingMode() {
        GameActivity activity = buildActivityWithEmptyGrid();
        Class<?> modeClass = getAbilityTargetModeClass();
        Object fireball = Enum.valueOf((Class<Enum>) modeClass, "WIZARD_FIREBALL");
        ReflectionHelpers.setField(activity, "pendingAbilityTargetMode", fireball);

        Button abilityButton = activity.findViewById(R.id.btnClassAbility);
        abilityButton.performClick();

        Object mode = ReflectionHelpers.getField(activity, "pendingAbilityTargetMode");
        Object none = Enum.valueOf((Class<Enum>) modeClass, "NONE");
        assertEquals(none, mode);
    }

    private GameActivity buildActivityWithEmptyGrid() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        GameActivity activity = controller.setup().get();

        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }

        ReflectionHelpers.setField(activity, "dungeonGrid", grid);
        ReflectionHelpers.setField(activity, "currentFloor", 1);
        ReflectionHelpers.setField(activity, "safeTilesToReveal", 25);
        ReflectionHelpers.setField(activity, "revealedSafeTiles", 0);
        ReflectionHelpers.callInstanceMethod(activity, "renderGrid");
        return activity;
    }

    private View getTileView(GameActivity activity, int row, int col) {
        int index = (row * 5) + col;
        GridLayout gridLayout = activity.findViewById(R.id.gridDungeon);
        return gridLayout.getChildAt(index);
    }

    private Class<?> getAbilityTargetModeClass() {
        try {
            return Class.forName("com.example.clickdungeon.GameActivity$AbilityTargetMode");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
