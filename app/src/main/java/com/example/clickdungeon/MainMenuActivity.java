package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.util.SaveManager;

/**
 * MainMenuActivity serves as the entry point for the player.
 * It provides navigation to start a new game, continue an existing one, view achievements, 
 * visit the shop, adjust settings, or manage inventory.
 */
public class MainMenuActivity extends AppCompatActivity {

    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Bind UI buttons from the layout.
        Button btnNewGame = findViewById(R.id.btnNewGame);
        btnContinue = findViewById(R.id.btnContinue);
        Button btnAchievements = findViewById(R.id.btnAchievements);
        Button btnShop = findViewById(R.id.btnShop);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnInventory = findViewById(R.id.btnInventory);

        // Initial check to see if the "Continue" button should be active.
        updateContinueState();

        // Navigate to the slot selection screen forced into "New Game" mode.
        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ContinueActivity.class);
            intent.putExtra(ContinueActivity.EXTRA_FORCE_NEW_GAME, true);
            startActivity(intent);
        });

        // Navigate to the slot selection screen to resume a previous run.
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ContinueActivity.class);
            startActivity(intent);
        });

        // Other utility and meta-game activities.
        btnAchievements.setOnClickListener(v ->
                startActivity(new Intent(this, AchievementsActivity.class)));

        btnShop.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        btnInventory.setOnClickListener(v ->
                startActivity(new Intent(this, InventoryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check for existing saves whenever the user returns to the menu.
        updateContinueState();
    }

    /**
     * Updates the visual state and enabled status of the Continue button based on save data.
     */
    private void updateContinueState() {
        boolean hasSave = hasAnySavedSlot();
        btnContinue.setEnabled(hasSave);
        btnContinue.setAlpha(hasSave ? 1f : 0.5f);
    }

    /**
     * Checks all available save slots to determine if any game data exists.
     * @return true if at least one slot is occupied.
     */
    private boolean hasAnySavedSlot() {
        SaveManager saveManager = new SaveManager(this);
        for (int i = 0; i < 4; i++) {
            if (saveManager.isSlotOccupied(i)) {
                return true;
            }
        }
        return false;
    }
}
