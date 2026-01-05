package com.example.clickdungeon.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.ShopItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ShopItemAdapterTest {

    @Test
    public void bindDisablesOutOfStockItems() {
        ShopItem item = new ShopItem("Potion", 20, "Restores", 0);
        ShopItemAdapter adapter = new ShopItemAdapter(Collections.singletonList(item), clicked -> { });

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_shop, null, false);
        ShopItemAdapter.ViewHolder holder = new ShopItemAdapter.ViewHolder(view);

        holder.bind(item, clicked -> { });

        TextView price = view.findViewById(R.id.textItemPrice);
        assertTrue(price.getText().toString().contains("Out of stock"));
        assertEquals(0.5f, view.getAlpha(), 0.01f);
        assertFalse(view.isEnabled());
    }

    @Test
    public void bindWiresClickListenerForInStock() {
        ShopItem item = new ShopItem("Potion", 20, "Restores", 1);
        AtomicBoolean clicked = new AtomicBoolean(false);

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_shop, null, false);
        ShopItemAdapter.ViewHolder holder = new ShopItemAdapter.ViewHolder(view);

        holder.bind(item, clickedItem -> clicked.set(true));
        view.performClick();

        assertTrue(clicked.get());
    }
}
