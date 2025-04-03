package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.Achievement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    private static final String PREFS_NAME = "player_prefs";
    private static final String KEY = "achievements";

    public static List<Achievement> loadAchievements(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY, null);
        Type type = new TypeToken<List<Achievement>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }

    public static void saveAchievements(Context context, List<Achievement> achievements) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(achievements);
        prefs.edit().putString(KEY, json).apply();
    }

    public static void unlock(Context context, String achievementTitle) {
        List<Achievement> achievements = loadAchievements(context);
        for (Achievement a : achievements) {
            if (a.getTitle().equals(achievementTitle) && !a.isUnlocked()) {
                a.setUnlocked(true);
            }
        }
        saveAchievements(context, achievements);
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
}
