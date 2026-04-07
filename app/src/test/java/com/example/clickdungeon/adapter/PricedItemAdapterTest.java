package com.example.clickdungeon.adapter;

import static org.junit.Assert.assertTrue;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.PricedItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PricedItemAdapterTest {

    @Test
    public void bindDisplaysNameAndPrice() {
        PricedItem item = new PricedItem("Herb", 5, 2);
        PricedItemAdapter adapter = new PricedItemAdapter(Collections.singletonList(item), clicked -> { });

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_priced, null, false);
        PricedItemAdapter.ViewHolder holder = new PricedItemAdapter.ViewHolder(view);

        holder.bind(item, clicked -> { });

        TextView name = view.findViewById(R.id.textPricedItemName);
        TextView price = view.findViewById(R.id.textPricedItemPrice);
        assertTrue(name.getText().toString().contains("Herb"));
        assertTrue(price.getText().toString().contains("Gold"));
        assertTrue(price.getText().toString().contains("Qty: 2"));
    }

    @Test
    public void bindWiresClickListener() {
        PricedItem item = new PricedItem("Elixir", 30, 1);
        AtomicBoolean clicked = new AtomicBoolean(false);

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_priced, null, false);
        PricedItemAdapter.ViewHolder holder = new PricedItemAdapter.ViewHolder(view);

        holder.bind(item, clickedItem -> clicked.set(true));
        view.performClick();

        assertTrue(clicked.get());
    }
}
