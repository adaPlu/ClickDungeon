package com.example.clickdungeon.adapter;

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.InventoryItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class InventoryAdapterTest {

    @Test
    public void bindPopulatesNameAndQuantity() {
        InventoryItem item = new InventoryItem("Potion", 2);
        InventoryAdapter adapter = new InventoryAdapter(Collections.singletonList(item));

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_inventory, null, false);
        InventoryAdapter.ViewHolder holder = new InventoryAdapter.ViewHolder(view);

        holder.bind(item, null);

        TextView name = view.findViewById(R.id.textItemName);
        TextView quantity = view.findViewById(R.id.textItemQuantity);
        assertEquals("Potion", name.getText().toString());
        assertEquals("x2", quantity.getText().toString());
    }
}
