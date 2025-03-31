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
    private static final String ACHIEVEMENTS_KEY = "achievements";

    public static void saveAchievements(Context context, List<Achievement> achievements) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(achievements);
        prefs.edit().putString(ACHIEVEMENTS_KEY, json).apply();
    }

    public static List<Achievement> loadAchievements(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(ACHIEVEMENTS_KEY, null);
        Type type = new TypeToken<List<Achievement>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }
}
