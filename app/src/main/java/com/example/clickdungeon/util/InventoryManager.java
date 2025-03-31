package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.InventoryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private static final String PREFS_NAME = "player_prefs";
    private static final String INVENTORY_KEY = "inventory";

    public static void saveInventory(Context context, List<InventoryItem> inventory) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = new Gson().toJson(inventory);
        prefs.edit().putString(INVENTORY_KEY, json).apply();
    }

    public static List<InventoryItem> loadInventory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(INVENTORY_KEY, null);
        Type type = new TypeToken<List<InventoryItem>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }
}
