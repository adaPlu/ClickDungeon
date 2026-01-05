package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Vibrator;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowVibrator;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@SuppressWarnings("deprecation")
public class FeedbackManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SettingsManager.setAudioEnabled(context, true);
        SettingsManager.setVibrationEnabled(context, true);
        SoundManager.release();
        SoundManager.init(context);
        SoundManager.syncMuteFromSettings(context);
        SoundManager.setTestPlaybackListener(null);
    }

    @After
    public void tearDown() {
        SoundManager.setTestPlaybackListener(null);
        SoundManager.release();
    }

    @Test
    public void playSound_audioDisabled_doesNotTriggerEffect() {
        SettingsManager.setAudioEnabled(context, false);
        SoundManager.syncMuteFromSettings(context);

        FeedbackManager.playSound(context, FeedbackManager.SoundEffect.POSITIVE);

        assertNull(SoundManager.getLastPlayedKey());
    }

    @Test
    public void playSound_audioEnabled_routesThroughSoundManager() {
        FeedbackManager.playSound(context, FeedbackManager.SoundEffect.TREASURE);

        assertEquals(SoundManager.KEY_EFFECT_TREASURE, SoundManager.getLastPlayedKey());
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
        FeedbackManager.resetVibrationTracker();

        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.HEAVY);

        assertTrue(FeedbackManager.getLastVibrationDuration() > 0);
    }

    @Test
    public void resetVibrationTrackerClearsLastDuration() {
        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.LIGHT);
        assertTrue(FeedbackManager.getLastVibrationDuration() > 0);

        FeedbackManager.resetVibrationTracker();

        assertEquals(0, FeedbackManager.getLastVibrationDuration());
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
