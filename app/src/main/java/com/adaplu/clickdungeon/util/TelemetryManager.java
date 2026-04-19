package com.adaplu.clickdungeon.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * TelemetryManager wraps FirebaseAnalytics and provides a typed API for
 * logging ClickDungeon game events. All methods are static after init().
 *
 * Collection is gated by FirebaseCrashlytics collection setting — in debug
 * builds analytics is still forwarded to the Analytics DebugView but no
 * data is retained in production unless the release build is running.
 *
 * Event schema: see docs/TELEMETRY_EVENTS.md
 */
public final class TelemetryManager {

    private static FirebaseAnalytics sAnalytics;

    private TelemetryManager() {}

    /** Call once from ClickDungeonApp.onCreate(). */
    public static void init(Context context) {
        sAnalytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
    }

    // -------------------------------------------------------------------------
    // Session
    // -------------------------------------------------------------------------

    /** Fired on every app launch. Uses the built-in app_open event. */
    public static void logSessionStart() {
        if (sAnalytics == null) return;
        sAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }

    // -------------------------------------------------------------------------
    // Run lifecycle
    // -------------------------------------------------------------------------

    /**
     * Fired when the player starts a new dungeon run.
     *
     * @param playerClass  e.g. "KNIGHT"
     * @param difficulty   e.g. "NORMAL"
     * @param saveSlot     0-3
     */
    public static void logRunStart(String playerClass, String difficulty, int saveSlot) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putString("player_class", playerClass);
        b.putString("difficulty", difficulty);
        b.putInt("save_slot", saveSlot);
        sAnalytics.logEvent("run_start", b);
    }

    /**
     * Fired when the player's HP hits 0.
     *
     * @param floor        floor the run ended on
     * @param playerClass  active class
     * @param level        character level at death
     * @param goldEarned   gold accumulated this run
     */
    public static void logRunFailed(int floor, String playerClass, int level, int goldEarned) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putInt("failure_floor", floor);
        b.putString("player_class", playerClass);
        b.putInt("player_level", level);
        b.putInt("gold_earned", goldEarned);
        sAnalytics.logEvent("run_failed", b);
    }

    /**
     * Fired when the player clears the final floor.
     *
     * @param finalFloor   highest floor reached
     * @param playerClass  active class
     * @param level        character level at victory
     * @param goldEarned   gold accumulated this run
     */
    public static void logRunCompleted(int finalFloor, String playerClass, int level, int goldEarned) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putInt("final_floor", finalFloor);
        b.putString("player_class", playerClass);
        b.putInt("player_level", level);
        b.putInt("gold_earned", goldEarned);
        sAnalytics.logEvent("run_completed", b);
    }

    // -------------------------------------------------------------------------
    // Combat
    // -------------------------------------------------------------------------

    /**
     * Fired when combat ends.
     *
     * @param outcome   "VICTORY", "DEFEAT", or "FLED"
     * @param enemyName monster display name
     * @param floor     current floor
     */
    public static void logCombatEnded(String outcome, String enemyName, int floor) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putString("outcome", outcome);
        b.putString("enemy_name", enemyName);
        b.putInt("floor_number", floor);
        sAnalytics.logEvent("combat_ended", b);
    }

    // -------------------------------------------------------------------------
    // Progression
    // -------------------------------------------------------------------------

    /**
     * Fired when the player gains a level.
     *
     * @param newLevel  character level after gain
     * @param floor     current floor
     */
    public static void logLevelUp(int newLevel, int floor) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putInt(FirebaseAnalytics.Param.LEVEL, newLevel);
        b.putInt("floor_number", floor);
        sAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, b);
    }

    // -------------------------------------------------------------------------
    // Economy
    // -------------------------------------------------------------------------

    /**
     * Fired on any shop or premium store purchase.
     *
     * @param itemName   item identifier
     * @param currency   "GOLD" or "PLATINUM"
     * @param price      amount spent
     */
    public static void logPurchase(String itemName, String currency, int price) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        b.putString(FirebaseAnalytics.Param.CURRENCY, currency);
        b.putDouble(FirebaseAnalytics.Param.VALUE, price);
        sAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, b);
    }

    // -------------------------------------------------------------------------
    // Exploration
    // -------------------------------------------------------------------------

    /**
     * Fired when the player descends to a new floor.
     *
     * @param floor       floor number just entered
     * @param terrain     terrain type name, e.g. "CAVERN"
     * @param isBossFloor whether this floor has a boss encounter
     */
    public static void logFloorReached(int floor, String terrain, boolean isBossFloor) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putInt("floor_number", floor);
        b.putString("terrain", terrain);
        b.putBoolean("is_boss_floor", isBossFloor);
        sAnalytics.logEvent("floor_reached", b);
    }

    // -------------------------------------------------------------------------
    // Abilities
    // -------------------------------------------------------------------------

    /**
     * Fired when the player activates a class ability.
     *
     * @param abilityName  ability identifier, e.g. "Fireball"
     * @param playerClass  active class name, e.g. "WIZARD"
     * @param floor        current floor
     */
    public static void logAbilityUsed(String abilityName, String playerClass, int floor) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putString("ability_name", abilityName);
        b.putString("player_class", playerClass);
        b.putInt("floor_number", floor);
        sAnalytics.logEvent("ability_used", b);
    }

    // -------------------------------------------------------------------------
    // Errors / health
    // -------------------------------------------------------------------------

    /**
     * Fired when a save operation fails. Also records a non-fatal to Crashlytics.
     *
     * @param reason  short reason string, e.g. "ENCRYPTION_ERROR"
     * @param slot    save slot index
     */
    public static void logSaveFailed(String reason, int slot) {
        if (sAnalytics == null) return;
        Bundle b = new Bundle();
        b.putString("reason", reason);
        b.putInt("save_slot", slot);
        sAnalytics.logEvent("save_failed", b);
        // Also surface as a Crashlytics non-fatal so it shows in the crash dashboard.
        try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(new RuntimeException("save_failed: " + reason + " slot=" + slot));
        } catch (Exception ignored) {}
    }
}
