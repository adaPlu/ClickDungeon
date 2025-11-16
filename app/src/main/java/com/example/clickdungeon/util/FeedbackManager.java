package com.example.clickdungeon.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;

/**
 * Dispatches lightweight audio and haptic feedback that respects the player preferences stored by
 * {@link SettingsManager}. All callers should route sound and vibration through this helper so the
 * settings screen truly controls the feel of the game.
 */
public final class FeedbackManager {

    private static final Object LOCK = new Object();
    private static ToneGenerator toneGenerator;

    private FeedbackManager() {
        // No instances.
    }

    public static void playSound(@NonNull Context context, @NonNull SoundEffect effect) {
        if (!SettingsManager.isAudioEnabled(context)) {
            return;
        }
        try {
            ToneGenerator generator = obtainToneGenerator();
            generator.startTone(effect.getToneType(), effect.getDurationMs());
        } catch (RuntimeException ignored) {
            // Some devices can throw if audio focus is unavailable. We silently ignore failures.
        }
    }

    public static void vibrate(@NonNull Context context, @NonNull VibrationPattern pattern) {
        if (!SettingsManager.isVibrationEnabled(context)) {
            return;
        }
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        long duration = pattern.getDurationMs();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }

    private static ToneGenerator obtainToneGenerator() {
        synchronized (LOCK) {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
            }
            return toneGenerator;
        }
    }

    public enum SoundEffect {
        TREASURE(ToneGenerator.TONE_PROP_ACK, 150),
        POSITIVE(ToneGenerator.TONE_PROP_BEEP2, 180),
        TRAP(ToneGenerator.TONE_SUP_ERROR, 220),
        COMBAT_VICTORY(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 280),
        COMBAT_DEFEAT(ToneGenerator.TONE_CDMA_ABBR_ALERT, 350);

        private final int toneType;
        private final int durationMs;

        SoundEffect(int toneType, int durationMs) {
            this.toneType = toneType;
            this.durationMs = durationMs;
        }

        int getToneType() {
            return toneType;
        }

        int getDurationMs() {
            return durationMs;
        }
    }

    public enum VibrationPattern {
        LIGHT(25),
        MEDIUM(60),
        HEAVY(120);

        private final long durationMs;

        VibrationPattern(long durationMs) {
            this.durationMs = durationMs;
        }

        long getDurationMs() {
            return durationMs;
        }
    }
}
