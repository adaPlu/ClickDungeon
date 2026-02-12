package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.ListView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.GameActivity;
import com.example.clickdungeon.R;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
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
    public void wizardAbilitySelectionConsumesMpAndTargets() throws Exception {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        int startingMp = profile.getCurrentMP();

        ActivityController<GameActivity> controller = launchWithProfile(profile);
        GameActivity activity = controller.setup().get();

        activity.findViewById(R.id.btnClassAbility).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        ListView listView = findListView(dialog);
        assertTrue(listView.getAdapter().getCount() > 0);

        listView.performItemClick(
                listView.getAdapter().getView(0, null, listView),
                0,
                listView.getAdapter().getItemId(0));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        CharacterProfile updated = (CharacterProfile) getField(activity, "profile");
        assertEquals(startingMp - 3, updated.getCurrentMP());
        Object mode = getField(activity, "pendingAbilityTargetMode");
        assertEquals("WIZARD_FIREBALL", mode.toString());
    }

    @Test
    public void wizardAbilityBlocksWhenOutOfMp() throws Exception {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        profile.setCurrentMP(0);

        ActivityController<GameActivity> controller = launchWithProfile(profile);
        GameActivity activity = controller.setup().get();

        activity.findViewById(R.id.btnClassAbility).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        ListView listView = findListView(dialog);
        listView.performItemClick(
                listView.getAdapter().getView(0, null, listView),
                0,
                listView.getAdapter().getItemId(0));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        CharacterProfile updated = (CharacterProfile) getField(activity, "profile");
        assertEquals(0, updated.getCurrentMP());
        Object mode = getField(activity, "pendingAbilityTargetMode");
        assertEquals("NONE", mode.toString());
    }

    private ActivityController<GameActivity> launchWithProfile(CharacterProfile profile) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, new Gson().toJson(profile));
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        return Robolectric.buildActivity(GameActivity.class, intent);
    }

    private Object getField(GameActivity activity, String name) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
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
