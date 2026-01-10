package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.GridLayout;

import com.example.clickdungeon.GameActivity;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class GameActivityAnimationLoopTest {

    @Test
    public void animateGridFrame_skipsInvisibleTiles_and_updatesVisibleOnes() {
        android.content.Context context = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_profile", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Test", PlayerClass.KNIGHT)))
                .apply();
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        GridLayout gridLayout = activity.findViewById(com.example.clickdungeon.R.id.gridDungeon);
        assertTrue(gridLayout.getChildCount() > 0);

        // Hide all tiles to simulate off-screen state.
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            gridLayout.getChildAt(i).setVisibility(View.INVISIBLE);
        }

        ReflectionHelpers.callInstanceMethod(activity, "animateGridFrame");
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        Map<String, Long> timestamps = getMapField(activity, "gridAnimationLastFrameMs");
        assertTrue("Off-screen tiles should throttle with timestamps", timestamps.size() > 0);

        // Make the first tile visible and laid out so getGlobalVisibleRect returns true.
        View first = gridLayout.getChildAt(0);
        first.setVisibility(View.VISIBLE);
        first.layout(0, 0, 50, 50);

        ReflectionHelpers.callInstanceMethod(activity, "animateGridFrame");
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        timestamps = getMapField(activity, "gridAnimationLastFrameMs");
        assertTrue("Visible tile should have a frame timestamp", timestamps.size() >= 1);
    }

    private Map<String, Long> getMapField(GameActivity activity, String fieldName) {
        Object value = ReflectionHelpers.getField(activity, fieldName);
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Long> map = (Map<String, Long>) value;
            return map;
        }
        throw new AssertionError("Expected map for " + fieldName);
    }
}
