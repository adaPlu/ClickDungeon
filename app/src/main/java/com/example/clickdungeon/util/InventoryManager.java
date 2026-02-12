package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.InventoryItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralized inventory persistence and currency helpers.
 */
public class InventoryManager {

    /** Shared preferences file for inventory and currency values. */
    private static final String PREFS_NAME = "player_prefs";
    /** Storage key for the inventory JSON blob. */
    private static final String INVENTORY_KEY = "inventory";
    /** Schema version for inventory persistence. */
    private static final int INVENTORY_SCHEMA_VERSION = 1;
    /** Key for gold balance. */
    private static final String GOLD_KEY = "gold";
    /** Key for platinum balance. */
    private static final String PLATINUM_KEY = "platinum";
    /** Key for premium inventory list. */
    private static final String PREMIUM_KEY = "premium_inventory";
    /** Key for recent inventory change log. */
    private static final String CHANGE_LOG_KEY = "inventory_change_log";
    /** Max entries stored for recent inventory changes. */
    private static final int CHANGE_LOG_LIMIT = 10;
    /** Default gold for a new run. */
    private static final int DEFAULT_GOLD = GameBalance.STARTING_GOLD;
    /** Default platinum for a new run. */
    private static final int DEFAULT_PLATINUM = GameBalance.STARTING_PLATINUM;

    private InventoryManager() {
    }

    /** Returns the secure preferences wrapper for inventory data. */
    private static SharedPreferences getPrefs(Context context) {
        return SecurePreferences.get(context, PREFS_NAME);
    }

    /**
     * Loads the persisted inventory list.
     */
    public static synchronized List<InventoryItem> loadInventory(Context context) {
        PersistedBlobStore.LoadResult result =
                PersistedBlobStore.load(context, PREFS_NAME, INVENTORY_KEY, INVENTORY_SCHEMA_VERSION);
        String json = result.status == PersistedBlobStore.LoadResult.Status.OK ? result.json : null;
        Type type = new TypeToken<List<InventoryItem>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }

    /**
     * Saves the inventory list to persistence storage.
     */
    public static synchronized void saveInventory(Context context, List<InventoryItem> inventory) {
        String json = new Gson().toJson(inventory);
        PersistedBlobStore.save(context, PREFS_NAME, INVENTORY_KEY, INVENTORY_SCHEMA_VERSION, json);
    }

    /**
     * Adjusts an item's quantity with an unbounded max stack.
     */
    public static synchronized void adjustItemQuantity(Context context, String itemName, int delta) {
        adjustItemQuantity(context, itemName, delta, Integer.MAX_VALUE);
    }

    /**
     * Adjusts an item's quantity with a caller-provided max stack.
     */
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
                // Remove empty stacks instead of persisting zero quantities.
                inventory.remove(index);
                saveInventory(context, inventory);
            }
            return;
        }

        // Clamp stacks to a positive range so callers cannot persist negative or zero values.
        newQuantity = Math.min(newQuantity, Math.max(1, maxQuantity));
        InventoryItem updated = new InventoryItem(itemName, newQuantity);
        if (index >= 0) {
            inventory.set(index, updated);
        } else {
            inventory.add(updated);
        }
        saveInventory(context, inventory);
    }

    /**
     * Returns the quantity for a named item, or 0 if missing.
     */
    public static synchronized int getItemQuantity(Context context, String itemName) {
        if (itemName == null) {
            return 0;
        }
        List<InventoryItem> inventory = loadInventory(context);
        int index = findItemIndex(inventory, itemName);
        return index >= 0 ? inventory.get(index).getQuantity() : 0;
    }

    /** Returns the current gold balance. */
    public static synchronized int getGold(Context context) {
        return Math.max(0, getPrefs(context).getInt(GOLD_KEY, DEFAULT_GOLD));
    }

    /** Sets the gold balance and returns the sanitized value. */
    public static synchronized int setGold(Context context, int amount) {
        int sanitized = Math.max(0, amount);
        getPrefs(context).edit().putInt(GOLD_KEY, sanitized).apply();
        return sanitized;
    }

    /** Adjusts the gold balance and returns the updated value. */
    public static synchronized int adjustGold(Context context, int delta) {
        int updated = getGold(context) + delta;
        return setGold(context, updated);
    }

    /** Returns the current platinum balance. */
    public static synchronized int getPlatinum(Context context) {
        return Math.max(0, getPrefs(context).getInt(PLATINUM_KEY, DEFAULT_PLATINUM));
    }

    /** Sets the platinum balance and returns the sanitized value. */
    public static synchronized int setPlatinum(Context context, int amount) {
        int sanitized = Math.max(0, amount);
        getPrefs(context).edit().putInt(PLATINUM_KEY, sanitized).apply();
        return sanitized;
    }

    /** Adjusts the platinum balance and returns the updated value. */
    public static synchronized int adjustPlatinum(Context context, int delta) {
        int updated = getPlatinum(context) + delta;
        return setPlatinum(context, updated);
    }

    /**
     * Returns the stored premium inventory list (platinum purchases).
     */
    public static synchronized java.util.List<InventoryItem> loadPremiumInventory(Context context) {
        PersistedBlobStore.LoadResult result =
                PersistedBlobStore.load(context, PREFS_NAME, PREMIUM_KEY, INVENTORY_SCHEMA_VERSION);
        String json = result.status == PersistedBlobStore.LoadResult.Status.OK ? result.json : null;
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<InventoryItem>>() {}.getType();
        return json != null ? new com.google.gson.Gson().fromJson(json, type) : new java.util.ArrayList<>();
    }

    /**
     * Saves the premium inventory list.
     */
    public static synchronized void savePremiumInventory(Context context, java.util.List<InventoryItem> items) {
        String json = new com.google.gson.Gson().toJson(items);
        PersistedBlobStore.save(context, PREFS_NAME, PREMIUM_KEY, INVENTORY_SCHEMA_VERSION, json);
    }

    /**
     * Adjusts a premium inventory item quantity.
     */
    public static synchronized void adjustPremiumItemQuantity(Context context, String itemName, int delta, int maxQuantity) {
        if (itemName == null || itemName.trim().isEmpty() || delta == 0) {
            return;
        }
        java.util.List<InventoryItem> inventory = loadPremiumInventory(context);
        int index = findItemIndex(inventory, itemName);
        int newQuantity = delta;
        if (index >= 0) {
            newQuantity = inventory.get(index).getQuantity() + delta;
        }
        if (newQuantity <= 0) {
            if (index >= 0) {
                inventory.remove(index);
                savePremiumInventory(context, inventory);
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
        savePremiumInventory(context, inventory);
    }

    /**
     * Test/support hook to wipe inventory state for a fresh scenario.
     */
    public static synchronized void clearInventory(Context context) {
        PersistedBlobStore.clear(context, PREFS_NAME, INVENTORY_KEY);
    }

    /**
     * Records a recent inventory change entry for UI display.
     */
    public static synchronized void recordInventoryChange(Context context, String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return;
        }
        java.util.List<String> log = getInventoryChangeLog(context);
        log.add(0, entry);
        while (log.size() > CHANGE_LOG_LIMIT) {
            log.remove(log.size() - 1);
        }
        persistInventoryChangeLog(context, log);
    }

    /**
     * Returns a copy of the recent inventory change log.
     */
    public static synchronized java.util.List<String> getInventoryChangeLog(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String json = prefs.getString(CHANGE_LOG_KEY, null);
        java.util.List<String> log = new java.util.ArrayList<>();
        if (json != null) {
            try {
                java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<String>>() {}.getType();
                java.util.List<String> parsed = new com.google.gson.Gson().fromJson(json, type);
                if (parsed != null) {
                    log.addAll(parsed);
                }
            } catch (Exception ignored) {
                // Fall back to empty log on parse errors.
            }
        }
        return log;
    }

    private static void persistInventoryChangeLog(Context context, java.util.List<String> log) {
        String json = new com.google.gson.Gson().toJson(log);
        getPrefs(context).edit().putString(CHANGE_LOG_KEY, json).apply();
    }

    /**
     * Convenience lookup used in tests; returns the first matching item or null.
     */
    public static synchronized InventoryItem getInventoryItem(Context context, String itemName) {
        if (itemName == null) {
            return null;
        }
        List<InventoryItem> inventory = loadInventory(context);
        for (InventoryItem item : inventory) {
            if (itemName.equals(item.getName())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Syncs gold to match the active run state.
     */
    public static synchronized void syncGoldWithCurrentRun(Context context, int runGold) {
        setGold(context, runGold);
    }

    /**
     * Syncs platinum to match the active run state.
     */
    public static synchronized void syncPlatinumWithCurrentRun(Context context, int runPlatinum) {
        setPlatinum(context, runPlatinum);
    }

    /**
     * Returns the index of a named item in the inventory list, or -1.
     */
    private static int findItemIndex(List<InventoryItem> inventory, String itemName) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getName().equals(itemName)) {
                return i;
            }
        }
        return -1;
    }
}
