package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.Tile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class GameStateManager {

    private static final String PREFS_NAME = "player_prefs";

    private static String key(String base, int slot) {
        return base + "_slot_" + slot;
    }

    public static void saveGrid(Context context, Tile[][] grid, int gold, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        prefs.edit()
                .putString(key("grid", slot), gson.toJson(grid))
                .putInt(key("gold", slot), gold)
                .apply();
    }

    public static Tile[][] loadGrid(Context context, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(key("grid", slot), null);
        if (json == null) return null;

        Gson gson = new Gson();
        Type type = new TypeToken<Tile[][]>() {}.getType();
        return gson.fromJson(json, type);
    }

    public static void clearSlot(Context context, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(key("grid", slot))
                .remove(key("gold", slot))
                .remove(key("floor", slot))
                .apply();
    }

    public static void saveFloor(Context context, int floor, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(key("floor", slot), floor).apply();
    }

    public static int loadFloor(Context context, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key("floor", slot), 1);
    }

    public static int loadGold(Context context, int slot) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(key("gold", slot), 0);
    }
}
