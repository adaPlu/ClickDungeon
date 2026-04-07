package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.BossCatalog;
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

    /** Starting gold for a new run. */
    public static final int STARTING_GOLD = 50;
    /** Starting premium currency for a new run. */
    public static final int STARTING_PLATINUM = 100;
    /** MP cost for the Wizard's Meteor ability. */
    public static final int WIZARD_MP_COST_METEOR = 7;
    /** MP cost for the Wizard's Chain Lightning ability. */
    public static final int WIZARD_MP_COST_CHAIN_LIGHTNING = 5;
    /** MP cost for the Wizard's Frost Nova ability. */
    public static final int WIZARD_MP_COST_FROST_NOVA = 4;
    /** MP cost for the Wizard's Arcane Shield ability. */
    public static final int WIZARD_MP_COST_ARCANE_SHIELD = 4;
    /** Default MP cost for unrecognized Wizard abilities. */
    public static final int WIZARD_MP_COST_DEFAULT = 3;
    /** Base XP for clearing a floor before scaling. */
    private static final int BASE_FLOOR_CLEAR_XP = 6;
    /** Base XP for finding items before scaling. */
    private static final int BASE_ITEM_FOUND_XP = 2;
    /** Base XP for disabling traps before scaling. */
    private static final int BASE_TRAP_DISABLED_XP = 3;
    /** Random variance for gold rewards. */
    private static final int GOLD_VARIANCE_BOUND = 3;
    /** Random variance for XP rewards. */
    private static final int XP_VARIANCE_BOUND = 2;
    /** Chance out of 100 to drop bonus gold. */
    private static final int MONSTER_LOOT_GOLD_CHANCE = 35;
    /** Chance out of 100 to drop a small key. */
    private static final int MONSTER_LOOT_SMALL_KEY_CHANCE = 12;
    /** Chance out of 100 to drop a base item. */
    private static final int MONSTER_LOOT_ITEM_CHANCE = 20;
    /** Chance out of 100 to drop a magic item. */
    private static final int MONSTER_LOOT_MAGIC_ITEM_CHANCE = 6;
    /** Shared prefs file storing shop stock overrides. */
    private static final String SHOP_PREFS = "shop_prefs";
    /** Prefix for per-item stock keys in prefs. */
    private static final String SHOP_STOCK_PREFIX = "stock_";
    /** Cached base shop list loaded from JSON. */
    private static List<ShopItem> cachedShopItems;
    /** Cached premium shop list loaded from JSON. */
    private static List<ShopItem> cachedPremiumItems;

    private GameBalance() {
    }

    /**
     * Calculates gold reward for defeating a monster.
     */
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

    /**
     * Calculates XP reward for defeating a monster.
     */
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

    /**
     * Calculates XP for clearing a floor.
     */
    public static int calculateFloorClearXp(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(1, BASE_FLOOR_CLEAR_XP);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return Math.max(1, base);
    }

    /**
     * Calculates XP for discovering loot on a floor.
     */
    public static int calculateItemFoundXp(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(1, BASE_ITEM_FOUND_XP);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return Math.max(1, base);
    }

    /**
     * Calculates XP for disabling or disarming a trap.
     */
    public static int calculateTrapDisabledXp(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(1, BASE_TRAP_DISABLED_XP);
        base = Math.round(base * getFloorDifficultyScale(floor));
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return Math.max(1, base);
    }

    /**
     * Determines extra loot drops for a defeated monster.
     */
    public static MonsterLootRoll rollMonsterLoot(int floor,
                                                  SettingsManager.Difficulty difficulty,
                                                  Random random) {
        int bonusGold = 0;
        int smallKeys = 0;
        java.util.List<String> items = new java.util.ArrayList<>();
        java.util.List<String> magicItems = new java.util.ArrayList<>();

        int roll = random.nextInt(100);
        if (roll < MONSTER_LOOT_GOLD_CHANCE) {
            bonusGold = Math.max(1, Math.round(calculateGoldPile(floor, difficulty, random) * 0.5f));
        }

        roll = random.nextInt(100);
        if (roll < MONSTER_LOOT_SMALL_KEY_CHANCE) {
            smallKeys = 1;
        }

        roll = random.nextInt(100);
        if (roll < MONSTER_LOOT_MAGIC_ITEM_CHANCE) {
            magicItems.add(ItemCatalog.randomMagicItem(random).getName());
        } else if (roll < MONSTER_LOOT_MAGIC_ITEM_CHANCE + MONSTER_LOOT_ITEM_CHANCE) {
            items.add(ItemCatalog.randomBaseItem(random).getName());
        }

        return new MonsterLootRoll(bonusGold, smallKeys, items, magicItems);
    }

    /**
     * Calculates gold pile value for floor loot.
     */
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

    /** Returns true if the floor should host a boss encounter. */
    public static boolean isBossFloor(int floor) {
        return BossCatalog.isBossFloor(floor);
    }

    /** Calculates bonus XP for defeating a boss. */
    public static int calculateBossXpReward(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(10, floor * 4);
        if (difficulty != null) {
            base = difficulty.scaleXpReward(base);
        }
        return base;
    }

    /** Calculates bonus gold for defeating a boss. */
    public static int calculateBossGoldReward(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(15, floor * 3);
        if (difficulty != null) {
            base = difficulty.scaleGoldReward(base);
        }
        return base;
    }

    /**
     * Loads shop stock with persisted overrides applied.
     */
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

    /**
     * Dev-only helper to force reloading shop items from JSON and return the resolved list.
     */
    public static synchronized List<ShopItem> reloadShopItemsForDebug(Context context) {
        cachedShopItems = null;
        return loadShopItems(context);
    }

    /**
     * Persists stock for a specific item and invalidates the cache.
     */
    public static synchronized void persistShopStock(Context context, String itemName, int stock) {
        context.getSharedPreferences(SHOP_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(SHOP_STOCK_PREFIX + itemName, Math.max(0, stock))
                .apply();
        cachedShopItems = null; // force reload next time so overrides are applied.
    }

    /**
     * Loads premium shop items from JSON (no persisted stock overrides yet).
     */
    public static synchronized List<ShopItem> loadPremiumItems(Context context) {
        if (cachedPremiumItems == null) {
            cachedPremiumItems = readShopItemsFromJson(context, R.raw.premium_items);
        }
        return cachedPremiumItems != null ? new ArrayList<>(cachedPremiumItems) : new ArrayList<>();
    }

    /**
     * Reads base shop items from a raw JSON resource.
     */
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
