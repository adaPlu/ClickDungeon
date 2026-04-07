package com.adaplu.clickdungeon.ui;

import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.GridLayout;

import com.adaplu.clickdungeon.GameActivity;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class GameActivityAnimationLoopTest {

    @Test
    public void animateGridFrame_updatesOnlyActiveAnimatedTiles() {
        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_profile", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Test", PlayerClass.KNIGHT)))
                .apply();
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        GridLayout gridLayout = activity.findViewById(com.adaplu.clickdungeon.R.id.gridDungeon);
        assertTrue(gridLayout.getChildCount() > 0);

        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        Tile enemyTile = new Tile(TileType.ENEMY, new com.adaplu.clickdungeon.model.Monster("Slime", 3, 1, 0, "S"));
        enemyTile.reveal();
        grid[0][0] = enemyTile;
        ReflectionHelpers.setField(activity, "dungeonGrid", grid);

        View tileView = gridLayout.getChildAt(0);
        ReflectionHelpers.callInstanceMethod(activity, "bindTileView",
                ReflectionHelpers.ClassParameter.from(View.class, tileView),
                ReflectionHelpers.ClassParameter.from(Tile.class, grid[0][0]),
                ReflectionHelpers.ClassParameter.from(int.class, 0),
                ReflectionHelpers.ClassParameter.from(int.class, 0));

        Map<String, ?> active = getMapField(activity, "activeAnimatedTiles");
        assertTrue("Active animated tiles should include revealed enemies", active.containsKey("0_0"));
        ReflectionHelpers.callInstanceMethod(activity, "animateGridFrame");
    }

    private Map<String, ?> getMapField(GameActivity activity, String fieldName) {
        Object value = ReflectionHelpers.getField(activity, fieldName);
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) value;
            return map;
        }
        throw new AssertionError("Expected map for " + fieldName);
    }
}
