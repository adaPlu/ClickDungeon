package com.example.clickdungeon.model;

public class ShopItem {
    private final String name;
    private final int price;
    private final int stock;
    private final String description;

    public ShopItem(String name, int price, int stock) {
        this(name, price, null, stock);
    }

    public ShopItem(String name, int price, String description, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public int getQuantity() { return stock; } // Alias for tests
    public String getDescription() { return description; }

    public ShopItem withStock(int newStock) {
        return new ShopItem(name, price, description, newStock);
    }
}
