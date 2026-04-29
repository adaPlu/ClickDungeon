package com.adaplu.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.GameActivity;
import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
public class GameActivityAbilityChooserTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("SaveSlot0", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE).edit().clear().commit();
        ShadowAlertDialog.reset();
    }

    @Test
    public void wizardAbilitySelectionShowsUnlockedAbilitiesAndConsumesAChargeOnResolve() throws Exception {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        profile.addClassXp(PlayerClass.WIZARD, 200);
        int startingCharges = profile.getAbilityCharges(
                PlayerClass.WIZARD,
                PlayerClass.ABILITY_WIZARD_FIREBALL,
                System.currentTimeMillis());

        ActivityController<GameActivity> controller = launchWithProfile(profile);
        GameActivity activity = controller.setup().get();
        setField(activity, "dungeonGrid", buildEmptyGrid());
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        invoke(activity, "renderGrid");

        activity.findViewById(R.id.btnClassAbility).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        ListView listView = findListView(dialog);
        assertEquals(profile.getUnlockedAbilities(PlayerClass.WIZARD).length, listView.getAdapter().getCount());

        listView.performItemClick(
                listView.getAdapter().getView(0, null, listView),
                0,
                listView.getAdapter().getItemId(0));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals("WIZARD_FIREBALL", getField(activity, "pendingAbilityTargetMode").toString());
        CharacterProfile updated = (CharacterProfile) getField(activity, "profile");
        assertEquals(startingCharges, updated.getAbilityCharges(
                PlayerClass.WIZARD,
                PlayerClass.ABILITY_WIZARD_FIREBALL,
                System.currentTimeMillis()));

        invoke(activity, "handleAbilityTargetSelection", 2, 3);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals("NONE", getField(activity, "pendingAbilityTargetMode").toString());
        assertEquals(startingCharges - 1, updated.getAbilityCharges(
                PlayerClass.WIZARD,
                PlayerClass.ABILITY_WIZARD_FIREBALL,
                System.currentTimeMillis()));
    }

    @Test
    public void wizardAbilityBlocksWhenOutOfCharges() throws Exception {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        profile.addClassXp(PlayerClass.WIZARD, 200);
        ActivityController<GameActivity> controller = launchWithProfile(profile);
        GameActivity activity = controller.setup().get();
        CharacterProfile activityProfile = (CharacterProfile) getField(activity, "profile");
        long now = System.currentTimeMillis();
        activityProfile.consumeAbilityCharge(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_FIREBALL, now);
        activityProfile.consumeAbilityCharge(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_FIREBALL, now);
        activityProfile.consumeAbilityCharge(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_FIREBALL, now);

        invoke(activity, "showAbilityChooser");
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        ListView listView = findListView(dialog);
        listView.performItemClick(
                listView.getAdapter().getView(0, null, listView),
                0,
                listView.getAdapter().getItemId(0));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals("NONE", getField(activity, "pendingAbilityTargetMode").toString());
    }

    private ActivityController<GameActivity> launchWithProfile(CharacterProfile profile) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, new Gson().toJson(profile));
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        return Robolectric.buildActivity(GameActivity.class, intent);
    }

    private void setField(GameActivity activity, String name, Object value) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Object getField(GameActivity activity, String name) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
    }

    private Object invoke(GameActivity activity, String methodName, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                types[i] = int.class;
            } else {
                types[i] = args[i].getClass();
            }
        }
        java.lang.reflect.Method method = GameActivity.class.getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        return method.invoke(activity, args);
    }

    private com.adaplu.clickdungeon.model.Tile[][] buildEmptyGrid() {
        com.adaplu.clickdungeon.model.Tile[][] grid = new com.adaplu.clickdungeon.model.Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new com.adaplu.clickdungeon.model.Tile(com.adaplu.clickdungeon.model.TileType.EMPTY);
            }
        }
        return grid;
    }

    private ListView findListView(android.app.Dialog dialog) {
        android.app.AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        if (alertDialog != null && alertDialog.getListView() != null) {
            return alertDialog.getListView();
        }
        if (dialog instanceof androidx.appcompat.app.AlertDialog) {
            return ((androidx.appcompat.app.AlertDialog) dialog).getListView();
        }
        ListView listView = dialog.findViewById(android.R.id.list);
        return listView;
    }
}
