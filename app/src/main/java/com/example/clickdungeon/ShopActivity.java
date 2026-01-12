package com.example.clickdungeon;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.PricedItemAdapter;
import com.example.clickdungeon.adapter.ShopItemAdapter;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.PricedItem;
import com.example.clickdungeon.model.ShopItem;
import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.MerchantManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ShopActivity handles purchasing gear and managing secondary merchant interactions.
 * It supports standard shop stock, as well as sell and buyback functionality 
 * when a merchant visit is triggered during a dungeon run.
 */
public class ShopActivity extends AppCompatActivity {

    /** Extra flag containing the floor number where the merchant visit occurred. */
    public static final String EXTRA_MERCHANT_FLOOR = "com.example.clickdungeon.extra.MERCHANT_FLOOR";
    private static final int DEFAULT_RESALE_VALUE = 10;

    // View references for currency and item lists.
    private RecyclerView recyclerView;
    private TextView goldText;
    private TextView platinumText;
    private RecyclerView sellRecycler;
    private RecyclerView buybackRecycler;
    private TextView sellHeader;
    private TextView buybackHeader;

    private int gold;
    private int platinum;
    private final List<ShopItem> shopItems = new ArrayList<>();
    private final List<PricedItem> sellItems = new ArrayList<>();
    private final List<PricedItem> buybackItems = new ArrayList<>();
    
    private ShopItemAdapter adapter;
    private PricedItemAdapter sellAdapter;
    private PricedItemAdapter buybackAdapter;
    
    private boolean isMerchantVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.shop);
        setContentView(R.layout.activity_shop);

        // Bind UI components.
        recyclerView = findViewById(R.id.recyclerShopItems);
        goldText = findViewById(R.id.textGold);
        platinumText = findViewById(R.id.textPlatinum);
        sellRecycler = findViewById(R.id.recyclerSellItems);
        buybackRecycler = findViewById(R.id.recyclerBuybackItems);
        sellHeader = findViewById(R.id.textSellHeader);
        buybackHeader = findViewById(R.id.textBuybackHeader);
        Button refreshButton = findViewById(R.id.btnRefreshShop);

        // Initialize layouts for the vertical lists.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sellRecycler.setLayoutManager(new LinearLayoutManager(this));
        buybackRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        // Fetch current currency state.
        gold = InventoryManager.getGold(this);
        platinum = InventoryManager.getPlatinum(this);

        // Determine if this is a specialized merchant visit or a standard menu shop.
        int merchantFloor = getIntent().getIntExtra(EXTRA_MERCHANT_FLOOR, -1);
        isMerchantVisit = merchantFloor > 0;
        if (isMerchantVisit) {
            setTitle(R.string.merchant_title);
            if (MerchantManager.getLastVisitFloor(this) != merchantFloor) {
                MerchantManager.setLastVisitFloor(this, merchantFloor);
            }
        } else {
            // Hide merchant-specific panels if just visiting from the main menu.
            sellHeader.setVisibility(View.GONE);
            sellRecycler.setVisibility(View.GONE);
            buybackHeader.setVisibility(View.GONE);
            buybackRecycler.setVisibility(View.GONE);
        }

        // Initialize the item data sets.
        loadShopItems();
        refreshSellItems();
        refreshBuybackItems();

        // Allow force-refreshing shop contents (primarily for testing/dev).
        refreshButton.setOnClickListener(view -> loadShopItems());

        updateUI();
    }

    /**
     * Loads available purchaseable items from GameBalance and populates the main recycler.
     */
    private void loadShopItems() {
        shopItems.clear();
        shopItems.addAll(GameBalance.loadShopItems(this));
        Map<String, ShopItem> shopIndex = buildShopIndex(shopItems);

        adapter = new ShopItemAdapter(shopItems, item -> {
            if (item.getStock() <= 0) {
                Toast.makeText(this, R.string.item_out_of_stock, Toast.LENGTH_SHORT).show();
                return;
            }
            if (gold >= item.getPrice()) {
                // Deduct currency and update stock levels.
                gold = InventoryManager.adjustGold(this, -item.getPrice());
                int remaining = item.getStock() - 1;
                GameBalance.persistShopStock(this, item.getName(), remaining);
                refreshListStock(item.getName(), remaining);
                updateUI();
                Toast.makeText(this, getString(R.string.purchase_successful, item.getName()), Toast.LENGTH_SHORT).show();
                
                // Add the item to inventory and refresh sell prices.
                InventoryManager.adjustItemQuantity(this, item.getName(), 1);
                refreshSellItems(shopIndex);
            } else {
                Toast.makeText(this, getString(R.string.not_enough_gold), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * Incremental list update when an item is purchased.
     */
    private void refreshListStock(String itemName, int remaining) {
        for (int i = 0; i < shopItems.size(); i++) {
            ShopItem item = shopItems.get(i);
            if (item.getName().equals(itemName)) {
                shopItems.set(i, item.withStock(remaining));
                if (adapter != null) {
                    adapter.notifyItemChanged(i);
                }
                break;
            }
        }
    }

    /**
     * Syncs currency text labels with current state.
     */
    private void updateUI() {
        gold = InventoryManager.getGold(this);
        platinum = InventoryManager.getPlatinum(this);
        goldText.setText(getString(R.string.gold_display_dynamic, gold));
        platinumText.setText(getString(R.string.platinum_display_dynamic, platinum));
    }

    private void refreshSellItems() {
        refreshSellItems(buildShopIndex(shopItems));
    }

    /**
     * Populates the sell list based on the user's current inventory.
     */
    private void refreshSellItems(Map<String, ShopItem> shopIndex) {
        if (!isMerchantVisit) {
            return;
        }
        int previousSize = sellItems.size();
        sellItems.clear();
        List<InventoryItem> inventory = InventoryManager.loadInventory(this);
        for (InventoryItem item : inventory) {
            int resale = getResaleValue(item.getName(), shopIndex);
            sellItems.add(new PricedItem(item.getName(), resale, item.getQuantity()));
        }
        if (sellAdapter == null) {
            sellAdapter = new PricedItemAdapter(sellItems, item -> sellItem(item, shopIndex));
            sellRecycler.setAdapter(sellAdapter);
        } else {
            if (previousSize > 0) {
                sellAdapter.notifyItemRangeRemoved(0, previousSize);
            }
            if (!sellItems.isEmpty()) {
                sellAdapter.notifyItemRangeInserted(0, sellItems.size());
            }
        }
    }

    /**
     * Populates the buyback list with items recently sold to the merchant.
     */
    private void refreshBuybackItems() {
        if (!isMerchantVisit) {
            return;
        }
        int previousSize = buybackItems.size();
        buybackItems.clear();
        buybackItems.addAll(MerchantManager.loadBuybackItems(this));
        if (buybackAdapter == null) {
            buybackAdapter = new PricedItemAdapter(buybackItems, this::buybackItem);
            buybackRecycler.setAdapter(buybackAdapter);
        } else {
            if (previousSize > 0) {
                buybackAdapter.notifyItemRangeRemoved(0, previousSize);
            }
            if (!buybackItems.isEmpty()) {
                buybackAdapter.notifyItemRangeInserted(0, buybackItems.size());
            }
        }
    }

    /**
     * Executes the sale of an inventory item.
     */
    private void sellItem(PricedItem item, Map<String, ShopItem> shopIndex) {
        if (item.getQuantity() <= 0) {
            return;
        }
        InventoryManager.adjustItemQuantity(this, item.getName(), -1);
        gold = InventoryManager.adjustGold(this, item.getPrice());
        // Move the item to the buyback tab.
        MerchantManager.addBuybackItem(this, new PricedItem(item.getName(), item.getPrice(), 1));
        Toast.makeText(this, getString(R.string.sale_successful, item.getName()), Toast.LENGTH_SHORT).show();
        refreshSellItems(shopIndex);
        refreshBuybackItems();
        updateUI();
    }

    /**
     * Allows the player to repurchase a previously sold item.
     */
    private void buybackItem(PricedItem item) {
        if (item.getQuantity() <= 0) {
            return;
        }
        if (gold < item.getPrice()) {
            Toast.makeText(this, getString(R.string.not_enough_gold), Toast.LENGTH_SHORT).show();
            return;
        }
        gold = InventoryManager.adjustGold(this, -item.getPrice());
        InventoryManager.adjustItemQuantity(this, item.getName(), 1);
        updateBuybackQuantity(item.getName(), item.getPrice(), -1);
        refreshBuybackItems();
        refreshSellItems();
        updateUI();
    }

    /**
     * Updates persistent buyback quantities.
     */
    private void updateBuybackQuantity(String name, int price, int delta) {
        List<PricedItem> items = MerchantManager.loadBuybackItems(this);
        for (int i = 0; i < items.size(); i++) {
            PricedItem item = items.get(i);
            if (item.getName().equals(name) && item.getPrice() == price) {
                int updated = item.getQuantity() + delta;
                if (updated <= 0) {
                    items.remove(i);
                } else {
                    items.set(i, item.withQuantity(updated));
                }
                MerchantManager.saveBuybackItems(this, items);
                return;
            }
        }
    }

    /**
     * Builds a map for quick price lookups.
     */
    private Map<String, ShopItem> buildShopIndex(List<ShopItem> items) {
        Map<String, ShopItem> map = new HashMap<>();
        for (ShopItem item : items) {
            map.put(item.getName(), item);
        }
        return map;
    }

    /**
     * Returns the gold value when selling an item.
     */
    private int getResaleValue(String name, Map<String, ShopItem> shopIndex) {
        ShopItem item = shopIndex.get(name);
        if (item != null) {
            return item.getResaleValue();
        }
        return DEFAULT_RESALE_VALUE;
    }

    /**
     * Injects custom items for testing scenarios.
     */
    public void setShopItems(List<ShopItem> items) {
        int previousSize = shopItems.size();
        shopItems.clear();
        shopItems.addAll(items);
        if (adapter == null) {
            adapter = new ShopItemAdapter(shopItems, item -> {
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
            if (previousSize > 0) {
                adapter.notifyItemRangeRemoved(0, previousSize);
            }
            if (!shopItems.isEmpty()) {
                adapter.notifyItemRangeInserted(0, shopItems.size());
            }
        }
    }

    public List<ShopItem> getShopItems() {
        return new ArrayList<>(shopItems);
    }
}
