package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.PricedItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MerchantManager {

    private static final String PREFS_NAME = "merchant_prefs";
    private static final String KEY_BUYBACK_ITEMS = "buyback_items";
    private static final String KEY_LAST_VISIT_FLOOR = "last_visit_floor";

    private MerchantManager() {
    }

    public static int getLastVisitFloor(Context context) {
        return getPrefs(context).getInt(KEY_LAST_VISIT_FLOOR, -1);
    }

    public static void setLastVisitFloor(Context context, int floor) {
        getPrefs(context).edit().putInt(KEY_LAST_VISIT_FLOOR, floor).apply();
    }

    public static List<PricedItem> loadBuybackItems(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String json = prefs.getString(KEY_BUYBACK_ITEMS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<PricedItem>>() {}.getType();
        List<PricedItem> items = new Gson().fromJson(json, type);
        return items != null ? items : new ArrayList<>();
    }

    public static void saveBuybackItems(Context context, List<PricedItem> items) {
        String json = new Gson().toJson(items);
        getPrefs(context).edit().putString(KEY_BUYBACK_ITEMS, json).apply();
    }

    public static void clearBuybackItems(Context context) {
        getPrefs(context).edit().remove(KEY_BUYBACK_ITEMS).apply();
    }

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

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

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
