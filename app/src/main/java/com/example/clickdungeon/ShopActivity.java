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
import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.InventoryManager;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView goldText;

    private int gold;
    private final List<ShopItem> shopItems = new ArrayList<>();
    private ShopItemAdapter adapter;
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
        shopItems.addAll(GameBalance.loadShopItems(this));

        adapter = new ShopItemAdapter(shopItems, item -> {
            if (item.getStock() <= 0) {
                Toast.makeText(this, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
                return;
            }
            if (gold >= item.getPrice()) {
                gold = InventoryManager.adjustGold(this, -item.getPrice());
                int remaining = item.getStock() - 1;
                GameBalance.persistShopStock(this, item.getName(), remaining);
                refreshListStock(item.getName(), remaining);
                updateUI();
                Toast.makeText(this, getString(R.string.purchase_successful, item.getName()), Toast.LENGTH_SHORT).show();
                InventoryManager.adjustItemQuantity(this, item.getName(), 1);
            } else {
                Toast.makeText(this, getString(R.string.not_enough_gold), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void refreshListStock(String itemName, int remaining) {
        for (int i = 0; i < shopItems.size(); i++) {
            ShopItem item = shopItems.get(i);
            if (item.getName().equals(itemName)) {
                shopItems.set(i, item.withStock(remaining));
                break;
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateUI() {
        goldText.setText(getString(R.string.gold_display_dynamic, gold));
    }

    // Test hooks
    public void setShopItems(List<ShopItem> items) {
        shopItems.clear();
        shopItems.addAll(items);
        if (adapter == null) {
            adapter = new ShopItemAdapter(shopItems, item -> {
                // mirror purchase logic for test injection
                if (item.getStock() <= 0) {
                    return;
                }
                if (gold >= item.getPrice()) {
                    gold = InventoryManager.adjustGold(this, -item.getPrice());
                    int remaining = item.getStock() - 1;
                    refreshListStock(item.getName(), remaining);
                    updateUI();
                    InventoryManager.adjustItemQuantity(this, item.getName(), 1);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    public List<ShopItem> getShopItems() {
        return new ArrayList<>(shopItems);
    }
}
