/*package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Tile;
import com.google.gson.Gson;

/**
 * Manages saving and loading game data across multiple save slots using SharedPreferences.
 */
/*public class SaveManager {

    private static final String PREFS_NAME_PREFIX = "SaveSlot";
    private static final int TOTAL_SLOTS = 4;
    private static final String KEY_PROFILE = "CharacterProfile";
    private static final String KEY_FLOOR = "CurrentFloor";
    private static final String KEY_GRID = "DungeonGrid";

    private final Context context;
    private final Gson gson;

    /**
     * Constructor for SaveManager.
     *
     * @param context The application context.

    public SaveManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    /**
     * Saves the game state to the specified slot.
     *
     * @param slotIndex    The index of the save slot (0 to 3).
     * @param profile      The character profile to save.
     * @param currentFloor The current floor level.
     * @param dungeonGrid  The current state of the dungeon grid.

    public void saveGame(int slotIndex,
                         CharacterProfile profile,
                         int currentFloor,
                         Tile[][] dungeonGrid) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index");
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME_PREFIX + slotIndex, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert objects to JSON
        String profileJson = gson.toJson(profile);
        String gridJson = gson.toJson(dungeonGrid);

        // Store them
        editor.putString(KEY_PROFILE, profileJson);
        editor.putInt(KEY_FLOOR, currentFloor);
        editor.putString(KEY_GRID, gridJson);

        editor.apply();
    }

    /**
     * Loads the game state from the specified slot.
     *
     * @param slotIndex The index of the save slot (0 to 3).
     * @return A GameState object containing the loaded data, or null if the slot is empty or data is invalid.

    public GameState loadGame(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index");
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME_PREFIX + slotIndex, Context.MODE_PRIVATE);

        // If we don't even have a profile key, consider it empty
        if (!prefs.contains(KEY_PROFILE)) {
            return null;
        }

        try {
            // Retrieve JSON strings and parse them
            String profileJson = prefs.getString(KEY_PROFILE, null);
            String gridJson = prefs.getString(KEY_GRID, null);

            // If these are missing, treat it as corruption
            if (profileJson == null || gridJson == null) {
                return null;
            }

            CharacterProfile profile = gson.fromJson(profileJson, CharacterProfile.class);
            int currentFloor = prefs.getInt(KEY_FLOOR, 1);
            Tile[][] dungeonGrid = gson.fromJson(gridJson, Tile[][].class);

            // If parsing fails or objects are null, treat as corruption
            if (profile == null || dungeonGrid == null) {
                return null;
            }

            return new GameState(profile, currentFloor, dungeonGrid);

        } catch (Exception e) {
            // Catch any JSON parsing errors or other exceptions
            return null;
        }
    }

    /**
     * Checks if a save slot is occupied.
     *
     * @param slotIndex The index of the save slot (0 to 3).
     * @return True if the slot is occupied, false otherwise.

    public boolean isSlotOccupied(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index");
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME_PREFIX + slotIndex, Context.MODE_PRIVATE);

        // We consider the slot "occupied" if there's a CharacterProfile key
        return prefs.contains(KEY_PROFILE);
    }

    /**
     * Deletes the save data in the specified slot.
     *
     * @param slotIndex The index of the save slot (0 to 3).

    public void deleteSave(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= TOTAL_SLOTS) {
            throw new IllegalArgumentException("Invalid slot index");
        }

        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME_PREFIX + slotIndex, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * A simple data class to hold the game state.

    public static class GameState {
        public CharacterProfile profile;
        public int currentFloor;
        public Tile[][] dungeonGrid;

        public GameState(CharacterProfile profile,
                         int currentFloor,
                         Tile[][] dungeonGrid) {
            this.profile = profile;
            this.currentFloor = currentFloor;
            this.dungeonGrid = dungeonGrid;
        }
    }
}
*/