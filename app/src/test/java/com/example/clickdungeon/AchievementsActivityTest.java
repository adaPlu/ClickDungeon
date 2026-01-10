package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AchievementsActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void recyclerHasAdapter() {
        AchievementsActivity activity = Robolectric.buildActivity(AchievementsActivity.class).setup().get();

        RecyclerView recyclerView = activity.findViewById(R.id.recyclerAchievements);
        assertNotNull(recyclerView.getAdapter());
    }

    @Test
    public void achievementsListShowsStatusText() {
        AchievementsActivity activity = Robolectric.buildActivity(AchievementsActivity.class).setup().get();
        RecyclerView recyclerView = activity.findViewById(R.id.recyclerAchievements);

        recyclerView.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY));
        recyclerView.layout(0, 0, 1080, 1920);

        View firstRow = recyclerView.getChildAt(0);
        assertNotNull(firstRow);
        TextView status = firstRow.findViewById(R.id.textAchievementStatus);
        assertEquals("Locked", status.getText().toString());
    }
}
