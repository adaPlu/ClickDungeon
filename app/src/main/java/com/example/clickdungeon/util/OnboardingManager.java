package com.example.clickdungeon.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.clickdungeon.R;

/**
 * Handles first-run tutorials and contextual onboarding prompts.
 */
public final class OnboardingManager {

    private static final String PREFS_NAME = "onboarding_prompts";
    private static final String KEY_DUNGEON_TUTORIAL_VERSION = "dungeon_tutorial_version";
    private static final int CURRENT_TUTORIAL_VERSION = 1;

    private OnboardingManager() {
    }

    public static void showDungeonTutorialIfNeeded(@NonNull Activity activity,
                                                   @NonNull SettingsManager.Difficulty difficulty,
                                                   boolean colorBlindModeEnabled) {
        if (!SettingsManager.areTutorialHintsEnabled(activity)) {
            return;
        }
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int shownVersion = prefs.getInt(KEY_DUNGEON_TUTORIAL_VERSION, 0);
        if (shownVersion >= CURRENT_TUTORIAL_VERSION) {
            return;
        }

        String difficultyLabel = getDifficultyLabel(activity, difficulty);
        String colorBlindLine = activity.getString(colorBlindModeEnabled
                ? R.string.onboarding_colorblind_enabled
                : R.string.onboarding_colorblind_disabled);

        new AlertDialog.Builder(activity)
                .setTitle(R.string.onboarding_dungeon_title)
                .setMessage(activity.getString(R.string.onboarding_dungeon_message, difficultyLabel, colorBlindLine))
                .setCancelable(false)
                .setPositiveButton(R.string.onboarding_tutorial_continue, (dialog, which) ->
                        prefs.edit().putInt(KEY_DUNGEON_TUTORIAL_VERSION, CURRENT_TUTORIAL_VERSION).apply())
                .setNegativeButton(R.string.onboarding_tutorial_disable, (dialog, which) -> {
                    prefs.edit().putInt(KEY_DUNGEON_TUTORIAL_VERSION, CURRENT_TUTORIAL_VERSION).apply();
                    SettingsManager.setTutorialHintsEnabled(activity, false);
                })
                .show();
    }

    private static String getDifficultyLabel(@NonNull Activity activity,
                                             @NonNull SettingsManager.Difficulty difficulty) {
        switch (difficulty) {
            case CASUAL:
                return activity.getString(R.string.settings_difficulty_casual);
            case HARDCORE:
                return activity.getString(R.string.settings_difficulty_hardcore);
            case NORMAL:
            default:
                return activity.getString(R.string.settings_difficulty_normal);
        }
    }
}
