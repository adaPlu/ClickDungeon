package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.util.InventoryManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import com.adaplu.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class InventoryUiTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
        InventoryManager.setGold(context, 250);
        InventoryManager.setPlatinum(context, 120);
    }

    @Test
    public void inventoryActivityDisplaysItemsAndGold() {
        InventoryManager.adjustItemQuantity(context, "RED KEY", 1);
        InventoryManager.adjustItemQuantity(context, "Trap Disarm Kit", 2);

        ActivityController<InventoryActivity> controller = Robolectric.buildActivity(InventoryActivity.class);
        InventoryActivity activity = controller.setup().get();

        RecyclerView recycler = activity.findViewById(R.id.recyclerInventory);
        TextView goldView = activity.findViewById(R.id.textInventoryGold);
        TextView platinumView = activity.findViewById(R.id.textInventoryPlatinum);
        TextView emptyView = activity.findViewById(R.id.textEmptyInventory);

        assertTrue(recycler.getAdapter().getItemCount() >= 2);
        assertEquals(View.GONE, emptyView.getVisibility());
        assertTrue(goldView.getText().toString().contains("250"));
        assertTrue(platinumView.getText().toString().contains("120"));
    }

    @Test
    public void inventoryActivityShowsSimplifiedStatSummary() {
        CharacterProfile profile = new CharacterProfile("Tuning", PlayerClass.KNIGHT);
        profile.addExperience(120);
        profile.addAttackBoost(1);
        context.getSharedPreferences("player_profile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile", new Gson().toJson(profile))
                .commit();

        ActivityController<InventoryActivity> controller = Robolectric.buildActivity(InventoryActivity.class);
        InventoryActivity activity = controller.setup().get();

        TextView classXpView = activity.findViewById(R.id.textCurrentClassXp);
        TextView attackView = activity.findViewById(R.id.textStatAttack);

        assertTrue(classXpView.getText().toString().contains("120"));
        assertTrue(attackView.getText().toString().contains("ATK:"));
        assertTrue(attackView.getText().toString().contains("(+1)"));
    }
}
