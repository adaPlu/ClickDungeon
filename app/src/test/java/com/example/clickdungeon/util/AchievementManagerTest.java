package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Achievement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AchievementManagerTest {

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
    public void loadAchievementsSeedsDefaultCatalog() {
        List<Achievement> achievements = AchievementManager.loadAchievements(context);
        assertEquals(7, achievements.size());

        Achievement first = achievements.get(0);
        assertEquals(context.getString(R.string.achievement_first_blood_title), first.getTitle());
        assertFalse(first.isUnlocked());
    }

    @Test
    public void unlockPersistsStateChanges() {
        String title = context.getString(R.string.achievement_first_blood_title);
        AchievementManager.unlock(context, title);
        assertTrue(AchievementManager.isUnlocked(context, title));
    }

    @Test
    public void loadAchievementsMergesExistingEntries() {
        String title = context.getString(R.string.achievement_first_blood_title);
        List<Achievement> staleList = new ArrayList<>();
        staleList.add(new Achievement(title, "Outdated description", false));
        AchievementManager.saveAchievements(context, staleList);

        List<Achievement> merged = AchievementManager.loadAchievements(context);
        assertEquals(7, merged.size());

        int index = findAchievementIndex(merged, title);
        assertTrue("Expected merged achievements to contain " + title, index >= 0);
        Achievement refreshed = merged.get(index);
        assertEquals(context.getString(R.string.achievement_first_blood_description), refreshed.getDescription());
    }

    private static int findAchievementIndex(List<Achievement> achievements, String title) {
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }
}
