package com.adaplu.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adaplu.clickdungeon.util.SaveManager;

/**
 * MainMenuActivity serves as the entry point for the player.
 * It provides navigation to start a new game, continue an existing one, view achievements, 
 * visit the shop, adjust settings, or manage inventory.
 */
public class MainMenuActivity extends AppCompatActivity {

    private FrameLayout btnContinue;
    private TextView btnContinueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Bind UI frames (compound controls) from the layout.
        FrameLayout btnNewGame = findViewById(R.id.btnNewGame);
        btnContinue = findViewById(R.id.btnContinue);
        FrameLayout btnAchievements = findViewById(R.id.btnAchievements);
        FrameLayout btnShop = findViewById(R.id.btnShop);
        FrameLayout btnSettings = findViewById(R.id.btnSettings);
        FrameLayout btnInventory = findViewById(R.id.btnInventory);

        // Labels inside compound controls
        TextView btnNewGameText = btnNewGame.findViewById(R.id.btnNewGame_text);
        btnContinueText = btnContinue.findViewById(R.id.btnContinue_text);
        TextView btnAchievementsText = btnAchievements.findViewById(R.id.btnAchievements_text);
        TextView btnShopText = btnShop.findViewById(R.id.btnShop_text);
        TextView btnSettingsText = btnSettings.findViewById(R.id.btnSettings_text);
        TextView btnInventoryText = btnInventory.findViewById(R.id.btnInventory_text);

        // Accessibility: set the container's contentDescription from the visible label
        if (btnNewGameText != null) {
            btnNewGame.setContentDescription(btnNewGameText.getText());
            btnNewGame.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnNewGameText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (btnContinueText != null) {
            btnContinue.setContentDescription(btnContinueText.getText());
            btnContinue.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnContinueText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (btnAchievementsText != null) {
            btnAchievements.setContentDescription(btnAchievementsText.getText());
            btnAchievements.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnAchievementsText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (btnShopText != null) {
            btnShop.setContentDescription(btnShopText.getText());
            btnShop.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnShopText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (btnSettingsText != null) {
            btnSettings.setContentDescription(btnSettingsText.getText());
            btnSettings.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnSettingsText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (btnInventoryText != null) {
            btnInventory.setContentDescription(btnInventoryText.getText());
            btnInventory.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnInventoryText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

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
        if (btnContinueText != null) {
            btnContinueText.setText(hasSave ? R.string.btn_continue : R.string.btn_continue_no_save);
        }
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
