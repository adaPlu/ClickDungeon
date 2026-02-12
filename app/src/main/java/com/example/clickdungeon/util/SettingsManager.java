package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Centralizes persisted user preferences for audio, vibration, and difficulty options.
 */
public final class SettingsManager {

    /** Shared preferences file storing player settings. */
    private static final String PREFS_NAME = "game_settings";
    /** Key for audio enablement. */
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";
    /** Key for vibration enablement. */
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    /** Key for difficulty selection. */
    private static final String KEY_DIFFICULTY = "difficulty";
    /** Key for color-blind mode preference. */
    private static final String KEY_COLOR_BLIND = "color_blind_mode";
    /** Key for tutorial hint preference. */
    private static final String KEY_TUTORIAL_HINTS = "tutorial_hints";
    /** Key for audio diagnostics logging. */
    private static final String KEY_AUDIO_DIAGNOSTICS = "audio_diagnostics";

    /** Default difficulty when no preference exists. */
    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    private SettingsManager() {
        // No instances.
    }

    /** Returns the SharedPreferences handle for settings. */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns true if audio is enabled. */
    public static boolean isAudioEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_AUDIO_ENABLED, true);
    }

    /** Persists the audio enabled flag. */
    public static void setAudioEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply();
    }

    /** Returns true if vibration is enabled. */
    public static boolean isVibrationEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    /** Persists the vibration enabled flag. */
    public static void setVibrationEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    /** Returns true if color-blind mode is enabled. */
    public static boolean isColorBlindModeEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_COLOR_BLIND, false);
    }

    /** Persists the color-blind mode flag. */
    public static void setColorBlindModeEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_COLOR_BLIND, enabled).apply();
    }

    /** Returns true if tutorial hints are enabled. */
    public static boolean areTutorialHintsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_TUTORIAL_HINTS, true);
    }

    /** Persists the tutorial hints flag. */
    public static void setTutorialHintsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_TUTORIAL_HINTS, enabled).apply();
    }

    /** Returns true if audio diagnostics are enabled. */
    public static boolean isAudioDiagnosticsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_AUDIO_DIAGNOSTICS, false);
    }

    /** Persists the audio diagnostics flag. */
    public static void setAudioDiagnosticsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_DIAGNOSTICS, enabled).apply();
    }

    /** Returns the raw difficulty value string. */
    public static String getDifficulty(Context context) {
        return getDifficultyMode(context).getValue();
    }

    /** Returns the difficulty enum based on the stored value. */
    public static Difficulty getDifficultyMode(Context context) {
        String storedValue = getPrefs(context).getString(KEY_DIFFICULTY, DEFAULT_DIFFICULTY.getValue());
        return Difficulty.fromValue(storedValue);
    }

    /** Updates difficulty preference by string. */
    public static void setDifficulty(Context context, String difficultyValue) {
        Difficulty difficulty = Difficulty.fromValue(difficultyValue);
        setDifficulty(context, difficulty);
    }

    /** Updates difficulty preference by enum. */
    public static void setDifficulty(Context context, Difficulty difficulty) {
        getPrefs(context).edit().putString(KEY_DIFFICULTY, difficulty.getValue()).apply();
    }

    /**
     * Difficulty presets with scaling multipliers for key systems.
     */
    public enum Difficulty {
        CASUAL("CASUAL", 0.8f, 0.85f, 0.65f, 0.85f, 0.8f),
        NORMAL("NORMAL", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f),
        HARDCORE("HARDCORE", 1.35f, 1.25f, 1.35f, 1.15f, 1.2f);

        private final String value;
        private final float monsterHealthMultiplier;
        private final float monsterOffenseMultiplier;
        private final float trapDamageMultiplier;
        private final float xpRewardMultiplier;
        private final float goldRewardMultiplier;

        Difficulty(String value,
                   float monsterHealthMultiplier,
                   float monsterOffenseMultiplier,
                   float trapDamageMultiplier,
                   float xpRewardMultiplier,
                   float goldRewardMultiplier) {
            this.value = value;
            this.monsterHealthMultiplier = monsterHealthMultiplier;
            this.monsterOffenseMultiplier = monsterOffenseMultiplier;
            this.trapDamageMultiplier = trapDamageMultiplier;
            this.xpRewardMultiplier = xpRewardMultiplier;
            this.goldRewardMultiplier = goldRewardMultiplier;
        }

        /** Returns the persistent value string. */
        public String getValue() {
            return value;
        }

        /** Scales monster HP by the difficulty multiplier. */
        public int scaleMonsterHealth(int base) {
            return Math.max(1, Math.round(base * monsterHealthMultiplier));
        }

        /** Scales monster attack by the difficulty multiplier. */
        public int scaleMonsterAttack(int base) {
            return Math.max(1, Math.round(base * monsterOffenseMultiplier));
        }

        /** Scales monster defense by the difficulty multiplier. */
        public int scaleMonsterDefense(int base) {
            return Math.max(0, Math.round(base * (monsterOffenseMultiplier * 0.9f)));
        }

        /** Scales trap damage by the difficulty multiplier. */
        public int scaleTrapDamage(int baseDamage) {
            if (baseDamage <= 0) {
                return 0;
            }
            return Math.max(1, Math.round(baseDamage * trapDamageMultiplier));
        }

        /** Scales XP rewards by the difficulty multiplier. */
        public int scaleXpReward(int base) {
            return Math.max(1, Math.round(base * xpRewardMultiplier));
        }

        /** Scales gold rewards by the difficulty multiplier. */
        public int scaleGoldReward(int base) {
            return Math.max(1, Math.round(base * goldRewardMultiplier));
        }

        /** Resolves a difficulty enum from a stored string value. */
        public static Difficulty fromValue(String value) {
            if (value != null) {
                for (Difficulty difficulty : values()) {
                    if (difficulty.value.equalsIgnoreCase(value)) {
                        return difficulty;
                    }
                }
            }
            return DEFAULT_DIFFICULTY;
        }
    }
}
