package com.example.clickdungeon;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.ShopItemAdapter;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.InventoryManager;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView goldText;

    private int gold;
    private final List<ShopItem> shopItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.shop);
        setContentView(R.layout.activity_shop);

        recyclerView = findViewById(R.id.recyclerShopItems);
        goldText = findViewById(R.id.textGold);
        Button refreshButton = findViewById(R.id.btnRefreshShop);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gold = InventoryManager.getGold(this);

        loadMockItems();

        refreshButton.setOnClickListener(view -> loadMockItems());

        updateUI();
    }

    private void loadMockItems() {
        shopItems.clear();
        shopItems.add(new ShopItem("Healing Potion", 100));
        shopItems.add(new ShopItem("Trap Disarm Kit", 150));
        shopItems.add(new ShopItem("Mana Potion", 120));
        shopItems.add(new ShopItem("Iron Sword", 250));

        recyclerView.setAdapter(new ShopItemAdapter(shopItems, item -> {
            if (gold >= item.getPrice()) {
                gold = InventoryManager.adjustGold(this, -item.getPrice());
                updateUI();
                Toast.makeText(this, getString(R.string.purchase_successful, item.getName()), Toast.LENGTH_SHORT).show();
                InventoryManager.adjustItemQuantity(this, item.getName(), 1);
            } else {
                Toast.makeText(this, getString(R.string.not_enough_gold), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void updateUI() {
        goldText.setText(getString(R.string.gold_display_dynamic, gold));
    }
}
