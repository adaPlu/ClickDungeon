package com.example.clickdungeon.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.annotation.NonNull;

/**
 * Dispatches lightweight audio and haptic feedback that respects the player preferences stored by
 * {@link SettingsManager}. All callers should route sound and vibration through this helper so the
 * settings screen truly controls the feel of the game.
 */
public final class FeedbackManager {

    private FeedbackManager() {
        // No instances.
    }

    /** Tracks the last vibration duration, mainly for test assertions. */
    private static volatile long lastVibrationDuration;

    /** Resets the last vibration duration tracker. */
    public static void resetVibrationTracker() {
        lastVibrationDuration = 0;
    }

    /** Returns the most recent vibration duration. */
    public static long getLastVibrationDuration() {
        return lastVibrationDuration;
    }

    /**
     * Plays a one-shot sound effect if audio is enabled in settings.
     */
    public static void playSound(@NonNull Context context, @NonNull SoundEffect effect) {
        if (!SettingsManager.isAudioEnabled(context)) {
            SoundManager.syncMuteFromSettings(context);
            return;
        }
        SoundManager.syncMuteFromSettings(context);
        SoundManager.playEffect(effect.getSoundKey());
    }

    /**
     * Triggers a simple vibration pattern if the player enables haptics.
     */
    @SuppressWarnings("deprecation")
    public static void vibrate(@NonNull Context context, @NonNull VibrationPattern pattern) {
        if (!SettingsManager.isVibrationEnabled(context)) {
            return;
        }
        Vibrator vibrator = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager =
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (manager != null) {
                vibrator = manager.getDefaultVibrator();
            }
        } else {
            //noinspection deprecation
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator == null) {
            return;
        }

        long duration = pattern.getDurationMs();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //noinspection deprecation
            vibrator.vibrate(duration);
        }
        lastVibrationDuration = duration;
    }

    /** Supported sound effect cues routed through the SoundManager. */
    public enum SoundEffect {
        TREASURE(SoundManager.KEY_EFFECT_TREASURE),
        POSITIVE(SoundManager.KEY_EFFECT_POSITIVE),
        TRAP(SoundManager.KEY_EFFECT_TRAP),
        COMBAT_VICTORY(SoundManager.KEY_EFFECT_VICTORY),
        COMBAT_DEFEAT(SoundManager.KEY_EFFECT_DEFEAT);

        private final String soundKey;

        SoundEffect(String soundKey) {
            this.soundKey = soundKey;
        }

        /** Returns the SoundManager key for the effect. */
        public String getSoundKey() {
            return soundKey;
        }
    }

    /** Simple one-shot vibration lengths used across the UI. */
    public enum VibrationPattern {
        LIGHT(25),
        MEDIUM(60),
        HEAVY(120);

        private final long durationMs;

        VibrationPattern(long durationMs) {
            this.durationMs = durationMs;
        }

        /** Returns the vibration duration in milliseconds. */
        long getDurationMs() {
            return durationMs;
        }
    }
}
