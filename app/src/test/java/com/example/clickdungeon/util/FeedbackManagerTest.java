package com.example.clickdungeon.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        shadowVibrator.clear();

        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.MEDIUM);

        assertNull(shadowVibrator.getLastVibration());
    }

    @Test
    public void vibrate_vibrationEnabled_recordsLastVibration() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        ShadowVibrator shadowVibrator = org.robolectric.Shadows.shadowOf(vibrator);
        shadowVibrator.clear();

        FeedbackManager.vibrate(context, FeedbackManager.VibrationPattern.HEAVY);

        assertNotNull(shadowVibrator.getLastVibration());
    }

    private void resetToneGenerator() {
        ReflectionHelpers.setStaticField(FeedbackManager.class, "toneGenerator", null);
    }

    private Object getToneGenerator() {
        return ReflectionHelpers.getStaticField(FeedbackManager.class, "toneGenerator");
    }
}
