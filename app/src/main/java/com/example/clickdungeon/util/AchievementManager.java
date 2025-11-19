package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Achievement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    private static final String PREFS_NAME = "player_prefs";
    private static final String KEY = "achievements";

    private AchievementManager() {
    }

    public static List<Achievement> loadAchievements(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY, null);
        Type type = new TypeToken<List<Achievement>>() {}.getType();
        List<Achievement> achievements = json != null ? new Gson().fromJson(json, type) : new ArrayList<>();

        if (mergeWithDefaults(context, achievements)) {
            saveAchievements(context, achievements);
        }

        return achievements;
    }

    public static void saveAchievements(Context context, List<Achievement> achievements) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(achievements);
        prefs.edit().putString(KEY, json).apply();
    }

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

    public static boolean isUnlocked(Context context, String achievementTitle) {
        List<Achievement> achievements = loadAchievements(context);
        for (Achievement a : achievements) {
            if (a.getTitle().equals(achievementTitle)) {
                return a.isUnlocked();
            }
        }
        return false;
    }

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

    private static int findAchievementIndex(List<Achievement> achievements, String title) {
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

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
