package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.util.InventoryManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class InventoryUiTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE).edit().clear().commit();
        InventoryManager.setGold(context, 250);
    }

    @Test
    public void inventoryActivityDisplaysItemsAndGold() {
        InventoryManager.adjustItemQuantity(context, "RED KEY", 1);
        InventoryManager.adjustItemQuantity(context, "Trap Disarm Kit", 2);

        ActivityController<InventoryActivity> controller = Robolectric.buildActivity(InventoryActivity.class);
        InventoryActivity activity = controller.setup().get();

        RecyclerView recycler = activity.findViewById(R.id.recyclerInventory);
        TextView goldView = activity.findViewById(R.id.textInventoryGold);
        TextView emptyView = activity.findViewById(R.id.textEmptyInventory);

        assertTrue(recycler.getAdapter().getItemCount() >= 2);
        assertEquals(View.GONE, emptyView.getVisibility());
        assertTrue(goldView.getText().toString().contains("250"));
    }
}
