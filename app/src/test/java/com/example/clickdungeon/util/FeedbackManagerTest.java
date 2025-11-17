package com.example.clickdungeon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Vibrator;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowVibrator;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
@SuppressWarnings("deprecation")
public class FeedbackManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        resetToneGenerator();
        SettingsManager.setAudioEnabled(context, true);
        SettingsManager.setVibrationEnabled(context, true);
    }

    @Test
    public void playSound_audioDisabled_doesNotInitializeToneGenerator() {
        SettingsManager.setAudioEnabled(context, false);
        FeedbackManager.playSound(context, FeedbackManager.SoundEffect.POSITIVE);
        assertNull(getToneGenerator());
    }

    @Test
    public void playSound_audioEnabled_initializesToneGenerator() {
        FeedbackManager.playSound(context, FeedbackManager.SoundEffect.TREASURE);
        assertNotNull(getToneGenerator());
    }

    @Test
    public void vibrate_vibrationDisabled_suppressesFeedback() {
        SettingsManager.setVibrationEnabled(context, false);
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        ShadowVibrator shadowVibrator = org.robolectric.Shadows.shadowOf(vibrator);
        clearShadowVibration(shadowVibrator);

        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.MEDIUM);

        assertFalse(hasRecordedVibration(shadowVibrator));
    }

    @Test
    public void vibrate_vibrationEnabled_recordsLastVibration() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        ShadowVibrator shadowVibrator = org.robolectric.Shadows.shadowOf(vibrator);
        clearShadowVibration(shadowVibrator);

        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.HEAVY);

        assertTrue(hasRecordedVibration(shadowVibrator));
    }

    private void resetToneGenerator() {
        ReflectionHelpers.setStaticField(FeedbackManager.class, "toneGenerator", null);
    }

    private Object getToneGenerator() {
        return ReflectionHelpers.getStaticField(FeedbackManager.class, "toneGenerator");
    }

    private void clearShadowVibration(ShadowVibrator shadowVibrator) {
        try {
            ShadowVibrator.class.getMethod("clear").invoke(shadowVibrator);
        } catch (Exception ignored) {
            try {
                Object records = ShadowVibrator.class
                        .getMethod("getVibrationEffects")
                        .invoke(shadowVibrator);
                if (records instanceof java.util.Collection) {
                    ((java.util.Collection<?>) records).clear();
                }
            } catch (Exception ignoredAgain) {
                // no-op
            }
        }
    }

    private boolean hasRecordedVibration(ShadowVibrator shadowVibrator) {
        try {
            Object record = ShadowVibrator.class
                    .getMethod("getLastVibration")
                    .invoke(shadowVibrator);
            return record != null;
        } catch (Exception ignored) {
            try {
                Object records = ShadowVibrator.class
                        .getMethod("getVibrationEffects")
                        .invoke(shadowVibrator);
                if (records instanceof java.util.Collection) {
                    return !((java.util.Collection<?>) records).isEmpty();
                }
            } catch (Exception ignoredAgain) {
                // no-op
            }
        }
        return false;
    }
}
