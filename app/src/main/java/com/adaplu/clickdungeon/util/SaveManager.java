package com.adaplu.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.VisibleForTesting;
import android.util.Log;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.adaplu.clickdungeon.util.DungeonGenerator;
import com.google.gson.Gson;

/**
 * SaveManager handles the serialization and persistence of game sessions.
 * It supports multi-slot saving (4 slots), background snapshotting to avoid UI lag,
 * and maintains data integrity using checksums via {@link PersistedBlobStore}.
 */
public class SaveManager {

    /** Prefix used for per-slot SharedPreferences names. */
    private static final String PREFS_NAME_PREFIX = "SaveSlot";
    /** Number of supported save slots. */
    private static final int TOTAL_SLOTS = 4;
    /** Schema version for the main save blob. */
    private static final int SAVE_SCHEMA_VERSION = 1;
    
    // Preference keys for the primary save blob and legacy individual fields.
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

    /** Application context to avoid leaking Activities. */
    private final Context context;
    /** Gson instance for serialization. */
    private final Gson gson = new Gson();
    
    /** Hook for unit tests to monitor save events. */
    private static volatile SaveListener testSaveListener;
    private static final String TAG = "SaveManager";

    public SaveManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public enum LoadStatus {
        OK,
        MISSING,
        CORRUPT,
        SCHEMA_MISMATCH
    }

    public static class LoadOutcome {
        public final LoadStatus status;
        public final GameState gameState;
        public final int schemaVersion;

        LoadOutcome(LoadStatus status, GameState gameState, int schemaVersion) {
            this.status = status;
            this.gameState = gameState;
            this.schemaVersion = schemaVersion;
        }
    }

    /**
     * Saves a complete game session immediately to the specified slot.
     */
    public void saveGame(int slotIndex,
                         CharacterProfile profile,
                         int currentFloor,
                         int currentGold,
                         int currentPlatinum,
                         Tile[][] dungeonGrid,
                         RunMetadata metadata) {
        validateSlot(slotIndex);
        SaveBlob blob = new SaveBlob(profile, currentFloor, currentGold, currentPlatinum, dungeonGrid, metadata);
        boolean ok = PersistedBlobStore.save(context, slotPrefsName(slotIndex), KEY_SAVE_BLOB,
                SAVE_SCHEMA_VERSION, gson.toJson(blob));
        if (ok) {
            clearLegacyKeys(slotPrefs(slotIndex));
            notifyTestSave(slotIndex);
        } else {
            Log.e(TAG, "Save failed for slot " + slotIndex);
        }
    }

    /**
     * Builds an immutable snapshot copy of the current state.
     */
    public SaveSnapshot buildSnapshot(CharacterProfile profile,
                                      int currentFloor,
                                      int currentGold,
                                      int currentPlatinum,
                                      Tile[][] dungeonGrid,
                                      RunMetadata metadata) {
        CharacterProfile profileCopy = copyProfile(profile);
        Tile[][] gridCopy = copyGrid(dungeonGrid);
        return SaveSnapshot.from(profileCopy, currentFloor, currentGold, currentPlatinum, gridCopy, metadata);
    }

    /**
     * Persists a pre-built snapshot to the specified slot.
     */
    public void saveSnapshot(int slotIndex, SaveSnapshot snapshot) {
        if (snapshot == null) return;
        validateSlot(slotIndex);
        boolean ok = PersistedBlobStore.save(context, slotPrefsName(slotIndex), KEY_SAVE_BLOB,
                SAVE_SCHEMA_VERSION, gson.toJson(snapshot));
        if (ok) {
            clearLegacyKeys(slotPrefs(slotIndex));
            notifyTestSave(slotIndex);
        } else {
            Log.e(TAG, "Snapshot save failed for slot " + slotIndex);
        }
    }

    /**
     * Loads a game session from the specified slot, falling back to legacy data.
     */
    public GameState loadGame(int slotIndex) {
        LoadOutcome outcome = loadGameWithStatus(slotIndex);
        return outcome.status == LoadStatus.OK || outcome.status == LoadStatus.SCHEMA_MISMATCH
                ? outcome.gameState
                : null;
    }

    public LoadOutcome loadGameWithStatus(int slotIndex) {
        validateSlot(slotIndex);
        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(
                context, slotPrefsName(slotIndex), KEY_SAVE_BLOB, SAVE_SCHEMA_VERSION
        );

        if (result.status == PersistedBlobStore.LoadResult.Status.OK) {
            return new LoadOutcome(LoadStatus.OK, parseBlob(result.json), result.schemaVersion);
        }
        if (result.status == PersistedBlobStore.LoadResult.Status.SCHEMA_MISMATCH) {
            GameState parsed = parseBlob(result.json);
            return new LoadOutcome(LoadStatus.SCHEMA_MISMATCH, parsed, result.schemaVersion);
        }

        GameState legacy = loadLegacy(slotIndex);
        if (legacy != null) {
            saveGame(slotIndex, legacy.profile, legacy.currentFloor, legacy.currentGold,
                    legacy.currentPlatinum, legacy.dungeonGrid, legacy.metadata);
            return new LoadOutcome(LoadStatus.OK, legacy, SAVE_SCHEMA_VERSION);
        }

        LoadStatus status = result.status == PersistedBlobStore.LoadResult.Status.MISSING
                ? LoadStatus.MISSING
                : LoadStatus.CORRUPT;
        return new LoadOutcome(status, null, result.schemaVersion);
    }

    public boolean restoreBackup(int slotIndex) {
        validateSlot(slotIndex);
        PersistedBlobStore.LoadResult restored = PersistedBlobStore.restoreBackup(
                context, slotPrefsName(slotIndex), KEY_SAVE_BLOB, SAVE_SCHEMA_VERSION);
        return restored != null && restored.status == PersistedBlobStore.LoadResult.Status.OK;
    }

    public void migrateSave(int slotIndex, GameState gameState) {
        if (gameState == null) {
            return;
        }
        saveGame(slotIndex, gameState.profile, gameState.currentFloor, gameState.currentGold,
                gameState.currentPlatinum, gameState.dungeonGrid, gameState.metadata);
    }

    /**
     * Ensures legacy grids have a floor-specific big key label.
     */
    private void migrateGrid(Tile[][] grid, int floor) {
        if (grid == null) return;
        String bigKeyName = DungeonGenerator.getBigKeyNameForFloor(floor);
        for (Tile[] row : grid) {
            if (row == null) continue;
            for (Tile tile : row) {
                if (tile == null) continue;
                if (tile.getType() == TileType.BIG_KEY) {
                    if (tile.getCustomName() == null || tile.getCustomName().isEmpty()) {
                        tile.setCustomName(bigKeyName);
                    }
                }
            }
        }
    }

    /**
     * Returns true if the slot has any saved data.
     */
    public boolean isSlotOccupied(int slotIndex) {
        validateSlot(slotIndex);
        SharedPreferences prefs = slotPrefs(slotIndex);
        return prefs.contains(KEY_SAVE_BLOB) || prefs.contains(KEY_PROFILE);
    }

    /**
     * Deletes all data for the specified slot.
     */
    public void deleteSave(int slotIndex) {
        validateSlot(slotIndex);
        slotPrefs(slotIndex).edit().clear().apply();
    }

    /** Returns the SharedPreferences name for a slot. */
    private String slotPrefsName(int slotIndex) { return PREFS_NAME_PREFIX + slotIndex; }

    /** Returns the SharedPreferences handle for a slot. */
    private SharedPreferences slotPrefs(int slotIndex) {
        return SecurePreferences.get(context, slotPrefsName(slotIndex));
    }

    /** Validates the slot index range. */
    private void validateSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index: " + slotIndex);
        }
    }

    /**
     * Parses a serialized save blob into a GameState.
     */
    private GameState parseBlob(String json) {
        if (json == null) return null;
        try {
            SaveBlob blob = gson.fromJson(json, SaveBlob.class);
            if (blob == null || blob.profile == null || blob.dungeonGrid == null) return null;
            migrateGrid(blob.dungeonGrid, blob.currentFloor);
            return new GameState(blob.profile, blob.currentFloor, blob.currentGold,
                    blob.currentPlatinum, blob.dungeonGrid, blob.metadata);
        } catch (Exception ignored) { return null; }
    }

    /**
     * Loads legacy save keys and builds a GameState.
     */
    private GameState loadLegacy(int slotIndex) {
        SharedPreferences prefs = slotPrefs(slotIndex);
        if (!prefs.contains(KEY_PROFILE)) return null;
        try {
            String profileJson = prefs.getString(KEY_PROFILE, null);
            String gridJson = prefs.getString(KEY_GRID, null);
            if (profileJson == null || gridJson == null) return null;

            CharacterProfile profile = gson.fromJson(profileJson, CharacterProfile.class);
            Tile[][] dungeonGrid = gson.fromJson(gridJson, Tile[][].class);
            if (profile == null || dungeonGrid == null) return null;

            int floor = prefs.getInt(KEY_FLOOR, 1);
            migrateGrid(dungeonGrid, floor);
            RunMetadata metadata = new RunMetadata(
                    prefs.getInt(KEY_PLAYER_ROW, -1),
                    prefs.getInt(KEY_PLAYER_COL, -1),
                    prefs.getBoolean(KEY_SHIELD_ACTIVE, false),
                    prefs.getInt(KEY_SHIELD_STRENGTH, 0),
                    prefs.getInt(KEY_SHIELD_ROW, -1),
                    prefs.getInt(KEY_SHIELD_COL, -1),
                    null
            );
            return new GameState(profile, floor, prefs.getInt(KEY_GOLD, 0), prefs.getInt(KEY_PLATINUM, 0), dungeonGrid, metadata);
        } catch (Exception ignored) { return null; }
    }

    /**
     * Removes legacy per-field save keys after migrating.
     */
    private void clearLegacyKeys(SharedPreferences prefs) {
        prefs.edit()
                .remove(KEY_PROFILE).remove(KEY_FLOOR).remove(KEY_GRID)
                .remove(KEY_GOLD).remove(KEY_PLATINUM).remove(KEY_PLAYER_ROW)
                .remove(KEY_PLAYER_COL).remove(KEY_SHIELD_ACTIVE).remove(KEY_SHIELD_STRENGTH)
                .remove(KEY_SHIELD_ROW).remove(KEY_SHIELD_COL)
                .apply();
    }

    @VisibleForTesting
    public static void setTestSaveListener(SaveListener listener) { testSaveListener = listener; }
    private static void notifyTestSave(int slotIndex) {
        if (testSaveListener != null) testSaveListener.onSave(slotIndex);
    }

    /**
     * Immutable container for a fully loaded game session.
     */
    public static class GameState {
        public final CharacterProfile profile;
        public final int currentFloor;
        public final int currentGold;
        public final int currentPlatinum;
        public final Tile[][] dungeonGrid;
        public final RunMetadata metadata;

        public GameState(CharacterProfile profile, int currentFloor, int currentGold,
                         int currentPlatinum, Tile[][] dungeonGrid, RunMetadata metadata) {
            this.profile = profile; this.currentFloor = currentFloor; this.currentGold = currentGold;
            this.currentPlatinum = currentPlatinum; this.dungeonGrid = dungeonGrid; this.metadata = metadata;
        }
    }

    /**
     * Lightweight metadata for transient run state.
     */
    public static class RunMetadata {
        public final int playerRow, playerCol;
        public final boolean knightShieldActive;
        public final int knightShieldStrength, knightShieldRow, knightShieldCol;
        public final int nextAbilityAvailableFloor;
        public final String currentTerrain;

        public RunMetadata(int playerRow, int playerCol, boolean knightShieldActive,
                           int knightShieldStrength, int knightShieldRow, int knightShieldCol,
                           String currentTerrain) {
            this(playerRow, playerCol, knightShieldActive, knightShieldStrength,
                    knightShieldRow, knightShieldCol, 1, currentTerrain);
        }

        public RunMetadata(int playerRow, int playerCol, boolean knightShieldActive,
                           int knightShieldStrength, int knightShieldRow, int knightShieldCol,
                           int nextAbilityAvailableFloor, String currentTerrain) {
            this.playerRow = playerRow; this.playerCol = playerCol; this.knightShieldActive = knightShieldActive;
            this.knightShieldStrength = knightShieldStrength; this.knightShieldRow = knightShieldRow;
            this.knightShieldCol = knightShieldCol;
            this.nextAbilityAvailableFloor = nextAbilityAvailableFloor;
            this.currentTerrain = currentTerrain;
        }
    }

    /**
     * Snapshot container used for background persistence.
     */
    public static class SaveSnapshot {
        final CharacterProfile profile;
        final int currentFloor, currentGold, currentPlatinum;
        final Tile[][] dungeonGrid;
        final RunMetadata metadata;

        private SaveSnapshot(CharacterProfile p, int floor, int gold, int plat, Tile[][] grid, RunMetadata m) {
            this.profile = p; this.currentFloor = floor; this.currentGold = gold;
            this.currentPlatinum = plat; this.dungeonGrid = grid; this.metadata = m;
        }

        static SaveSnapshot from(CharacterProfile p, int f, int g, int pt, Tile[][] grid, RunMetadata m) {
            return new SaveSnapshot(p, f, g, pt, grid, m);
        }
    }

    /** Test hook invoked when a save completes. */
    public interface SaveListener { void onSave(int slotIndex); }

    /**
     * Creates a deep copy of the CharacterProfile for background saves.
     */
    private CharacterProfile copyProfile(CharacterProfile profile) {
        if (profile == null) {
            return null;
        }
        return gson.fromJson(gson.toJson(profile), CharacterProfile.class);
    }

    /**
     * Creates a deep copy of the dungeon grid for background saves.
     */
    private Tile[][] copyGrid(Tile[][] grid) {
        if (grid == null) {
            return null;
        }
        Tile[][] copy = new Tile[grid.length][];
        for (int r = 0; r < grid.length; r++) {
            Tile[] row = grid[r];
            if (row == null) {
                copy[r] = null;
                continue;
            }
            copy[r] = new Tile[row.length];
            for (int c = 0; c < row.length; c++) {
                copy[r][c] = copyTile(row[c]);
            }
        }
        return copy;
    }

    /**
     * Copies a tile, preserving its cached render metadata.
     */
    private Tile copyTile(Tile tile) {
        if (tile == null) {
            return null;
        }
        TileType type = tile.getType();
        Tile copy = tile.getCustomName() != null
                ? new Tile(type, tile.getCustomName())
                : new Tile(type);
        if (tile.isRevealed()) {
            copy.reveal();
        }
        copy.setHasPlayer(tile.hasPlayer());
        copy.setMonsterSpriteKey(tile.getMonsterSpriteKey());
        copy.setCachedMonsterHp(tile.getCachedMonsterHp());
        copy.setCachedMonsterMaxHp(tile.getCachedMonsterMaxHp());
        if (tile.hasMonster()) {
            copy.setMonster(copyMonster(tile.getMonster()));
        }
        return copy;
    }

    /**
     * Copies a monster and syncs its current HP.
     */
    private Monster copyMonster(Monster monster) {
        if (monster == null) {
            return null;
        }
        Monster copy = new Monster(monster.getMonsterType(),
                monster.getMaxHP(),
                monster.getAttack(),
                monster.getDefense(),
                monster.getImage(),
                monster.getFamily(),
                monster.getAffinity());
        copy.setHasRangedAttack(monster.hasRangedAttack());
        copy.setBoss(monster.isBoss());
        copy.setBossPhaseCount(monster.getBossPhaseCount());
        int missingHp = Math.max(0, monster.getMaxHP() - monster.getCurrentHP());
        if (missingHp > 0) {
            copy.takeDamage(missingHp);
        }
        return copy;
    }

    /**
     * Serialized save payload used for persistence.
     */
    private static class SaveBlob {
        final CharacterProfile profile;
        final int currentFloor, currentGold, currentPlatinum;
        final Tile[][] dungeonGrid;
        final RunMetadata metadata;

        SaveBlob(CharacterProfile p, int f, int g, int pt, Tile[][] grid, RunMetadata m) {
            this.profile = p; this.currentFloor = f; this.currentGold = g;
            this.currentPlatinum = pt; this.dungeonGrid = grid; this.metadata = m;
        }
    }
}
