package com.adaplu.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.model.Achievement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads, saves, and merges achievement data with localized defaults.
 */
public class AchievementManager {

    /** Shared preferences file where achievements are stored. */
    private static final String PREFS_NAME = "player_prefs";
    /** Storage key for the achievement JSON blob. */
    private static final String KEY = "achievements";
    /** Schema version for persisted achievements. */
    private static final int ACHIEVEMENT_SCHEMA_VERSION = 1;

    private static final Gson GSON = new Gson();

    private AchievementManager() {
    }

    /**
     * Loads achievements from storage and merges in any new defaults.
     */
    public static List<Achievement> loadAchievements(Context context) {
        PersistedBlobStore.LoadResult result =
                PersistedBlobStore.load(context, PREFS_NAME, KEY, ACHIEVEMENT_SCHEMA_VERSION);
        String json = result.status == PersistedBlobStore.LoadResult.Status.OK ? result.json : null;
        Type type = new TypeToken<List<Achievement>>() {}.getType();
        List<Achievement> achievements = json != null ? GSON.fromJson(json, type) : new ArrayList<>();

        if (mergeWithDefaults(context, achievements)) {
            saveAchievements(context, achievements);
        }

        return achievements;
    }

    /**
     * Persists the achievement list to storage.
     */
    public static void saveAchievements(Context context, List<Achievement> achievements) {
        String json = GSON.toJson(achievements);
        boolean ok = PersistedBlobStore.save(context, PREFS_NAME, KEY, ACHIEVEMENT_SCHEMA_VERSION, json);
        if (!ok) {
            android.util.Log.w("AchievementManager", "Failed to persist achievements");
        }
    }

    /**
     * Marks an achievement as unlocked when the title matches.
     */
    public static void unlock(Context context, String achievementTitle) {
        List<Achievement> achievements = loadAchievements(context);
        boolean changed = false;
        for (Achievement a : achievements) {
            if (a.getTitle().equals(achievementTitle) && !a.isUnlocked()) {
                a.setUnlocked(true);
                changed = true;
                break;
            }
        }
        if (changed) {
            saveAchievements(context, achievements);
        }
    }

    /**
     * Returns true if the achievement with the given title is unlocked.
     */
    public static boolean isUnlocked(Context context, String achievementTitle) {
        List<Achievement> achievements = loadAchievements(context);
        for (Achievement a : achievements) {
            if (a.getTitle().equals(achievementTitle)) {
                return a.isUnlocked();
            }
        }
        return false;
    }

    /**
     * Ensures new default achievements are added and descriptions stay current.
     */
    private static boolean mergeWithDefaults(Context context, List<Achievement> achievements) {
        List<Achievement> defaults = getDefaultAchievements(context);
        boolean changed = false;

        for (Achievement defaultAchievement : defaults) {
            int existingIndex = findAchievementIndex(achievements, defaultAchievement.getTitle());
            if (existingIndex >= 0) {
                Achievement existing = achievements.get(existingIndex);
                if (!existing.getDescription().equals(defaultAchievement.getDescription())) {
                    achievements.set(existingIndex,
                            new Achievement(existing.getTitle(), defaultAchievement.getDescription(), existing.isUnlocked()));
                    changed = true;
                }
            } else {
                achievements.add(defaultAchievement);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Finds the index of an achievement by title, or -1 if missing.
     */
    private static int findAchievementIndex(List<Achievement> achievements, String title) {
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Builds the localized default achievement list.
     */
    private static List<Achievement> getDefaultAchievements(Context context) {
        List<Achievement> defaults = new ArrayList<>();
        defaults.add(new Achievement(
                context.getString(R.string.achievement_first_blood_title),
                context.getString(R.string.achievement_first_blood_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_dungeon_explorer_title),
                context.getString(R.string.achievement_dungeon_explorer_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_gold_hoarder_title),
                context.getString(R.string.achievement_gold_hoarder_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_boss_slayer_title),
                context.getString(R.string.achievement_boss_slayer_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_low_hp_survivor_title),
                context.getString(R.string.achievement_low_hp_survivor_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_trap_dodger_title),
                context.getString(R.string.achievement_trap_dodger_description),
                false));
        defaults.add(new Achievement(
                context.getString(R.string.achievement_victory_title),
                context.getString(R.string.achievement_victory_description),
                false));
        return defaults;
    }
}
