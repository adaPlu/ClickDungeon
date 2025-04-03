package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.Tile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class GameStateManager {

    private static final String PREFS_NAME = "game_state";
    private static final String GRID_KEY = "dungeon_grid";
    private static final String GOLD_KEY = "current_gold";

    public static void saveGrid(Context context, Tile[][] grid, int gold) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonGrid = gson.toJson(grid);
        prefs.edit().putString(GRID_KEY, jsonGrid).putInt(GOLD_KEY, gold).apply();
    }

    public static Tile[][] loadGrid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(GRID_KEY, null);
        if (json == null) return null;

        Type type = new TypeToken<Tile[][]>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public static int loadGold(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(GOLD_KEY, 0);
    }

    public static void clearState(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static void saveFloor(Context context, int floor) {
        SharedPreferences prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("floor", floor).apply();
    }

    public static int loadFloor(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        return prefs.getInt("floor", 1);
    }

}

