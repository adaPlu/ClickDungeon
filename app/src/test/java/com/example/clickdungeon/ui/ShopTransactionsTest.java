package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.GameActivity;
import com.example.clickdungeon.ShopActivity;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.adapter.ShopItemAdapter;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class ShopTransactionsTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        InventoryManager.syncGoldWithCurrentRun(context, 100);
        InventoryManager.clearInventory(context);
        SharedPreferences profilePrefs = context.getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        profilePrefs.edit()
                .putString("profile", new Gson().toJson(new CharacterProfile("Shopper", PlayerClass.KNIGHT)))
                .apply();
    }

    @Test
    public void purchaseDecrementsGoldAndStock() {
        ActivityController<ShopActivity> controller = Robolectric.buildActivity(ShopActivity.class);
        ShopActivity activity = controller.setup().get();

        // Inject deterministic shop items
        List<ShopItem> items = new ArrayList<>();
        items.add(new ShopItem("Trap Disarm Kit", 20, 2));
        items.add(new ShopItem("Potion", 10, 1));
        activity.setShopItems(items);

        RecyclerView list = activity.findViewById(R.id.recyclerShopItems);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        // Click first item twice to deplete stock using the adapter listener.
        ShopItemAdapter adapter = (ShopItemAdapter) list.getAdapter();
        ShopItem first = activity.getShopItems().get(0);
        invokeClick(adapter, first);
        ShopItem updatedFirst = activity.getShopItems().get(0);
        invokeClick(adapter, updatedFirst);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        int remainingGold = InventoryManager.getGold(context);
        assertEquals("Gold should reduce by 40", 60, remainingGold);

        InventoryItem item = InventoryManager.getInventoryItem(context, "Trap Disarm Kit");
        assertEquals(2, item.getQuantity());

        ShopItem updated = activity.getShopItems().get(0);
        assertEquals(0, updated.getQuantity());

        // Third click should fail due to stock
        invokeClick(adapter, updated);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertEquals("Gold should not change when out of stock", 60, InventoryManager.getGold(context));
    }

    @Test
    public void purchaseBlocksWhenInsufficientGold() {
        InventoryManager.syncGoldWithCurrentRun(context, 5);
        ActivityController<ShopActivity> controller = Robolectric.buildActivity(ShopActivity.class);
        ShopActivity activity = controller.setup().get();

        List<ShopItem> items = new ArrayList<>();
        items.add(new ShopItem("Expensive Item", 50, 1));
        activity.setShopItems(items);

        RecyclerView list = activity.findViewById(R.id.recyclerShopItems);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        ShopItemAdapter adapter = (ShopItemAdapter) list.getAdapter();
        invokeClick(adapter, items.get(0));
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals("Gold should remain when insufficient", 5, InventoryManager.getGold(context));
        ShopItem updated = activity.getShopItems().get(0);
        assertEquals(1, updated.getQuantity());
        InventoryItem inv = InventoryManager.getInventoryItem(context, "Expensive Item");
        assertTrue(inv == null || inv.getQuantity() == 0);
    }

    private void invokeClick(ShopItemAdapter adapter, ShopItem item) {
        try {
            java.lang.reflect.Field field = ShopItemAdapter.class.getDeclaredField("listener");
            field.setAccessible(true);
            ShopItemAdapter.OnItemClickListener listener =
                    (ShopItemAdapter.OnItemClickListener) field.get(adapter);
            listener.onItemClick(item);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void maybeShowMerchant_eligibleFloor_startsMerchantShop() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        GameActivity activity = controller.setup().get();
        ReflectionHelpers.setField(activity, "currentFloor", 6);
        ReflectionHelpers.setField(activity, "random", new FloatRandom(0.1f));

        ReflectionHelpers.callInstanceMethod(activity, "maybeShowMerchant");

        Intent started = org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity();
        assertTrue(started != null);
        assertEquals(ShopActivity.class.getName(), started.getComponent().getClassName());
        assertEquals(6, started.getIntExtra(ShopActivity.EXTRA_MERCHANT_FLOOR, -1));
    }

    @Test
    public void maybeShowMerchant_ineligibleOrMissedChance_doesNotStartShop() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        GameActivity activity = controller.setup().get();

        ReflectionHelpers.setField(activity, "currentFloor", 5);
        ReflectionHelpers.setField(activity, "random", new FloatRandom(0.0f));
        ReflectionHelpers.callInstanceMethod(activity, "maybeShowMerchant");
        assertTrue(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity() == null);

        ReflectionHelpers.setField(activity, "currentFloor", 6);
        ReflectionHelpers.setField(activity, "random", new FloatRandom(0.9f));
        ReflectionHelpers.callInstanceMethod(activity, "maybeShowMerchant");
        assertTrue(org.robolectric.Shadows.shadowOf(activity).getNextStartedActivity() == null);
    }

    private static final class FloatRandom extends java.util.Random {
        private final float value;

        FloatRandom(float value) {
            this.value = value;
        }

        @Override
        public float nextFloat() {
            return value;
        }
    }
}
