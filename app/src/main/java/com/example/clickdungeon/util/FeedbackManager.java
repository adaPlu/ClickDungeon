package com.example.clickdungeon.util;

import android.content.Context;
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

    private FeedbackManager() {
        // No instances.
    }

    private static volatile long lastVibrationDuration;

    public static void resetVibrationTracker() {
        lastVibrationDuration = 0;
    }

    public static long getLastVibrationDuration() {
        return lastVibrationDuration;
    }

    public static void playSound(@NonNull Context context, @NonNull SoundEffect effect) {
        if (!SettingsManager.isAudioEnabled(context)) {
            SoundManager.syncMuteFromSettings(context);
            return;
        }
        SoundManager.syncMuteFromSettings(context);
        SoundManager.playEffect(effect.getSoundKey());
    }

    public static void vibrate(@NonNull Context context, @NonNull VibrationPattern pattern) {
        if (!SettingsManager.isVibrationEnabled(context)) {
            return;
        }
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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

        public String getSoundKey() {
            return soundKey;
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
