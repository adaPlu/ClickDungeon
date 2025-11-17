package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Tile;
import com.google.gson.Gson;

/**
 * Persists and restores the full dungeon state for up to four save slots.
 */
public class SaveManager {

    private static final String PREFS_NAME_PREFIX = "SaveSlot";
    private static final int TOTAL_SLOTS = 4;
    private static final String KEY_PROFILE = "CharacterProfile";
    private static final String KEY_FLOOR = "CurrentFloor";
    private static final String KEY_GRID = "DungeonGrid";
    private static final String KEY_GOLD = "CurrentGold";
    private static final String KEY_PLAYER_ROW = "PlayerRow";
    private static final String KEY_PLAYER_COL = "PlayerCol";
    private static final String KEY_SHIELD_ACTIVE = "KnightShieldActive";
    private static final String KEY_SHIELD_STRENGTH = "KnightShieldStrength";
    private static final String KEY_SHIELD_ROW = "KnightShieldRow";
    private static final String KEY_SHIELD_COL = "KnightShieldCol";
    private static final String KEY_ABILITY_READY_FLOOR = "AbilityReadyFloor";

    private final Context context;
    private final Gson gson = new Gson();

    public SaveManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void saveGame(int slotIndex,
                         CharacterProfile profile,
                         int currentFloor,
                         int currentGold,
                         Tile[][] dungeonGrid,
                         RunMetadata metadata) {
        validateSlot(slotIndex);
        SharedPreferences prefs = slotPrefs(slotIndex);

        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_PROFILE, gson.toJson(profile))
                .putInt(KEY_FLOOR, currentFloor)
                .putString(KEY_GRID, gson.toJson(dungeonGrid))
                .putInt(KEY_GOLD, currentGold);

        if (metadata != null) {
            editor.putInt(KEY_PLAYER_ROW, metadata.playerRow)
                    .putInt(KEY_PLAYER_COL, metadata.playerCol)
                    .putBoolean(KEY_SHIELD_ACTIVE, metadata.knightShieldActive)
                    .putInt(KEY_SHIELD_STRENGTH, metadata.knightShieldStrength)
                    .putInt(KEY_SHIELD_ROW, metadata.knightShieldRow)
                    .putInt(KEY_SHIELD_COL, metadata.knightShieldCol)
                    .putInt(KEY_ABILITY_READY_FLOOR, metadata.nextAbilityAvailableFloor);
        }

        editor.apply();
    }

    public GameState loadGame(int slotIndex) {
        validateSlot(slotIndex);
        SharedPreferences prefs = slotPrefs(slotIndex);
        if (!prefs.contains(KEY_PROFILE)) {
            return null;
        }

        try {
            String profileJson = prefs.getString(KEY_PROFILE, null);
            String gridJson = prefs.getString(KEY_GRID, null);
            if (profileJson == null || gridJson == null) {
                return null;
            }

            CharacterProfile profile = gson.fromJson(profileJson, CharacterProfile.class);
            Tile[][] dungeonGrid = gson.fromJson(gridJson, Tile[][].class);
            if (profile == null || dungeonGrid == null) {
                return null;
            }

            int floor = prefs.getInt(KEY_FLOOR, 1);
            int gold = prefs.getInt(KEY_GOLD, 0);
            RunMetadata metadata = new RunMetadata(
                    prefs.getInt(KEY_PLAYER_ROW, -1),
                    prefs.getInt(KEY_PLAYER_COL, -1),
                    prefs.getBoolean(KEY_SHIELD_ACTIVE, false),
                    prefs.getInt(KEY_SHIELD_STRENGTH, 0),
                    prefs.getInt(KEY_SHIELD_ROW, -1),
                    prefs.getInt(KEY_SHIELD_COL, -1),
                    prefs.getInt(KEY_ABILITY_READY_FLOOR, 1)
            );
            return new GameState(profile, floor, gold, dungeonGrid, metadata);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isSlotOccupied(int slotIndex) {
        validateSlot(slotIndex);
        return slotPrefs(slotIndex).contains(KEY_PROFILE);
    }

    public void deleteSave(int slotIndex) {
        validateSlot(slotIndex);
        slotPrefs(slotIndex).edit().clear().apply();
    }

    private SharedPreferences slotPrefs(int slotIndex) {
        return context.getSharedPreferences(PREFS_NAME_PREFIX + slotIndex, Context.MODE_PRIVATE);
    }

    private void validateSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index" + slotIndex);
        }
    }

    public static class GameState {
        public final CharacterProfile profile;
        public final int currentFloor;
        public final int currentGold;
        public final Tile[][] dungeonGrid;
        public final RunMetadata metadata;

        public GameState(CharacterProfile profile,
                         int currentFloor,
                         int currentGold,
                         Tile[][] dungeonGrid,
                         RunMetadata metadata) {
            this.profile = profile;
            this.currentFloor = currentFloor;
            this.currentGold = currentGold;
            this.dungeonGrid = dungeonGrid;
            this.metadata = metadata;
        }
    }

    public static class RunMetadata {
        public final int playerRow;
        public final int playerCol;
        public final boolean knightShieldActive;
        public final int knightShieldStrength;
        public final int knightShieldRow;
        public final int knightShieldCol;
        public final int nextAbilityAvailableFloor;

        public RunMetadata(int playerRow,
                           int playerCol,
                           boolean knightShieldActive,
                           int knightShieldStrength,
                           int knightShieldRow,
                           int knightShieldCol,
                           int nextAbilityAvailableFloor) {
            this.playerRow = playerRow;
            this.playerCol = playerCol;
            this.knightShieldActive = knightShieldActive;
            this.knightShieldStrength = knightShieldStrength;
            this.knightShieldRow = knightShieldRow;
            this.knightShieldCol = knightShieldCol;
            this.nextAbilityAvailableFloor = nextAbilityAvailableFloor;
        }
    }
}
