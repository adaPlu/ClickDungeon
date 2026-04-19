package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.util.InventoryManager;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowToast;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class GameActivityInventoryDialogTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        InventoryManager.clearInventory(context);
        InventoryManager.setGold(context, 0);
        InventoryManager.setPlatinum(context, 0);
    }

    @Test
    public void inventoryDialog_emptyStateVisible() {
        seedProfile(PlayerClass.KNIGHT);
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        InventoryManager.clearInventory(context);

        showInventoryDialog(activity);

        android.app.Dialog dialog = getLatestDialog();
        assertNotNull(dialog);
        TextView emptyView = dialog.findViewById(R.id.textEmptyInventory);
        assertNotNull(emptyView);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }

    @Test
    public void inventoryDialog_displaysCurrencyCounts() {
        seedProfile(PlayerClass.KNIGHT);
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        InventoryManager.setGold(context, 250);
        InventoryManager.setPlatinum(context, 120);

        showInventoryDialog(activity);

        android.app.Dialog dialog = getLatestDialog();
        TextView goldView = dialog.findViewById(R.id.textInventoryGold);
        TextView platinumView = dialog.findViewById(R.id.textInventoryPlatinum);
        assertNotNull(goldView);
        assertNotNull(platinumView);
        assertTrue(goldView.getText().toString().contains("250"));
        assertTrue(platinumView.getText().toString().contains("120"));
    }

    @Test
    public void inventoryDialog_equipUpdatesStatDeltaAndToast() {
        seedProfile(PlayerClass.KNIGHT);
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        InventoryManager.clearInventory(context);
        InventoryManager.adjustItemQuantity(context, "Iron Sword", 1);

        showInventoryDialog(activity);

        android.app.Dialog dialog = getLatestDialog();
        RecyclerView recycler = dialog.findViewById(R.id.recyclerInventory);
        TextView statView = dialog.findViewById(R.id.textStatDelta);
        assertNotNull(recycler);
        assertNotNull(statView);

        @SuppressWarnings("unchecked")
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter =
                (RecyclerView.Adapter<RecyclerView.ViewHolder>) recycler.getAdapter();
        assertNotNull(adapter);
        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(recycler, adapter.getItemViewType(0));
        adapter.onBindViewHolder(holder, 0);
        holder.itemView.performClick();

        CharacterProfile profile = ReflectionHelpers.getField(activity, "profile");
        String expected = activity.getString(R.string.equipped_stat_delta,
                profile.getBaseAttack(),
                profile.getAttackBonus(),
                profile.getBaseDefense(),
                profile.getDefenseBonus());
        assertEquals(expected, statView.getText().toString());

        String toastText = ShadowToast.getTextOfLatestToast();
        assertEquals(activity.getString(R.string.equip_success, "Iron Sword"), toastText);
    }

    @Test
    public void inventoryChangeLog_keepsNewestFirst() {
        seedProfile(PlayerClass.KNIGHT);
        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();

        ReflectionHelpers.callInstanceMethod(activity, "addInventoryChange",
                ReflectionHelpers.ClassParameter.from(String.class, "Potion"),
                ReflectionHelpers.ClassParameter.from(boolean.class, true));
        ReflectionHelpers.callInstanceMethod(activity, "addInventoryChange",
                ReflectionHelpers.ClassParameter.from(String.class, "Trap Disarm Kit"),
                ReflectionHelpers.ClassParameter.from(boolean.class, false));

        List<String> changeLog = ReflectionHelpers.getField(activity, "inventoryChangeLog");
        assertTrue(changeLog.get(0).contains("Trap Disarm Kit"));
        assertTrue(changeLog.get(1).contains("Potion"));
    }

    @Test
    public void inventoryDialog_showsSimplifiedStatSummary() {
        CharacterProfile profile = seedProfile(PlayerClass.WIZARD);
        profile.addExperience(120);
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(profile))
                .commit();

        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        showInventoryDialog(activity);

        android.app.Dialog dialog = getLatestDialog();
        TextView classXpView = dialog.findViewById(R.id.textCurrentClassXp);
        TextView healthView = dialog.findViewById(R.id.textStatHealth);
        TextView attackView = dialog.findViewById(R.id.textStatAttack);
        TextView defenseView = dialog.findViewById(R.id.textStatDefense);
        assertNotNull(classXpView);
        assertNotNull(healthView);
        assertNotNull(attackView);
        assertNotNull(defenseView);
        assertTrue(classXpView.getText().toString().contains("120"));
        assertTrue(healthView.getText().toString().contains("HP:"));
        assertTrue(attackView.getText().toString().contains("ATK:"));
        assertTrue(defenseView.getText().toString().contains("DEF:"));
    }

    @Test
    public void inventoryDialog_showsRecentChanges() {
        seedProfile(PlayerClass.KNIGHT);
        InventoryManager.recordInventoryChange(context, "Picked up Gold");

        GameActivity activity = Robolectric.buildActivity(GameActivity.class).setup().get();
        showInventoryDialog(activity);

        android.app.Dialog dialog = getLatestDialog();
        TextView changeLogView = dialog.findViewById(R.id.textInventoryChangeLog);
        assertNotNull(changeLogView);
        assertTrue(changeLogView.getText().toString().contains("Picked up Gold"));
    }

    private CharacterProfile seedProfile(PlayerClass playerClass) {
        CharacterProfile profile = new CharacterProfile("Test", playerClass);
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(profile))
                .commit();
        return profile;
    }

    private void showInventoryDialog(GameActivity activity) {
        ReflectionHelpers.callInstanceMethod(activity, "showInventoryDialog");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }

    private android.app.Dialog getLatestDialog() {
        android.app.AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        if (alertDialog != null) {
            return alertDialog;
        }
        return ShadowDialog.getLatestDialog();
    }
}
