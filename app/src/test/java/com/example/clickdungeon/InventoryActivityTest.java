package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class InventoryActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "player_prefs")
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void emptyInventoryShowsEmptyState() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        View emptyView = activity.findViewById(R.id.textEmptyInventory);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }

    @Test
    public void inventoryCountAndChangeLogRender() {
        InventoryManager.adjustItemQuantity(context, "Healing Potion", 2);
        InventoryManager.recordInventoryChange(context, "Found Healing Potion");

        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        TextView countView = activity.findViewById(R.id.textInventoryCount);
        TextView changeLogView = activity.findViewById(R.id.textInventoryChangeLog);
        assertTrue(countView.getText().toString().contains("2"));
        assertTrue(changeLogView.getText().toString().contains("Found Healing Potion"));
    }
}
