package com.adaplu.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.ShopItem;
import com.adaplu.clickdungeon.util.BossCatalog;
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
    /** Long-form campaign floor cap used for progression scaling. */
    public static final int FINAL_FLOOR = 99;
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
    /** Max Manhattan distance for targeted class abilities. */
    public static final int ABILITY_TARGET_RANGE = 3;
    /** Radius used by class abilities that affect a 3x3 area. */
    public static final int ABILITY_AREA_RADIUS = 1;
    /** Charges granted by Thief Veil of Smoke. */
    public static final int THIEF_SMOKE_VEIL_CHARGES = 1;
    /** Minimum damage for Wizard Fireball. */
    public static final int WIZARD_FIREBALL_BASE_DAMAGE = 4;
    /** Wizard Fireball bonus over attack power. */
    public static final int WIZARD_FIREBALL_ATTACK_BONUS = 2;
    /** Minimum damage for Wizard Frost Nova. */
    public static final int WIZARD_FROST_NOVA_BASE_DAMAGE = 2;
    /** Minimum primary damage for Wizard Chain Lightning. */
    public static final int WIZARD_CHAIN_LIGHTNING_BASE_DAMAGE = 3;
    /** Wizard Chain Lightning bonus over attack power. */
    public static final int WIZARD_CHAIN_LIGHTNING_ATTACK_BONUS = 1;
    /** Damage drop applied to chained Wizard Chain Lightning targets. */
    public static final int WIZARD_CHAIN_LIGHTNING_CHAIN_FALLOFF = 1;
    /** Minimum damage for Wizard Meteor. */
    public static final int WIZARD_METEOR_BASE_DAMAGE = 6;
    /** Wizard Meteor attack power multiplier. */
    public static final int WIZARD_METEOR_ATTACK_MULTIPLIER = 2;
    /** Wizard Meteor bonus after attack scaling. */
    public static final int WIZARD_METEOR_ATTACK_BONUS = 2;
    /** Minimum heal for Wizard Arcane Shield. */
    public static final int WIZARD_ARCANE_SHIELD_BASE_RESTORE = 4;
    /** Wizard Arcane Shield bonus over defense power. */
    public static final int WIZARD_ARCANE_SHIELD_DEFENSE_BONUS = 4;
    /** Minimum damage for Thief Ambush. */
    public static final int THIEF_AMBUSH_BASE_DAMAGE = 4;
    /** Thief Ambush bonus over attack power. */
    public static final int THIEF_AMBUSH_ATTACK_BONUS = 2;
    /** Minimum heal for Knight Fortify. */
    public static final int KNIGHT_FORTIFY_BASE_HEAL = 4;
    /** Knight Fortify bonus over defense power. */
    public static final int KNIGHT_FORTIFY_DEFENSE_BONUS = 3;
    /** Minimum heal for Knight Guardian's Oath. */
    public static final int KNIGHT_GUARDIANS_OATH_BASE_HEAL = 6;
    /** Knight Guardian's Oath flat HP bonus after max HP scaling. */
    public static final int KNIGHT_GUARDIANS_OATH_HP_BONUS = 4;
    /** Divisor used for Knight Guardian's Oath max HP scaling. */
    public static final int KNIGHT_GUARDIANS_OATH_HP_DIVISOR = 3;
    /** Minimum damage for Knight Valiant Strike. */
    public static final int KNIGHT_VALIANT_STRIKE_BASE_DAMAGE = 6;
    /** Knight Valiant Strike attack power multiplier. */
    public static final int KNIGHT_VALIANT_STRIKE_ATTACK_MULTIPLIER = 2;
    /** Knight Valiant Strike bonus after attack scaling. */
    public static final int KNIGHT_VALIANT_STRIKE_ATTACK_BONUS = 2;
    /** Minimum shield strength for Knight shield abilities. */
    public static final int KNIGHT_SHIELD_BASE_STRENGTH = 6;
    /** Divisor used for Knight shield max HP scaling. */
    public static final int KNIGHT_SHIELD_HP_DIVISOR = 3;
    /** Knight shield defense power multiplier. */
    public static final int KNIGHT_SHIELD_DEFENSE_MULTIPLIER = 2;
    // Ranger ability base damage values
    public static final int RANGER_PIERCING_SHOT_BASE = 3;
    public static final int RANGER_PIERCING_SHOT_ATTACK_BONUS = 1;
    public static final int RANGER_RAPID_VOLLEY_BASE = 2;
    public static final int RANGER_NET_TRAP_DEBUFF = 1;
    public static final int RANGER_NET_TRAP_ATTACK_DIVISOR = 2;
    public static final int RANGER_EAGLE_EYE_BASE = 4;
    public static final int RANGER_EAGLE_EYE_ATTACK_BONUS = 2;
    public static final int RANGER_EAGLE_EYE_CRIT_MULT = 2;
    public static final int RANGER_CAMOUFLAGE_HEAL = 2;
    public static final int RANGER_CAMOUFLAGE_MAX_HP_DIVISOR = 4;
    /** Permanent HP granted by a health shrine tile. */
    public static final int BOOST_HEALTH_GAIN = 2;
    /** Permanent ATK granted by an attack shrine tile. */
    public static final int BOOST_ATTACK_GAIN = 1;
    /** Permanent DEF granted by a defense shrine tile. */
    public static final int BOOST_DEFENSE_GAIN = 1;
    /** Base XP for clearing a floor before scaling. */
    private static final int BASE_FLOOR_CLEAR_XP = 14;
    /** Base XP for finding items before scaling. */
    private static final int BASE_ITEM_FOUND_XP = 4;
    /** Base XP for disabling traps before scaling. */
    private static final int BASE_TRAP_DISABLED_XP = 5;
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

    /** Calculates Wizard Fireball damage from current attack power. */
    public static int calculateWizardFireballDamage(int attackPower) {
        return Math.max(WIZARD_FIREBALL_BASE_DAMAGE,
                sanitizeAbilityPower(attackPower) + WIZARD_FIREBALL_ATTACK_BONUS);
    }

    /** Calculates Wizard Frost Nova damage from current attack power. */
    public static int calculateWizardFrostNovaDamage(int attackPower) {
        return Math.max(WIZARD_FROST_NOVA_BASE_DAMAGE, sanitizeAbilityPower(attackPower));
    }

    /** Calculates Wizard Chain Lightning primary target damage. */
    public static int calculateWizardChainLightningPrimaryDamage(int attackPower) {
        return Math.max(WIZARD_CHAIN_LIGHTNING_BASE_DAMAGE,
                sanitizeAbilityPower(attackPower) + WIZARD_CHAIN_LIGHTNING_ATTACK_BONUS);
    }

    /** Calculates Wizard Chain Lightning secondary target damage. */
    public static int calculateWizardChainLightningChainDamage(int primaryDamage) {
        return Math.max(1, primaryDamage - WIZARD_CHAIN_LIGHTNING_CHAIN_FALLOFF);
    }

    /** Calculates Wizard Meteor damage from current attack power. */
    public static int calculateWizardMeteorDamage(int attackPower) {
        return Math.max(WIZARD_METEOR_BASE_DAMAGE,
                (sanitizeAbilityPower(attackPower) * WIZARD_METEOR_ATTACK_MULTIPLIER)
                        + WIZARD_METEOR_ATTACK_BONUS);
    }

    /** Calculates Wizard Arcane Shield healing. */
    public static int calculateWizardArcaneShieldRestore(int defensePower) {
        return Math.max(WIZARD_ARCANE_SHIELD_BASE_RESTORE,
                sanitizeAbilityPower(defensePower) + WIZARD_ARCANE_SHIELD_DEFENSE_BONUS);
    }

    /** Calculates Thief Ambush damage from current attack power. */
    public static int calculateThiefAmbushDamage(int attackPower) {
        return Math.max(THIEF_AMBUSH_BASE_DAMAGE,
                sanitizeAbilityPower(attackPower) + THIEF_AMBUSH_ATTACK_BONUS);
    }

    /** Calculates Knight Fortify healing from current defense power. */
    public static int calculateKnightFortifyHeal(int defensePower) {
        return Math.max(KNIGHT_FORTIFY_BASE_HEAL,
                sanitizeAbilityPower(defensePower) + KNIGHT_FORTIFY_DEFENSE_BONUS);
    }

    /** Calculates Knight Guardian's Oath healing from max HP. */
    public static int calculateKnightGuardiansOathHeal(int maxHp) {
        return Math.max(KNIGHT_GUARDIANS_OATH_BASE_HEAL,
                (Math.max(0, maxHp) / KNIGHT_GUARDIANS_OATH_HP_DIVISOR)
                        + KNIGHT_GUARDIANS_OATH_HP_BONUS);
    }

    /** Calculates Knight Valiant Strike damage from current attack power. */
    public static int calculateKnightValiantStrikeDamage(int attackPower) {
        return Math.max(KNIGHT_VALIANT_STRIKE_BASE_DAMAGE,
                (sanitizeAbilityPower(attackPower) * KNIGHT_VALIANT_STRIKE_ATTACK_MULTIPLIER)
                        + KNIGHT_VALIANT_STRIKE_ATTACK_BONUS);
    }

    /** Calculates Knight shield strength from max HP and defense power. */
    public static int calculateKnightShieldStrength(int maxHp, int defensePower) {
        return Math.max(KNIGHT_SHIELD_BASE_STRENGTH,
                (Math.max(0, maxHp) / KNIGHT_SHIELD_HP_DIVISOR)
                        + (sanitizeAbilityPower(defensePower) * KNIGHT_SHIELD_DEFENSE_MULTIPLIER));
    }

    /** Calculates Ranger Piercing Shot damage from current attack power. */
    public static int calculateRangerPiercingShotDamage(int attackPower) {
        return Math.max(RANGER_PIERCING_SHOT_BASE,
                sanitizeAbilityPower(attackPower) + RANGER_PIERCING_SHOT_ATTACK_BONUS);
    }

    /** Calculates Ranger Rapid Volley damage from current attack power. */
    public static int calculateRangerRapidVolleyDamage(int attackPower) {
        return Math.max(RANGER_RAPID_VOLLEY_BASE, sanitizeAbilityPower(attackPower));
    }

    /** Calculates Ranger Camouflage healing from max HP. */
    public static int calculateRangerCamouflageHeal(int maxHp) {
        return Math.max(RANGER_CAMOUFLAGE_HEAL,
                Math.max(0, maxHp) / RANGER_CAMOUFLAGE_MAX_HP_DIVISOR);
    }

    /** Calculates Ranger Net Trap direct damage. */
    public static int calculateRangerNetTrapDamage(int attackPower) {
        return Math.max(RANGER_NET_TRAP_DEBUFF,
                RANGER_NET_TRAP_DEBUFF + (sanitizeAbilityPower(attackPower) / RANGER_NET_TRAP_ATTACK_DIVISOR));
    }

    /** Calculates Ranger Eagle Eye base damage before the guaranteed crit multiplier. */
    public static int calculateRangerEagleEyeBaseDamage(int attackPower) {
        return Math.max(RANGER_EAGLE_EYE_BASE,
                sanitizeAbilityPower(attackPower) + RANGER_EAGLE_EYE_ATTACK_BONUS);
    }

    /** Calculates Ranger Eagle Eye guaranteed critical damage. */
    public static int calculateRangerEagleEyeCritDamage(int attackPower) {
        return calculateRangerEagleEyeBaseDamage(attackPower) * RANGER_EAGLE_EYE_CRIT_MULT;
    }

    private static int sanitizeAbilityPower(int value) {
        return Math.max(1, value);
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
        int base = Math.max(2, (monster.getMaxHP() / 2) + (floor / 3) + 2);
        base += random.nextInt(XP_VARIANCE_BOUND + 1);
        base = Math.round(base * getFloorDifficultyScale(floor));
        base = Math.round(base * getXpRewardScale(floor));
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
        base = Math.round(base * getXpRewardScale(floor));
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
        base = Math.round(base * getXpRewardScale(floor));
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
        base = Math.round(base * getXpRewardScale(floor));
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
     * Returns the reward pacing scale for the long campaign.
     * This grows steadily from floor 1 to floor 99 so player progression stays active.
     */
    public static float getFloorDifficultyScale(int floor) {
        if (floor <= 0) {
            return 1f;
        }
        int clampedFloor = Math.min(FINAL_FLOOR, Math.max(1, floor));
        float progress = (clampedFloor - 1) / (float) (FINAL_FLOOR - 1);
        return 1f + (progress * 1.6f); // 1.00 .. 2.60 by floor 99
    }

    /**
     * Returns the combat pacing scale for monsters.
     * This intentionally ramps slower than reward scaling so players have room to grow.
     */
    public static float getMonsterDifficultyScale(int floor) {
        if (floor <= 0) {
            return 1f;
        }
        int clampedFloor = Math.min(FINAL_FLOOR, Math.max(1, floor));
        float progress = (clampedFloor - 1) / (float) (FINAL_FLOOR - 1);
        return 1f + (progress * 0.38f); // 1.00 .. 1.38 by floor 99
    }

    /**
     * Returns a bonus multiplier for XP pacing so players level more steadily than enemy power rises.
     */
    public static float getXpRewardScale(int floor) {
        if (floor <= 0) {
            return 1.10f;
        }
        int clampedFloor = Math.min(FINAL_FLOOR, Math.max(1, floor));
        float progress = (clampedFloor - 1) / (float) (FINAL_FLOOR - 1);
        return 1.10f + (progress * 0.55f); // 1.10 .. 1.65 by floor 99
    }

    /**
     * Returns the actual chance to apply a terrain status on the given floor.
     * Terrain pressure ramps in slowly even after a terrain theme unlocks.
     */
    public static float getTerrainHazardChance(int floor, float lateGameBaseChance) {
        if (lateGameBaseChance <= 0f) {
            return 0f;
        }
        int clampedFloor = Math.min(FINAL_FLOOR, Math.max(1, floor));
        float progress = (clampedFloor - 1) / (float) (FINAL_FLOOR - 1);
        float scale = 0.45f + (progress * 0.40f); // 45% .. 85% of the late-game base rate
        return Math.max(0f, Math.min(1f, lateGameBaseChance * scale));
    }

    /**
     * Returns terrain status duration with early-game mitigation.
     */
    public static int getTerrainHazardTurns(int floor, int lateGameTurns) {
        if (lateGameTurns <= 1) {
            return Math.max(1, lateGameTurns);
        }
        int clampedFloor = Math.min(FINAL_FLOOR, Math.max(1, floor));
        float progress = (clampedFloor - 1) / (float) (FINAL_FLOOR - 1);
        return progress >= 0.65f ? lateGameTurns : Math.max(1, lateGameTurns - 1);
    }

    /** Returns true if the floor should host a boss encounter. */
    public static boolean isBossFloor(int floor) {
        return BossCatalog.isBossFloor(floor);
    }

    /** Calculates bonus XP for defeating a boss. */
    public static int calculateBossXpReward(int floor, SettingsManager.Difficulty difficulty) {
        int base = Math.max(10, floor * 4);
        base = Math.round(base * getXpRewardScale(floor));
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
