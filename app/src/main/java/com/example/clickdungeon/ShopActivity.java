package com.example.clickdungeon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.ShopItemAdapter;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.InventoryManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView goldText;
    private Button refreshButton;

    private int gold;
    private final List<ShopItem> shopItems = new ArrayList<>();
    private List<InventoryItem> inventory = new ArrayList<>();

    private static final String PREFS_NAME = "player_prefs";
    private static final String GOLD_KEY = "gold";
    private static final String INVENTORY_KEY = "inventory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.shop);
        setContentView(R.layout.activity_shop);

        recyclerView = findViewById(R.id.recyclerShopItems);
        goldText = findViewById(R.id.textGold);
        refreshButton = findViewById(R.id.btnRefreshShop);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gold = loadGold();
        inventory = loadInventory();

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
                gold -= item.getPrice();
                saveGold(gold);
                updateUI();
                Toast.makeText(this, getString(R.string.purchase_successful, item.getName()), Toast.LENGTH_SHORT).show();
                updateInventory(item.getName(), 1);
            } else {
                Toast.makeText(this, getString(R.string.not_enough_gold), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void updateInventory(String itemName, int quantity) {
        boolean found = false;
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem item = inventory.get(i);
            if (item.getName().equals(itemName)) {
                inventory.set(i, new InventoryItem(item.getName(), item.getQuantity() + quantity));
                found = true;
                break;
            }
        }
        if (!found) {
            inventory.add(new InventoryItem(itemName, quantity));
        }
        saveInventory(inventory);
    }

    private void updateUI() {
        goldText.setText(getString(R.string.gold_display_dynamic, gold));
    }

    private void saveGold(int amount) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(GOLD_KEY, amount).apply();
    }

    private int loadGold() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(GOLD_KEY, 500); // default 500
    }

    private void saveInventory(List<InventoryItem> inventory) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(inventory);
        prefs.edit().putString(INVENTORY_KEY, json).apply();
    }

    private List<InventoryItem> loadInventory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(INVENTORY_KEY, null);
        Type type = new TypeToken<List<InventoryItem>>() {}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }
}
