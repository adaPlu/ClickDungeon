package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.Tile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class GameStateManager {

    private static final String PREFS_NAME = "player_prefs";
    private static final String GRID_KEY = "grid";
    private static final String GOLD_KEY = "gold";
    private static final String FLOOR_KEY = "floor";

    public static void saveGrid(Context context, Tile[][] grid, int gold) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        prefs.edit()
                .putString(GRID_KEY, gson.toJson(grid))
                .putInt(GOLD_KEY, gold)
                .apply();
    }

    public static Tile[][] loadGrid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(GRID_KEY, null);
        if (json == null) return null;

        Gson gson = new Gson();
        Type type = new TypeToken<Tile[][]>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static void clearState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(GRID_KEY)
                .remove(GOLD_KEY)
                .remove(FLOOR_KEY)
                .apply();
    }

    public static void saveFloor(Context context, int floor) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(FLOOR_KEY, floor).apply();
    }

    public static int loadFloor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(FLOOR_KEY, 1);
    }

    public static int loadGold(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(GOLD_KEY, 0);
    }
}
