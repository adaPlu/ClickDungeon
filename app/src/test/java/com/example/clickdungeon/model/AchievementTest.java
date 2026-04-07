package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AchievementTest {

    @Test
    public void constructorAndGettersReturnAssignedValues() {
        Achievement achievement = new Achievement("Dungeon Delver", "Reach floor 10", false);
        assertEquals("Dungeon Delver", achievement.getTitle());
        assertEquals("Reach floor 10", achievement.getDescription());
        assertFalse(achievement.isUnlocked());
    }

    @Test
    public void setUnlockedUpdatesState() {
        Achievement achievement = new Achievement("Untouchable", "Win without damage", false);
        achievement.setUnlocked(true);
        assertTrue(achievement.isUnlocked());
        achievement.setUnlocked(false);
        assertFalse(achievement.isUnlocked());
    }
}
