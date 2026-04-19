package com.adaplu.clickdungeon.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

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
import com.adaplu.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class GameActivityStatAllocationDialogTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "SaveSlot0").edit().clear().commit();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE).edit().clear().commit();
        ShadowAlertDialog.reset();
    }

    @Test
    public void inventoryDialogShowsSimplifiedStatSummary() throws Exception {
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.KNIGHT);
        profile.addExperience(120);
        profile.addDefenseBoost(2);
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(profile))
                .commit();

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, new Gson().toJson(profile));
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        GameActivity activity = controller.setup().get();

        java.lang.reflect.Method method = GameActivity.class.getDeclaredMethod("showInventoryDialog");
        method.setAccessible(true);
        method.invoke(activity);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        android.app.Dialog dialog = ShadowDialog.getLatestDialog();
        assertNotNull(dialog);

        TextView classXpView = dialog.findViewById(R.id.textCurrentClassXp);
        TextView defenseView = dialog.findViewById(R.id.textStatDefense);
        assertNotNull(classXpView);
        assertNotNull(defenseView);
        assertTrue(classXpView.getText().toString().contains("120"));
        assertTrue(defenseView.getText().toString().contains("DEF:"));
    }
}
