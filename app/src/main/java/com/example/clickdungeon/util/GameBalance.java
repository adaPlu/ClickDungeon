package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.ShopItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Type;

/**
 * Centralizes reward calculations and encounter pacing so the same tuning is applied everywhere.
 */
public final class GameBalance {

    private static final int GOLD_VARIANCE_BOUND = 3;
    private static final int XP_VARIANCE_BOUND = 2;
    private static final String SHOP_PREFS = "shop_prefs";
    private static final String SHOP_STOCK_PREFIX = "stock_";
    private static List<ShopItem> cachedShopItems;

    private GameBalance() {
    }

    public static int calculateGoldReward(@NonNull Monster monster,
                                          int floor,
                                          SettingsManager.Difficulty difficulty,
                                          Random random) {
        int base = Math.max(2, (monster.getMaxHP() / 3) + floor + (monster.getAttack() / 2));
        base += random.nextInt(GOLD_VARIANCE_BOUND);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty != null) {
            base = difficulty.scaleGoldReward(base);
        }
        return Math.max(1, base);
    }

    public static int calculateXpReward(@NonNull Monster monster,
                                        int floor,
                                        SettingsManager.Difficulty difficulty,
                                        Random random) {
        int base = Math.max(1, (monster.getMaxHP() / 2) + (floor / 2));
        base += random.nextInt(XP_VARIANCE_BOUND + 1);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return Math.max(1, base);
    }

    public static int calculateGoldPile(int floor,
                                        SettingsManager.Difficulty difficulty,
                                        Random random) {
        int base = Math.max(1, floor + 1);
        base += random.nextInt(GOLD_VARIANCE_BOUND);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty == SettingsManager.Difficulty.HARDCORE) {
            base += 1; // Reward riskier play with slightly richer drops.
        } else if (difficulty == SettingsManager.Difficulty.CASUAL) {
            base = Math.max(1, base - 1);
        }
        return Math.max(1, base);
    }

    /**
     * Returns a gentle scaling factor per floor. Floors 1-5: light ramp; 6-10: moderate; 11-15: hardest.
     */
    public static float getFloorDifficultyScale(int floor) {
        if (floor <= 0) return 1f;
        if (floor <= 5) {
            return 1f + (Math.max(0, floor - 1) * 0.03f); // up to ~1.12
        } else if (floor <= 10) {
            return 1.12f + ((floor - 5) * 0.04f); // 1.16..1.32
        } else {
            return 1.32f + ((Math.min(15, floor) - 10) * 0.05f); // 1.37..1.57 at F15
        }
    }

    public static synchronized List<ShopItem> loadShopItems(Context context) {
        if (cachedShopItems == null) {
            // Read base stock from JSON once; per-session quantities are layered on via prefs.
            cachedShopItems = readShopItemsFromJson(context, R.raw.shop_items);
        }
        // Apply persisted stock overrides so each run sees updated quantities.
        SharedPreferences prefs = context.getSharedPreferences(SHOP_PREFS, Context.MODE_PRIVATE);
        List<ShopItem> resolved = new ArrayList<>();
        for (ShopItem item : cachedShopItems) {
            int stock = prefs.getInt(SHOP_STOCK_PREFIX + item.getName(), item.getStock());
            resolved.add(item.withStock(Math.max(0, stock)));
        }
        return resolved;
    }

    public static synchronized void persistShopStock(Context context, String itemName, int stock) {
        context.getSharedPreferences(SHOP_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(SHOP_STOCK_PREFIX + itemName, Math.max(0, stock))
                .apply();
        cachedShopItems = null; // force reload next time so overrides are applied.
    }

    private static List<ShopItem> readShopItemsFromJson(Context context, @RawRes int resId) {
        List<ShopItem> result = new ArrayList<>();
        try (InputStream stream = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            Type listType = new TypeToken<List<ShopItem>>() { }.getType();
            result = new Gson().fromJson(reader, listType);
        } catch (IOException | RuntimeException ignored) {
            // If parsing fails, fall back to an empty shop.
        }
        return result != null ? result : new ArrayList<>();
    }
}
