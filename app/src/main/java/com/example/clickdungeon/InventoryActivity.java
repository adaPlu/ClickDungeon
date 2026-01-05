package com.example.clickdungeon;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.InventoryAdapter;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.util.InventoryManager;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.inventory_title);
        setContentView(R.layout.dialog_inventory);

        RecyclerView recycler = findViewById(R.id.recyclerInventory);
        TextView goldView = findViewById(R.id.textInventoryGold);
        TextView emptyView = findViewById(R.id.textEmptyInventory);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        List<InventoryItem> items = InventoryManager.loadInventory(this);
        recycler.setAdapter(new InventoryAdapter(items));
        goldView.setText(getString(R.string.gold_display_dynamic, InventoryManager.getGold(this)));
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
