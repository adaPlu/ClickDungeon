package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.widget.GridLayout;

import com.example.clickdungeon.GameActivity;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class GameActivityGridAnimationThrottleTest {

    @Test
    public void detachClearsAnimationEntry() {
        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_profile", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Test", PlayerClass.KNIGHT)))
                .apply();
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        GridLayout gridLayout = activity.findViewById(com.example.clickdungeon.R.id.gridDungeon);
        View tileView = gridLayout.getChildAt(0);

        // Mark the tile as an enemy to ensure an animator is created
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        grid[0][0] = new Tile(TileType.ENEMY, new com.example.clickdungeon.model.Monster("Slime", 3, 1, 0, "S"));
        ReflectionHelpers.setField(activity, "dungeonGrid", grid);
        ReflectionHelpers.callInstanceMethod(activity, "bindTileView",
                ReflectionHelpers.ClassParameter.from(View.class, tileView),
                ReflectionHelpers.ClassParameter.from(Tile.class, grid[0][0]),
                ReflectionHelpers.ClassParameter.from(int.class, 0),
                ReflectionHelpers.ClassParameter.from(int.class, 0));

        // Detach and ensure caches are cleared
        gridLayout.removeView(tileView);
        Map<String, ?> anims = getMapField(activity, "gridMonsterAnimations");
        Map<String, ?> timestamps = getMapField(activity, "gridAnimationLastFrameMs");
        assertEquals(false, anims.containsKey("0_0"));
        assertEquals(false, timestamps.containsKey("0_0"));
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
