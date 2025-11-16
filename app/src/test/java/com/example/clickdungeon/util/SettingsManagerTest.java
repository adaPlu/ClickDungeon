package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SettingsManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void defaultsAreEnabledWithNormalDifficulty() {
        assertTrue(SettingsManager.isAudioEnabled(context));
        assertTrue(SettingsManager.isVibrationEnabled(context));
        assertFalse(SettingsManager.isColorBlindModeEnabled(context));
        assertTrue(SettingsManager.areTutorialHintsEnabled(context));
        assertEquals(SettingsManager.Difficulty.NORMAL, SettingsManager.getDifficultyMode(context));
    }

    @Test
    public void togglesPersistAudioAndVibration() {
        SettingsManager.setAudioEnabled(context, false);
        SettingsManager.setVibrationEnabled(context, false);
        SettingsManager.setColorBlindModeEnabled(context, true);
        SettingsManager.setTutorialHintsEnabled(context, false);

        assertFalse(SettingsManager.isAudioEnabled(context));
        assertFalse(SettingsManager.isVibrationEnabled(context));
        assertTrue(SettingsManager.isColorBlindModeEnabled(context));
        assertFalse(SettingsManager.areTutorialHintsEnabled(context));

        SettingsManager.setAudioEnabled(context, true);
        SettingsManager.setVibrationEnabled(context, true);
        SettingsManager.setColorBlindModeEnabled(context, false);
        SettingsManager.setTutorialHintsEnabled(context, true);

        assertTrue(SettingsManager.isAudioEnabled(context));
        assertTrue(SettingsManager.isVibrationEnabled(context));
        assertFalse(SettingsManager.isColorBlindModeEnabled(context));
        assertTrue(SettingsManager.areTutorialHintsEnabled(context));
    }

    @Test
    public void difficultyScalingAppliesExpectedMultipliers() {
        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.CASUAL);
        SettingsManager.Difficulty difficulty = SettingsManager.getDifficultyMode(context);

        assertEquals(SettingsManager.Difficulty.CASUAL, difficulty);
        assertEquals(8, difficulty.scaleMonsterHealth(10));
        assertEquals(9, difficulty.scaleMonsterAttack(10));
        assertEquals(6, difficulty.scaleTrapDamage(8));
        assertEquals(7, difficulty.scaleXpReward(8));
        assertEquals(8, difficulty.scaleGoldReward(10));

        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.HARDCORE);
        difficulty = SettingsManager.getDifficultyMode(context);

        assertEquals(SettingsManager.Difficulty.HARDCORE, difficulty);
        assertEquals(14, difficulty.scaleMonsterHealth(10));
        assertEquals(14, difficulty.scaleMonsterAttack(11));
        assertEquals(12, difficulty.scaleTrapDamage(9));
        assertEquals(12, difficulty.scaleXpReward(10));
        assertEquals(12, difficulty.scaleGoldReward(10));
    }
}
