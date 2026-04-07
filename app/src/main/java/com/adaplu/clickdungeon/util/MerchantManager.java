package com.adaplu.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.adaplu.clickdungeon.model.PricedItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists merchant visit state and buyback inventory.
 */
public final class MerchantManager {

    /** Shared preferences for merchant-specific state. */
    private static final String PREFS_NAME = "merchant_prefs";
    /** Storage key for the serialized buyback item list. */
    private static final String KEY_BUYBACK_ITEMS = "buyback_items";
    /** Storage key for the last merchant floor visited. */
    private static final String KEY_LAST_VISIT_FLOOR = "last_visit_floor";

    private static final Gson GSON = new Gson();

    private MerchantManager() {
    }

    /** Returns the last floor where a merchant was visited. */
    public static int getLastVisitFloor(Context context) {
        return getPrefs(context).getInt(KEY_LAST_VISIT_FLOOR, -1);
    }

    /** Persists the last floor where a merchant was visited. */
    public static void setLastVisitFloor(Context context, int floor) {
        getPrefs(context).edit().putInt(KEY_LAST_VISIT_FLOOR, floor).apply();
    }

    /** Loads buyback items from preferences. */
    public static List<PricedItem> loadBuybackItems(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String json = prefs.getString(KEY_BUYBACK_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<PricedItem>>() {}.getType();
        List<PricedItem> items = GSON.fromJson(json, type);
        return items != null ? items : new ArrayList<>();
    }

    /** Saves buyback items to preferences. */
    public static void saveBuybackItems(Context context, List<PricedItem> items) {
        String json = GSON.toJson(items);
        getPrefs(context).edit().putString(KEY_BUYBACK_ITEMS, json).apply();
    }

    /** Clears all buyback items. */
    public static void clearBuybackItems(Context context) {
        getPrefs(context).edit().remove(KEY_BUYBACK_ITEMS).apply();
    }

    /** Adds or increments a buyback item entry. */
    public static void addBuybackItem(Context context, PricedItem item) {
        List<PricedItem> items = loadBuybackItems(context);
        int index = findItemIndex(items, item.getName(), item.getPrice());
        if (index >= 0) {
            PricedItem existing = items.get(index);
            items.set(index, existing.withQuantity(existing.getQuantity() + item.getQuantity()));
        } else {
            items.add(item);
        }
        saveBuybackItems(context, items);
    }

    /** Returns the merchant-specific preferences handle. */
    private static SharedPreferences getPrefs(Context context) {
        return SecurePreferences.get(context, PREFS_NAME);
    }

    /** Finds an existing buyback entry by name and price. */
    private static int findItemIndex(List<PricedItem> items, String name, int price) {
        for (int i = 0; i < items.size(); i++) {
            PricedItem item = items.get(i);
            if (item.getName().equals(name) && item.getPrice() == price) {
                return i;
            }
        }
        return -1;
    }
}
