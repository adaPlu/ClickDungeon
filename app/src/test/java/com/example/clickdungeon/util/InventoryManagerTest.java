package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.InventoryItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class InventoryManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void adjustItemQuantityAddsUpdatesAndRemovesItems() {
        InventoryManager.adjustItemQuantity(context, "Potion", 2);
        assertEquals(2, InventoryManager.getItemQuantity(context, "Potion"));

        InventoryManager.adjustItemQuantity(context, "Potion", 3);
        assertEquals(5, InventoryManager.getItemQuantity(context, "Potion"));

        InventoryManager.adjustItemQuantity(context, "Potion", -5);
        assertEquals(0, InventoryManager.getItemQuantity(context, "Potion"));

        List<InventoryItem> inventory = InventoryManager.loadInventory(context);
        assertTrue(inventory.isEmpty());
    }

    @Test
    public void quantityRespectsProvidedMaximum() {
        InventoryManager.adjustItemQuantity(context, "Trap Disarm Kit", 10, 3);
        assertEquals(3, InventoryManager.getItemQuantity(context, "Trap Disarm Kit"));

        InventoryManager.adjustItemQuantity(context, "Trap Disarm Kit", -1, 3);
        assertEquals(2, InventoryManager.getItemQuantity(context, "Trap Disarm Kit"));
    }

    @Test
    public void goldOperationsClampToValidRange() {
        assertEquals(GameBalance.STARTING_GOLD, InventoryManager.getGold(context));

        InventoryManager.setGold(context, 1200);
        assertEquals(1200, InventoryManager.getGold(context));

        InventoryManager.adjustGold(context, -300);
        assertEquals(900, InventoryManager.getGold(context));

        InventoryManager.adjustGold(context, -1000);
        assertEquals(0, InventoryManager.getGold(context));
    }

    @Test
    public void platinumOperationsClampToValidRange() {
        assertEquals(GameBalance.STARTING_PLATINUM, InventoryManager.getPlatinum(context));

        InventoryManager.setPlatinum(context, 240);
        assertEquals(240, InventoryManager.getPlatinum(context));

        InventoryManager.adjustPlatinum(context, -40);
        assertEquals(200, InventoryManager.getPlatinum(context));

        InventoryManager.adjustPlatinum(context, -1000);
        assertEquals(0, InventoryManager.getPlatinum(context));
    }

    @Test
    public void clearInventoryRemovesAllItems() {
        InventoryManager.adjustItemQuantity(context, "Potion", 2);
        InventoryManager.adjustItemQuantity(context, "Trap Disarm Kit", 1);

        InventoryManager.clearInventory(context);

        assertTrue(InventoryManager.loadInventory(context).isEmpty());
    }

    @Test
    public void getInventoryItemReturnsFirstMatchOrNull() {
        assertNull(InventoryManager.getInventoryItem(context, "Unknown"));

        InventoryManager.adjustItemQuantity(context, "Potion", 2);
        InventoryItem item = InventoryManager.getInventoryItem(context, "Potion");

        assertEquals("Potion", item.getName());
        assertEquals(2, item.getQuantity());
    }

    @Test
    public void syncGoldWithCurrentRunUpdatesStoredGold() {
        InventoryManager.setGold(context, 100);
        InventoryManager.syncGoldWithCurrentRun(context, 250);

        assertEquals(250, InventoryManager.getGold(context));
    }

    @Test
    public void syncPlatinumWithCurrentRunUpdatesStoredPlatinum() {
        InventoryManager.setPlatinum(context, 80);
        InventoryManager.syncPlatinumWithCurrentRun(context, 140);

        assertEquals(140, InventoryManager.getPlatinum(context));
    }
}
