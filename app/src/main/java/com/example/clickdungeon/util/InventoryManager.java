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
    private static final String GOLD_KEY = "gold";
    private static final int DEFAULT_GOLD = 500;

    private InventoryManager() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized List<InventoryItem> loadInventory(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String json = prefs.getString(INVENTORY_KEY, null);
        Type type = new TypeToken<List<InventoryItem>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }

    public static synchronized void saveInventory(Context context, List<InventoryItem> inventory) {
        SharedPreferences prefs = getPrefs(context);
        String json = new Gson().toJson(inventory);
        prefs.edit().putString(INVENTORY_KEY, json).apply();
    }

    public static synchronized void adjustItemQuantity(Context context, String itemName, int delta) {
        adjustItemQuantity(context, itemName, delta, Integer.MAX_VALUE);
    }

    public static synchronized void adjustItemQuantity(Context context, String itemName, int delta, int maxQuantity) {
        if (itemName == null || itemName.trim().isEmpty() || delta == 0) {
            return;
        }

        List<InventoryItem> inventory = loadInventory(context);
        int index = findItemIndex(inventory, itemName);
        int newQuantity = delta;
        if (index >= 0) {
            newQuantity = inventory.get(index).getQuantity() + delta;
        }

        if (newQuantity <= 0) {
            if (index >= 0) {
                inventory.remove(index);
                saveInventory(context, inventory);
            }
            return;
        }

        newQuantity = Math.min(newQuantity, Math.max(1, maxQuantity));
        InventoryItem updated = new InventoryItem(itemName, newQuantity);
        if (index >= 0) {
            inventory.set(index, updated);
        } else {
            inventory.add(updated);
        }
        saveInventory(context, inventory);
    }

    public static synchronized int getItemQuantity(Context context, String itemName) {
        if (itemName == null) {
            return 0;
        }
        List<InventoryItem> inventory = loadInventory(context);
        int index = findItemIndex(inventory, itemName);
        return index >= 0 ? inventory.get(index).getQuantity() : 0;
    }

    public static synchronized int getGold(Context context) {
        return Math.max(0, getPrefs(context).getInt(GOLD_KEY, DEFAULT_GOLD));
    }

    public static synchronized int setGold(Context context, int amount) {
        int sanitized = Math.max(0, amount);
        getPrefs(context).edit().putInt(GOLD_KEY, sanitized).apply();
        return sanitized;
    }

    public static synchronized int adjustGold(Context context, int delta) {
        int updated = getGold(context) + delta;
        return setGold(context, updated);
    }

    public static synchronized void syncGoldWithCurrentRun(Context context, int runGold) {
        setGold(context, runGold);
    }

    private static int findItemIndex(List<InventoryItem> inventory, String itemName) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getName().equals(itemName)) {
                return i;
            }
        }
        return -1;
    }
}
