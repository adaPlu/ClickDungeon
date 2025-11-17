package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.util.SaveManager;
import com.example.clickdungeon.util.SaveManager.GameState;

public class ContinueActivity extends AppCompatActivity {

    public static final String EXTRA_FORCE_NEW_GAME =
            "com.example.clickdungeon.extra.FORCE_NEW_GAME";

    private SaveManager saveManager;
    private boolean forceNewGameMode = false;

    private TextView slot1Info, slot2Info, slot3Info, slot4Info;
    private Button slot1Button, slot2Button, slot3Button, slot4Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue);

        saveManager = new SaveManager(this);
        forceNewGameMode = getIntent().getBooleanExtra(EXTRA_FORCE_NEW_GAME, false);

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
        refreshSlotsUI();
    }

    private void updateSlotUI(int slotIndex, TextView infoView, Button actionButton) {
        boolean isOccupied = saveManager.isSlotOccupied(slotIndex);
        GameState gameState = saveManager.loadGame(slotIndex);

        if (gameState == null) {
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

    private void showOverwriteDialog(int slotIndex) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.continue_slot_overwrite_title)
                .setMessage(R.string.continue_slot_overwrite_message)
                .setPositiveButton(R.string.continue_slot_overwrite_confirm, (dialog, which) -> startNewGame(slotIndex))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void startNewGame(int slotIndex) {
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, slotIndex);
        startActivity(intent);
    }

    private void continueGame(int slotIndex) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, slotIndex);
        startActivity(intent);
    }

    private void refreshSlotsUI() {
        updateSlotUI(0, slot1Info, slot1Button);
        updateSlotUI(1, slot2Info, slot2Button);
        updateSlotUI(2, slot3Info, slot3Button);
        updateSlotUI(3, slot4Info, slot4Button);
    }
}
