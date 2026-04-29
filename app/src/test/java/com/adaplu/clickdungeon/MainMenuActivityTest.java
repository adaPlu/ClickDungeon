package com.adaplu.clickdungeon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.adaplu.clickdungeon.util.SaveManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLooper;
import com.adaplu.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 4; i++) {
            SecurePreferences.get(context, "SaveSlot" + i)
                    .edit()
                    .clear()
                    .commit();
        }
        SecurePreferences.get(context, "player_profile")
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void continueDisabledWhenNoSaves() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        View continueButton = activity.findViewById(R.id.btnContinue);
        assertFalse(continueButton.isEnabled());
    }

    @Test
    public void continueEnabledWhenSaveExists() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][] { { new Tile(TileType.EMPTY) } };
        saveManager.saveGame(0, profile, 1, 0, 0, grid, null);

        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        View continueButton = activity.findViewById(R.id.btnContinue);
        assertTrue(continueButton.isEnabled());
    }

    @Test
    public void classesButtonShowsUpgradeDetailsForChosenClass() {
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.RANGER);
        profile.addClassXp(PlayerClass.RANGER, 56);
        SecurePreferences.get(context, "player_profile")
                .edit()
                .putString("profile", new com.google.gson.Gson().toJson(profile))
                .commit();

        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();
        activity.findViewById(R.id.btnClasses).performClick();

        android.app.Dialog picker = ShadowDialog.getLatestDialog();
        assertNotNull(picker);
        ListView classList = findListView(picker);
        assertNotNull(classList);
        classList.performItemClick(
                classList.getAdapter().getView(1, null, classList),
                1,
                classList.getAdapter().getItemId(1));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog detailDialog = ShadowDialog.getLatestDialog();
        assertNotNull(detailDialog);
        assertNotNull(findListView(detailDialog));

        String detail = activity.buildClassUpgradeDetailMessage(PlayerClass.RANGER, profile);
        assertTrue(detail.contains("Current XP: 56"));
        assertTrue(detail.contains("Tap an affordable ability"));
        assertTrue(detail.contains("Affordable - tap to unlock"));
    }

    @Test
    public void classesButtonUnlocksAffordableAbility() {
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.RANGER);
        profile.addClassXp(PlayerClass.RANGER, 56);
        SecurePreferences.get(context, "player_profile")
                .edit()
                .putString("profile", new com.google.gson.Gson().toJson(profile))
                .commit();

        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        activity.findViewById(R.id.btnClasses).performClick();

        android.app.Dialog picker = ShadowDialog.getLatestDialog();
        assertNotNull(picker);
        ListView classList = findListView(picker);
        assertNotNull(classList);
        classList.performItemClick(
                classList.getAdapter().getView(1, null, classList),
                1,
                classList.getAdapter().getItemId(1));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog detailDialog = ShadowDialog.getLatestDialog();
        assertNotNull(detailDialog);
        ListView abilityList = findListView(detailDialog);
        assertNotNull(abilityList);
        abilityList.performItemClick(
                abilityList.getAdapter().getView(1, null, abilityList),
                1,
                abilityList.getAdapter().getItemId(1));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        String json = SecurePreferences.get(context, "player_profile").getString("profile", null);
        CharacterProfile updated = new com.google.gson.Gson().fromJson(json, CharacterProfile.class);
        assertTrue(updated.isAbilityUnlocked(PlayerClass.RANGER, PlayerClass.ABILITY_RANGER_RAPID_VOLLEY));
    }

    private ListView findListView(android.app.Dialog dialog) {
        if (dialog instanceof androidx.appcompat.app.AlertDialog) {
            return ((androidx.appcompat.app.AlertDialog) dialog).getListView();
        }
        return dialog.findViewById(android.R.id.list);
    }
}
