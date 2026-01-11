package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.DungeonGenerator;
import com.google.gson.Gson;

/**
 * Persists and restores the full dungeon state for up to four save slots.
 */
public class SaveManager {

    private static final String PREFS_NAME_PREFIX = "SaveSlot";
    private static final int TOTAL_SLOTS = 4;
    private static final int SAVE_SCHEMA_VERSION = 1;
    private static final String KEY_SAVE_BLOB = "SaveBlob";
    private static final String KEY_PROFILE = "CharacterProfile";
    private static final String KEY_FLOOR = "CurrentFloor";
    private static final String KEY_GRID = "DungeonGrid";
    private static final String KEY_GOLD = "CurrentGold";
    private static final String KEY_PLATINUM = "CurrentPlatinum";
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
                         int currentPlatinum,
                         Tile[][] dungeonGrid,
                         RunMetadata metadata) {
        validateSlot(slotIndex);
        SaveBlob blob = new SaveBlob(profile,
                currentFloor,
                currentGold,
                currentPlatinum,
                dungeonGrid,
                metadata);
        PersistedBlobStore.save(context, slotPrefsName(slotIndex), KEY_SAVE_BLOB,
                SAVE_SCHEMA_VERSION, gson.toJson(blob));
        clearLegacyKeys(slotPrefs(slotIndex));
    }

    public GameState loadGame(int slotIndex) {
        validateSlot(slotIndex);
        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(
                context,
                slotPrefsName(slotIndex),
                KEY_SAVE_BLOB,
                SAVE_SCHEMA_VERSION
        );
        if (result.status == PersistedBlobStore.LoadResult.Status.OK) {
            return parseBlob(result.json);
        }

        GameState legacy = loadLegacy(slotIndex);
        if (legacy != null) {
            saveGame(slotIndex,
                    legacy.profile,
                    legacy.currentFloor,
                    legacy.currentGold,
                    legacy.currentPlatinum,
                    legacy.dungeonGrid,
                    legacy.metadata);
        }
        return legacy;
    }

    private void migrateGrid(Tile[][] grid, int floor) {
        if (grid == null) {
            return;
        }
        String bigKeyName = DungeonGenerator.getBigKeyNameForFloor(floor);
        for (Tile[] row : grid) {
            if (row == null) {
                continue;
            }
            for (Tile tile : row) {
                if (tile == null) {
                    continue;
                }
                TileType type = tile.getType();
                if (type == TileType.BIG_KEY) {
                    String customName = tile.getCustomName();
                    if (customName == null || customName.isEmpty()) {
                        tile.setCustomName(bigKeyName);
                    }
                }
            }
        }
    }

    public boolean isSlotOccupied(int slotIndex) {
        validateSlot(slotIndex);
        SharedPreferences prefs = slotPrefs(slotIndex);
        return prefs.contains(KEY_SAVE_BLOB) || prefs.contains(KEY_PROFILE);
    }

    public void deleteSave(int slotIndex) {
        validateSlot(slotIndex);
        slotPrefs(slotIndex).edit().clear().apply();
    }

    private SharedPreferences slotPrefs(int slotIndex) {
        return SecurePreferences.get(context, PREFS_NAME_PREFIX + slotIndex);
    }

    private String slotPrefsName(int slotIndex) {
        return PREFS_NAME_PREFIX + slotIndex;
    }

    private void validateSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index" + slotIndex);
        }
    }

    private GameState parseBlob(String json) {
        if (json == null) {
            return null;
        }
        try {
            SaveBlob blob = gson.fromJson(json, SaveBlob.class);
            if (blob == null || blob.profile == null || blob.dungeonGrid == null) {
                return null;
            }
            migrateGrid(blob.dungeonGrid, blob.currentFloor);
            return new GameState(blob.profile,
                    blob.currentFloor,
                    blob.currentGold,
                    blob.currentPlatinum,
                    blob.dungeonGrid,
                    blob.metadata);
        } catch (Exception ignored) {
            return null;
        }
    }

    private GameState loadLegacy(int slotIndex) {
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
            migrateGrid(dungeonGrid, floor);
            int gold = prefs.getInt(KEY_GOLD, 0);
            int platinum = prefs.getInt(KEY_PLATINUM, 0);
            RunMetadata metadata = new RunMetadata(
                    prefs.getInt(KEY_PLAYER_ROW, -1),
                    prefs.getInt(KEY_PLAYER_COL, -1),
                    prefs.getBoolean(KEY_SHIELD_ACTIVE, false),
                    prefs.getInt(KEY_SHIELD_STRENGTH, 0),
                    prefs.getInt(KEY_SHIELD_ROW, -1),
                    prefs.getInt(KEY_SHIELD_COL, -1),
                    prefs.getInt(KEY_ABILITY_READY_FLOOR, 1),
                    null
            );
            return new GameState(profile, floor, gold, platinum, dungeonGrid, metadata);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void clearLegacyKeys(SharedPreferences prefs) {
        prefs.edit()
                .remove(KEY_PROFILE)
                .remove(KEY_FLOOR)
                .remove(KEY_GRID)
                .remove(KEY_GOLD)
                .remove(KEY_PLATINUM)
                .remove(KEY_PLAYER_ROW)
                .remove(KEY_PLAYER_COL)
                .remove(KEY_SHIELD_ACTIVE)
                .remove(KEY_SHIELD_STRENGTH)
                .remove(KEY_SHIELD_ROW)
                .remove(KEY_SHIELD_COL)
                .remove(KEY_ABILITY_READY_FLOOR)
                .apply();
    }

    public static class GameState {
        public final CharacterProfile profile;
        public final int currentFloor;
        public final int currentGold;
        public final int currentPlatinum;
        public final Tile[][] dungeonGrid;
        public final RunMetadata metadata;

        public GameState(CharacterProfile profile,
                         int currentFloor,
                         int currentGold,
                         int currentPlatinum,
                         Tile[][] dungeonGrid,
                         RunMetadata metadata) {
            this.profile = profile;
            this.currentFloor = currentFloor;
            this.currentGold = currentGold;
            this.currentPlatinum = currentPlatinum;
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
        public final String currentTerrain;

        public RunMetadata(int playerRow,
                           int playerCol,
                           boolean knightShieldActive,
                           int knightShieldStrength,
                           int knightShieldRow,
                           int knightShieldCol,
                           int nextAbilityAvailableFloor,
                           String currentTerrain) {
            this.playerRow = playerRow;
            this.playerCol = playerCol;
            this.knightShieldActive = knightShieldActive;
            this.knightShieldStrength = knightShieldStrength;
            this.knightShieldRow = knightShieldRow;
            this.knightShieldCol = knightShieldCol;
            this.nextAbilityAvailableFloor = nextAbilityAvailableFloor;
            this.currentTerrain = currentTerrain;
        }
    }

    private static class SaveBlob {
        final CharacterProfile profile;
        final int currentFloor;
        final int currentGold;
        final int currentPlatinum;
        final Tile[][] dungeonGrid;
        final RunMetadata metadata;

        SaveBlob(CharacterProfile profile,
                 int currentFloor,
                 int currentGold,
                 int currentPlatinum,
                 Tile[][] dungeonGrid,
                 RunMetadata metadata) {
            this.profile = profile;
            this.currentFloor = currentFloor;
            this.currentGold = currentGold;
            this.currentPlatinum = currentPlatinum;
            this.dungeonGrid = dungeonGrid;
            this.metadata = metadata;
        }
    }
}
