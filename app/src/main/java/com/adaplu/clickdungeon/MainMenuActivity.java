package com.adaplu.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.util.SaveManager;
import com.adaplu.clickdungeon.util.SecurePreferences;
import com.google.gson.Gson;

/**
 * MainMenuActivity serves as the entry point for the player.
 * It provides navigation to start a new game, continue an existing one, view achievements, 
 * visit the shop, adjust settings, or manage inventory.
 */
public class MainMenuActivity extends AppCompatActivity {

    private static final String PROFILE_PREFS = "player_profile";
    private static final String PROFILE_KEY = "profile";

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
        FrameLayout btnClasses = findViewById(R.id.btnClasses);
        FrameLayout btnInventory = findViewById(R.id.btnInventory);

        // Labels inside compound controls
        TextView btnNewGameText = btnNewGame.findViewById(R.id.btnNewGame_text);
        btnContinueText = btnContinue.findViewById(R.id.btnContinue_text);
        TextView btnAchievementsText = btnAchievements.findViewById(R.id.btnAchievements_text);
        TextView btnShopText = btnShop.findViewById(R.id.btnShop_text);
        TextView btnSettingsText = btnSettings.findViewById(R.id.btnSettings_text);
        TextView btnClassesText = btnClasses.findViewById(R.id.btnClasses_text);
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
        if (btnClassesText != null) {
            btnClasses.setContentDescription(btnClassesText.getText());
            btnClasses.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            btnClassesText.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);
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

        btnClasses.setOnClickListener(v -> showClassUpgradeMenu());

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

    private void showClassUpgradeMenu() {
        PlayerClass[] classes = PlayerClass.values();
        CharacterProfile profile = loadProfile();
        String[] labels = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            int xp = profile != null ? profile.getClassXp(classes[i]) : 0;
            labels[i] = getString(R.string.class_upgrade_picker_entry, formatClassName(classes[i]), xp);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.class_upgrade_picker_title)
                .setItems(labels, (dialog, which) -> showClassUpgradeDetail(classes[which], profile))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showClassUpgradeDetail(PlayerClass playerClass, CharacterProfile profile) {
        if (playerClass == null) {
            return;
        }
        PlayerClass.AbilityDefinition[] abilities = playerClass.getAbilityProgression();
        String[] labels = new String[abilities.length];
        for (int i = 0; i < abilities.length; i++) {
            labels[i] = buildClassUpgradeAbilityLabel(playerClass, profile, abilities[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.class_upgrade_detail_title, formatClassName(playerClass)))
                .setMessage(buildClassUpgradeHeaderMessage(playerClass, profile))
                .setItems(labels, (dialog, which) -> handleClassUpgradeSelection(playerClass, profile, abilities[which]))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String buildClassUpgradeHeaderMessage(PlayerClass playerClass, CharacterProfile profile) {
        int currentXp = profile != null ? profile.getClassXp(playerClass) : 0;
        StringBuilder detail = new StringBuilder();
        detail.append(getString(R.string.class_upgrade_current_xp, currentXp));
        if (profile == null) {
            detail.append("\n").append(getString(R.string.class_upgrade_profile_required));
        } else {
            detail.append("\n").append(getString(R.string.class_upgrade_detail_note));
        }
        return detail.toString();
    }

    String buildClassUpgradeDetailMessage(PlayerClass playerClass, CharacterProfile profile) {
        int currentXp = profile != null ? profile.getClassXp(playerClass) : 0;
        StringBuilder detail = new StringBuilder();
        detail.append(getString(R.string.class_upgrade_current_xp, currentXp));
        if (profile == null) {
            detail.append("\n").append(getString(R.string.class_upgrade_profile_required));
        } else {
            detail.append("\n").append(getString(R.string.class_upgrade_detail_note));
        }
        for (PlayerClass.AbilityDefinition ability : playerClass.getAbilityProgression()) {
            detail.append("\n").append(buildClassUpgradeAbilityLabel(playerClass, profile, ability));
        }
        return detail.toString();
    }

    String buildClassUpgradeAbilityLabel(PlayerClass playerClass,
                                         CharacterProfile profile,
                                         PlayerClass.AbilityDefinition ability) {
        if (ability == null) {
            return "";
        }
        int currentXp = profile != null ? profile.getClassXp(playerClass) : 0;
        String state;
        if (profile == null) {
            state = getString(R.string.class_upgrade_profile_required);
        } else if (profile.isAbilityUnlocked(playerClass, ability.getName())) {
            int charges = profile.getAbilityCharges(playerClass, ability.getName(), System.currentTimeMillis());
            state = getString(R.string.class_upgrade_state_unlocked, charges);
        } else if (currentXp >= ability.getUnlockXpCost()) {
            state = getString(R.string.class_upgrade_state_affordable);
        } else {
            state = getString(R.string.class_upgrade_state_locked,
                    Math.max(0, ability.getUnlockXpCost() - currentXp));
        }
        return getString(
                R.string.class_upgrade_ability_entry,
                ability.getName(),
                ability.getUnlockXpCost(),
                state,
                ability.getDescription());
    }

    private void handleClassUpgradeSelection(PlayerClass playerClass,
                                             CharacterProfile profile,
                                             PlayerClass.AbilityDefinition ability) {
        if (playerClass == null || ability == null) {
            return;
        }
        if (profile == null) {
            Toast.makeText(this, R.string.class_upgrade_profile_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (profile.isAbilityUnlocked(playerClass, ability.getName())) {
            Toast.makeText(this, getString(R.string.class_upgrade_already_unlocked, ability.getName()), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!profile.unlockAbility(playerClass, ability.getName())) {
            Toast.makeText(this, getString(R.string.class_ability_unlock_failed, ability.getName()), Toast.LENGTH_SHORT).show();
            return;
        }
        persistProfile(profile);
        Toast.makeText(this, getString(R.string.class_ability_unlock_success, ability.getName()), Toast.LENGTH_SHORT).show();
        showClassUpgradeDetail(playerClass, profile);
    }

    private void persistProfile(CharacterProfile profile) {
        if (profile == null) {
            return;
        }
        SecurePreferences.get(this, PROFILE_PREFS)
                .edit()
                .putString(PROFILE_KEY, new Gson().toJson(profile))
                .apply();
    }

    private CharacterProfile loadProfile() {
        String json = SecurePreferences.get(this, PROFILE_PREFS).getString(PROFILE_KEY, null);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        return new Gson().fromJson(json, CharacterProfile.class);
    }

    private String formatClassName(PlayerClass playerClass) {
        if (playerClass == null) {
            return getString(R.string.player_name_fallback);
        }
        String raw = playerClass.name().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
