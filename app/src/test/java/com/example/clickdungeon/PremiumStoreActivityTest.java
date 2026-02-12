package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.InventoryManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PremiumStoreActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        InventoryManager.setPlatinum(context, 100);
        java.util.List<com.example.clickdungeon.model.InventoryItem> cleared = new java.util.ArrayList<>();
        InventoryManager.savePremiumInventory(context, cleared);
    }

    @Test
    public void purchaseConsumesPlatinumAndAddsPremiumItem() {
        PremiumStoreActivity activity = Robolectric.buildActivity(PremiumStoreActivity.class).setup().get();
        // Select first item via adapter click.
        com.example.clickdungeon.model.ShopItem first = GameBalance.loadPremiumItems(context).get(0);

        activity.findViewById(R.id.btnPremiumBuy).performClick(); // should prompt selection
        // simulate selection by clicking through adapter
        androidx.recyclerview.widget.RecyclerView recycler = activity.findViewById(R.id.recyclerPremiumItems);
        com.example.clickdungeon.adapter.ShopItemAdapter adapter =
                (com.example.clickdungeon.adapter.ShopItemAdapter) recycler.getAdapter();
        java.lang.reflect.Field listenerField;
        try {
            listenerField = com.example.clickdungeon.adapter.ShopItemAdapter.class.getDeclaredField("listener");
            listenerField.setAccessible(true);
            com.example.clickdungeon.adapter.ShopItemAdapter.OnItemClickListener listener =
                    (com.example.clickdungeon.adapter.ShopItemAdapter.OnItemClickListener) listenerField.get(adapter);
            listener.onItemClick(first);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

        int before = InventoryManager.getPlatinum(context);
        activity.findViewById(R.id.btnPremiumBuy).performClick();

        int after = InventoryManager.getPlatinum(context);
        assertEquals(before - first.getPrice(), after);
        assertTrue(InventoryManager.loadPremiumInventory(context).stream()
                .anyMatch(item -> item.getName().equals(first.getName()) && item.getQuantity() > 0));
    }

    @Test
    public void purchaseBlocksWhenInsufficientPlatinum() {
        InventoryManager.setPlatinum(context, 0);
        PremiumStoreActivity activity = Robolectric.buildActivity(PremiumStoreActivity.class).setup().get();
        com.example.clickdungeon.model.ShopItem first = GameBalance.loadPremiumItems(context).get(0);

        // select item
        androidx.recyclerview.widget.RecyclerView recycler = activity.findViewById(R.id.recyclerPremiumItems);
        com.example.clickdungeon.adapter.ShopItemAdapter adapter =
                (com.example.clickdungeon.adapter.ShopItemAdapter) recycler.getAdapter();
        try {
            java.lang.reflect.Field listenerField = com.example.clickdungeon.adapter.ShopItemAdapter.class.getDeclaredField("listener");
            listenerField.setAccessible(true);
            com.example.clickdungeon.adapter.ShopItemAdapter.OnItemClickListener listener =
                    (com.example.clickdungeon.adapter.ShopItemAdapter.OnItemClickListener) listenerField.get(adapter);
            listener.onItemClick(first);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }

        activity.findViewById(R.id.btnPremiumBuy).performClick();
        assertEquals(0, InventoryManager.getPlatinum(context));
    }
}
