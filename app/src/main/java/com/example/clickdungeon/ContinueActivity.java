package com.example.clickdungeon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.clickdungeon.R;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.util.SaveManager;
import com.example.clickdungeon.util.SaveManager.GameState;

public class ContinueActivity extends AppCompatActivity {

    private SaveManager saveManager;

    private TextView slot1Info, slot2Info, slot3Info, slot4Info;
    private Button slot1Button, slot2Button, slot3Button, slot4Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue);

        // Initialize SaveManager
        saveManager = new SaveManager(this);

        // Bind views
        slot1Info = findViewById(R.id.slot1Info);
        slot1Button = findViewById(R.id.slot1Button);

        slot2Info = findViewById(R.id.slot2Info);
        slot2Button = findViewById(R.id.slot2Button);

        slot3Info = findViewById(R.id.slot3Info);
        slot3Button = findViewById(R.id.slot3Button);

        slot4Info = findViewById(R.id.slot4Info);
        slot4Button = findViewById(R.id.slot4Button);

        // Update UI for each slot
        updateSlotUI(0, slot1Info, slot1Button);
        updateSlotUI(1, slot2Info, slot2Button);
        updateSlotUI(2, slot3Info, slot3Button);
        updateSlotUI(3, slot4Info, slot4Button);
    }

    /**
     * Update the display and button behavior for a specific slot.
     */
    private void updateSlotUI(int slotIndex, TextView infoView, Button actionButton) {
        boolean isOccupied = saveManager.isSlotOccupied(slotIndex);
        GameState gameState = saveManager.loadGame(slotIndex);

        if (gameState == null) {
            // Could be empty or corrupted
            if (isOccupied) {
                // Occupied, but loading returned null => corrupted
                infoView.setText("Slot " + (slotIndex + 1) + ": Data Corrupted");
                actionButton.setText("New Game");
                actionButton.setOnClickListener(v -> {
                    // Attempt to start a new game, but slot is "occupied" => confirm overwrite
                    showOverwriteDialog(slotIndex);
                });
            } else {
                // Truly empty slot
                infoView.setText("Slot " + (slotIndex + 1) + " is empty");
                actionButton.setText("New Game");
                actionButton.setOnClickListener(v -> {
                    // No need to confirm; slot is empty
                    startNewGame(slotIndex);
                });
            }
        } else {
            // Valid data in this slot
            String name = gameState.profile.getName();
            String charClass = gameState.profile.getPlayerClass().name();
            int floor = gameState.currentFloor;

            // Display something like "Slot 1 - Hero (KNIGHT) - Floor 5"
            infoView.setText("Slot " + (slotIndex + 1)
                    + " - " + name + " (" + charClass + ") - Floor " + floor);

            // Button says "Continue"
            actionButton.setText("Continue");
            actionButton.setOnClickListener(v -> {
                // Continue the game from this slot
                continueGame(slotIndex);
            });
        }
    }

    /**
     * Pops up a dialog to confirm overwriting an already-occupied slot.
     */
    private void showOverwriteDialog(int slotIndex) {
        new AlertDialog.Builder(this)
                .setTitle("Overwrite Slot?")
                .setMessage("This slot already has a saved game (or corrupted data). Overwrite it?")
                .setPositiveButton("Yes", (dialog, which) -> startNewGame(slotIndex))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Creates a brand-new game in the specified slot.
     * Adjust this to fit your character creation flow.
     */
    private void startNewGame(int slotIndex) {
        // Create a new hero with a specified maxHP (e.g., 100)
        CharacterProfile newProfile = new CharacterProfile("HeroName", PlayerClass.KNIGHT);
        int startingFloor = 1;

        // Example: create an empty or initial dungeon grid
        Tile[][] initialGrid = createInitialDungeonGrid();

        // Save to the specified slot
        saveManager.saveGame(slotIndex, newProfile, startingFloor, initialGrid);

        // Show a confirmation message
        Toast.makeText(this, "New game created and saved!", Toast.LENGTH_SHORT).show();

        // Optionally, launch the main game activity:
        // startActivity(MainGameActivity.getIntent(this, slotIndex));

        // Refresh the UI so the slot now shows as occupied
        refreshSlotsUI();
    }

    /**
     * Continues a game from the specified slot.
     * Typically, you'd launch your main game screen with the slot index.
     */
    private void continueGame(int slotIndex) {
        // For example:
        // startActivity(MainGameActivity.getIntent(this, slotIndex));
        Toast.makeText(this, "Loading slot " + (slotIndex + 1), Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper to refresh the slot displays after a save or overwrite.
     */
    private void refreshSlotsUI() {
        updateSlotUI(0, slot1Info, slot1Button);
        updateSlotUI(1, slot2Info, slot2Button);
        updateSlotUI(2, slot3Info, slot3Button);
        updateSlotUI(3, slot4Info, slot4Button);
    }

    /**
     * Example method to build an initial dungeon grid.
     * Replace with your actual logic.
     */
    private Tile[][] createInitialDungeonGrid() {
        // Just an example 2D array
        Tile[][] grid = new Tile[5][5];
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                grid[row][col] = new Tile(/*...*/);
            }
        }
        return grid;
    }
}
