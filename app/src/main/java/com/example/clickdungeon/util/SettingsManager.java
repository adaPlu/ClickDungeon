package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Centralizes persisted user preferences for audio, vibration, and difficulty options.
 */
public final class SettingsManager {

    private static final String PREFS_NAME = "game_settings";
    private static final String KEY_AUDIO_ENABLED = "audio_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_COLOR_BLIND = "color_blind_mode";
    private static final String KEY_TUTORIAL_HINTS = "tutorial_hints";

    private static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    private SettingsManager() {
        // No instances.
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAudioEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_AUDIO_ENABLED, true);
    }

    public static void setAudioEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply();
    }

    public static boolean isVibrationEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public static void setVibrationEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public static boolean isColorBlindModeEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_COLOR_BLIND, false);
    }

    public static void setColorBlindModeEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_COLOR_BLIND, enabled).apply();
    }

    public static boolean areTutorialHintsEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_TUTORIAL_HINTS, true);
    }

    public static void setTutorialHintsEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_TUTORIAL_HINTS, enabled).apply();
    }

    public static String getDifficulty(Context context) {
        return getDifficultyMode(context).getValue();
    }

    public static Difficulty getDifficultyMode(Context context) {
        String storedValue = getPrefs(context).getString(KEY_DIFFICULTY, DEFAULT_DIFFICULTY.getValue());
        return Difficulty.fromValue(storedValue);
    }

    public static void setDifficulty(Context context, String difficultyValue) {
        Difficulty difficulty = Difficulty.fromValue(difficultyValue);
        setDifficulty(context, difficulty);
    }

    public static void setDifficulty(Context context, Difficulty difficulty) {
        getPrefs(context).edit().putString(KEY_DIFFICULTY, difficulty.getValue()).apply();
    }

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

        public String getValue() {
            return value;
        }

        public int scaleMonsterHealth(int base) {
            return Math.max(1, Math.round(base * monsterHealthMultiplier));
        }

        public int scaleMonsterAttack(int base) {
            return Math.max(1, Math.round(base * monsterOffenseMultiplier));
        }

        public int scaleMonsterDefense(int base) {
            return Math.max(0, Math.round(base * (monsterOffenseMultiplier * 0.9f)));
        }

        public int scaleTrapDamage(int baseDamage) {
            if (baseDamage <= 0) {
                return 0;
            }
            return Math.max(1, Math.round(baseDamage * trapDamageMultiplier));
        }

        public int scaleXpReward(int base) {
            return Math.max(1, Math.round(base * xpRewardMultiplier));
        }

        public int scaleGoldReward(int base) {
            return Math.max(1, Math.round(base * goldRewardMultiplier));
        }

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
