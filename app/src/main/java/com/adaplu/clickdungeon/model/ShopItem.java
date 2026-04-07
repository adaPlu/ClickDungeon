package com.adaplu.clickdungeon.model;

/**
 * Represents a shop catalog item with pricing, stock, and optional description.
 */
public class ShopItem {
    /** Item identifier or display name. */
    private final String name;
    /** Price in gold for a single unit. */
    private final int price;
    /** Current stock level available for purchase. */
    private final int stock;
    /** Optional descriptive text for UI display. */
    private final String description;
    /** Resale value used when selling to a merchant. */
    private final int resaleValue;

    /**
     * Creates a shop item with a default resale value.
     */
    public ShopItem(String name, int price, int stock) {
        this(name, price, null, stock, Math.max(1, price / 2));
    }

    /**
     * Creates a shop item with a description and default resale value.
     */
    public ShopItem(String name, int price, String description, int stock) {
        this(name, price, description, stock, Math.max(1, price / 2));
    }

    /**
     * Creates a shop item with explicit resale value.
     */
    public ShopItem(String name, int price, String description, int stock, int resaleValue) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.resaleValue = Math.max(1, resaleValue);
    }

    /** Returns the item name. */
    public String getName() { return name; }
    /** Returns the purchase price. */
    public int getPrice() { return price; }
    /** Returns the available stock. */
    public int getStock() { return stock; }
    /** Returns the stock count (alias used in tests). */
    public int getQuantity() { return stock; } // Alias for tests
    /** Returns the description text, if any. */
    public String getDescription() { return description; }
    /** Returns the resale value in gold. */
    public int getResaleValue() { return resaleValue; }

    /**
     * Returns a new instance with updated stock.
     */
    public ShopItem withStock(int newStock) {
        return new ShopItem(name, price, description, newStock, resaleValue);
    }
}
