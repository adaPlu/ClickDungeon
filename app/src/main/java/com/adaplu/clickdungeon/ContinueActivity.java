package com.adaplu.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adaplu.clickdungeon.util.SaveManager;
import com.adaplu.clickdungeon.util.SaveManager.GameState;

/**
 * ContinueActivity handles the save slot management UI.
 * It allows players to select a slot to resume an existing game, overwrite a slot with a new game,
 * or identifies corrupted slots.
 */
public class ContinueActivity extends AppCompatActivity {

    /** Extra flag to determine if the activity should force starting a new game in a slot. */
    public static final String EXTRA_FORCE_NEW_GAME =
            "com.adaplu.clickdungeon.extra.FORCE_NEW_GAME";

    private SaveManager saveManager;
    private boolean forceNewGameMode = false;

    // View references for the four save slots.
    private TextView slot1Info, slot2Info, slot3Info, slot4Info;
    private Button slot1Button, slot2Button, slot3Button, slot4Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue);

        saveManager = new SaveManager(this);
        forceNewGameMode = getIntent().getBooleanExtra(EXTRA_FORCE_NEW_GAME, false);

        // Bind UI components.
        slot1Info = findViewById(R.id.slot1Info);
        slot1Button = findViewById(R.id.slot1Button);

        slot2Info = findViewById(R.id.slot2Info);
        slot2Button = findViewById(R.id.slot2Button);

        slot3Info = findViewById(R.id.slot3Info);
        slot3Button = findViewById(R.id.slot3Button);

        slot4Info = findViewById(R.id.slot4Info);
        slot4Button = findViewById(R.id.slot4Button);

        refreshSlotsUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh slot metadata whenever the activity returns to focus.
        refreshSlotsUI();
    }

    /**
     * Updates the UI representation of a single save slot.
     */
    private void updateSlotUI(int slotIndex, TextView infoView, Button actionButton) {
        boolean isOccupied = saveManager.isSlotOccupied(slotIndex);
        SaveManager.LoadOutcome outcome = saveManager.loadGameWithStatus(slotIndex);
        GameState gameState = outcome.gameState;

        if (outcome.status == SaveManager.LoadStatus.SCHEMA_MISMATCH) {
            infoView.setText(getString(R.string.continue_slot_schema_mismatch, slotIndex + 1));
            actionButton.setText(R.string.continue_slot_review);
            actionButton.setOnClickListener(v -> showSchemaMismatchDialog(slotIndex, outcome));
            return;
        }

        if (gameState == null) {
            // Handle empty or corrupted slots.
            if (isOccupied) {
                infoView.setText(getString(R.string.continue_slot_corrupted, slotIndex + 1));
                actionButton.setText(R.string.continue_slot_overwrite);
                actionButton.setOnClickListener(v -> showOverwriteDialog(slotIndex));
            } else {
                infoView.setText(getString(R.string.continue_slot_empty, slotIndex + 1));
                actionButton.setText(R.string.continue_slot_new_game);
                actionButton.setOnClickListener(v -> startNewGame(slotIndex));
            }
            return;
        }

        // Display summary of the saved character.
        String name = gameState.profile.getName();
        String charClass = gameState.profile.getPlayerClass().name();
        int floor = gameState.currentFloor;

        infoView.setText(getString(R.string.continue_slot_occupied, slotIndex + 1, name, charClass, floor));
        if (forceNewGameMode) {
            actionButton.setText(R.string.continue_slot_overwrite);
            actionButton.setOnClickListener(v -> showOverwriteDialog(slotIndex));
        } else {
            actionButton.setText(R.string.continue_slot_continue);
            actionButton.setOnClickListener(v -> continueGame(slotIndex));
        }
    }

    private void showSchemaMismatchDialog(int slotIndex, SaveManager.LoadOutcome outcome) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.continue_slot_schema_mismatch_title)
                .setMessage(R.string.continue_slot_schema_mismatch_message)
                .setPositiveButton(R.string.continue_slot_schema_migrate, (dialog, which) -> {
                    if (outcome.gameState != null) {
                        saveManager.migrateSave(slotIndex, outcome.gameState);
                        continueGame(slotIndex);
                    } else {
                        startNewGame(slotIndex);
                    }
                })
                .setNeutralButton(R.string.continue_slot_schema_restore, (dialog, which) -> {
                    boolean restored = saveManager.restoreBackup(slotIndex);
                    if (restored) {
                        refreshSlotsUI();
                    } else {
                        infoViewForSlot(slotIndex).setText(
                                getString(R.string.continue_slot_corrupted, slotIndex + 1));
                    }
                })
                .setNegativeButton(R.string.continue_slot_schema_reset, (dialog, which) -> {
                    saveManager.deleteSave(slotIndex);
                    startNewGame(slotIndex);
                })
                .show();
    }

    private TextView infoViewForSlot(int slotIndex) {
        switch (slotIndex) {
            case 0:
                return slot1Info;
            case 1:
                return slot2Info;
            case 2:
                return slot3Info;
            case 3:
            default:
                return slot4Info;
        }
    }

    /**
     * Warns the user before deleting an existing save slot.
     */
    private void showOverwriteDialog(int slotIndex) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.continue_slot_overwrite_title)
                .setMessage(R.string.continue_slot_overwrite_message)
                .setPositiveButton(R.string.continue_slot_overwrite_confirm, (dialog, which) -> startNewGame(slotIndex))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Navigates to the character creation screen for the specified slot.
     */
    private void startNewGame(int slotIndex) {
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, slotIndex);
        startActivity(intent);
    }

    /**
     * Resumes the game session from the provided save slot data.
     */
    private void continueGame(int slotIndex) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, slotIndex);
        startActivity(intent);
    }

    /**
     * Triggers a UI refresh for all slots.
     */
    private void refreshSlotsUI() {
        updateSlotUI(0, slot1Info, slot1Button);
        updateSlotUI(1, slot2Info, slot2Button);
        updateSlotUI(2, slot3Info, slot3Button);
        updateSlotUI(3, slot4Info, slot4Button);
    }
}
