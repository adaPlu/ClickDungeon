package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.PricedItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class MerchantManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("merchant_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void addBuybackItemAggregatesQuantities() {
        MerchantManager.addBuybackItem(context, new PricedItem("Sword", 20, 1));
        MerchantManager.addBuybackItem(context, new PricedItem("Sword", 20, 2));

        List<PricedItem> items = MerchantManager.loadBuybackItems(context);
        assertEquals(1, items.size());
        assertEquals(3, items.get(0).getQuantity());
    }

    @Test
    public void setAndGetLastVisitFloor() {
        assertEquals(-1, MerchantManager.getLastVisitFloor(context));
        MerchantManager.setLastVisitFloor(context, 5);
        assertEquals(5, MerchantManager.getLastVisitFloor(context));
    }

    @Test
    public void clearBuybackItemsEmptiesList() {
        MerchantManager.addBuybackItem(context, new PricedItem("Shield", 15, 1));
        MerchantManager.clearBuybackItems(context);
        assertTrue(MerchantManager.loadBuybackItems(context).isEmpty());
    }
}
