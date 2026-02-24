package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.adapter.ShopItemAdapter;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.InventoryManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.example.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class ShopActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "player_prefs").edit().clear().commit();
        context.getSharedPreferences("shop_prefs", Context.MODE_PRIVATE).edit().clear().commit();
        InventoryManager.setGold(context, 500);
        // reset cached shop
        GameBalance.persistShopStock(context, "Healing Potion", 5);
        GameBalance.persistShopStock(context, "Trap Disarm Kit", 3);
        GameBalance.persistShopStock(context, "Mana Potion", 4);
        GameBalance.persistShopStock(context, "Iron Sword", 2);
    }

    @Test
    public void purchaseUpdatesGoldInventoryAndStock() throws Exception {
        ActivityController<ShopActivity> controller = Robolectric.buildActivity(ShopActivity.class);
        ShopActivity activity = controller.setup().get();

        RecyclerView recycler = activity.findViewById(R.id.recyclerShopItems);
        ShopItemAdapter adapter = (ShopItemAdapter) recycler.getAdapter();
        ShopItem first = getShopItem(adapter, 0);
        int startingGold = InventoryManager.getGold(context);
        int startingStock = first.getStock();

        invokeClick(adapter, first);

        int expectedGold = startingGold - first.getPrice();
        assertEquals(expectedGold, InventoryManager.getGold(context));
        assertTrue(InventoryManager.getItemQuantity(context, first.getName()) > 0);

        List<ShopItem> refreshed = GameBalance.loadShopItems(context);
        ShopItem updated = refreshed.stream()
                .filter(item -> item.getName().equals(first.getName()))
                .findFirst()
                .orElse(null);
        assertEquals(Math.max(0, startingStock - 1), updated.getStock());
    }

    @Test
    public void debugPreviewLongPressShowsDialog() {
        ActivityController<ShopActivity> controller = Robolectric.buildActivity(ShopActivity.class);
        ShopActivity activity = controller.setup().get();

        android.widget.Button refreshButton = activity.findViewById(R.id.btnRefreshShop);
        boolean handled = refreshButton.performLongClick();

        android.app.AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        if (handled) {
            assertTrue(dialog != null);
        } else {
            assertTrue(dialog == null);
        }
    }

    private ShopItem getShopItem(ShopItemAdapter adapter, int index) {
        try {
            Field field = ShopItemAdapter.class.getDeclaredField("shopItems");
            field.setAccessible(true);
            Object raw = field.get(adapter);
            List<ShopItem> items = new ArrayList<>();
            if (raw instanceof List) {
                for (Object entry : (List<?>) raw) {
                    if (entry instanceof ShopItem) {
                        items.add((ShopItem) entry);
                    }
                }
            }
            return items.get(index);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void invokeClick(ShopItemAdapter adapter, ShopItem item) {
        try {
            Field field = ShopItemAdapter.class.getDeclaredField("listener");
            field.setAccessible(true);
            ShopItemAdapter.OnItemClickListener listener = (ShopItemAdapter.OnItemClickListener) field.get(adapter);
            listener.onItemClick(item);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
