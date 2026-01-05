package com.example.clickdungeon;

import static org.junit.Assert.assertNotNull;

import android.content.Context;

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
}
