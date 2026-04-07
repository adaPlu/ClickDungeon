package com.adaplu.clickdungeon;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adaplu.clickdungeon.adapter.ShopItemAdapter;
import com.adaplu.clickdungeon.model.ShopItem;
import com.adaplu.clickdungeon.util.GameBalance;
import com.adaplu.clickdungeon.util.InventoryManager;
import com.adaplu.clickdungeon.util.SoundManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder premium store that spends platinum and delivers premium inventory items.
 */
public class PremiumStoreActivity extends AppCompatActivity {

    private final List<ShopItem> premiumItems = new ArrayList<>();
    private ShopItemAdapter adapter;
    private TextView platinumView;
    private int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.premium_store_title);
        setContentView(R.layout.activity_premium_store);

        platinumView = findViewById(R.id.textPremiumPlatinum);
        RecyclerView recycler = findViewById(R.id.recyclerPremiumItems);
        Button buyButton = findViewById(R.id.btnPremiumBuy);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        premiumItems.addAll(GameBalance.loadPremiumItems(this));
        adapter = new ShopItemAdapter(premiumItems, item -> {
            selectedIndex = premiumItems.indexOf(item);
            Toast.makeText(this, getString(R.string.premium_selected, item.getName()), Toast.LENGTH_SHORT).show();
        });
        recycler.setAdapter(adapter);

        updatePlatinumDisplay();

        buyButton.setOnClickListener(v -> attemptPurchase());
    }

    private void attemptPurchase() {
        if (selectedIndex < 0 || selectedIndex >= premiumItems.size()) {
            Toast.makeText(this, R.string.premium_select_item, Toast.LENGTH_SHORT).show();
            return;
        }
        ShopItem selected = premiumItems.get(selectedIndex);
        if (selected.getStock() <= 0) {
            Toast.makeText(this, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
            return;
        }
        int currentPlatinum = InventoryManager.getPlatinum(this);
        if (currentPlatinum < selected.getPrice()) {
            Toast.makeText(this, R.string.not_enough_platinum, Toast.LENGTH_SHORT).show();
            return;
        }

        InventoryManager.adjustPlatinum(this, -selected.getPrice());
        InventoryManager.adjustPremiumItemQuantity(this, selected.getName(), 1, Integer.MAX_VALUE);
        SoundManager.playAndReport(SoundManager.KEY_EFFECT_SHOP_PURCHASE);
        Toast.makeText(this, getString(R.string.premium_purchase_success, selected.getName()), Toast.LENGTH_SHORT).show();
        updatePlatinumDisplay();
    }

    private void updatePlatinumDisplay() {
        platinumView.setText(getString(R.string.platinum_display_dynamic, InventoryManager.getPlatinum(this)));
    }
}
