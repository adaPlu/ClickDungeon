package com.example.clickdungeon.model;

public class ShopItem {
    private final String name;
    private final int price;
    private final int stock;
    private final String description;
    private final int resaleValue;

    public ShopItem(String name, int price, int stock) {
        this(name, price, null, stock, Math.max(1, price / 2));
    }

    public ShopItem(String name, int price, String description, int stock) {
        this(name, price, description, stock, Math.max(1, price / 2));
    }

    public ShopItem(String name, int price, String description, int stock, int resaleValue) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.resaleValue = Math.max(1, resaleValue);
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public int getQuantity() { return stock; } // Alias for tests
    public String getDescription() { return description; }
    public int getResaleValue() { return resaleValue; }

    public ShopItem withStock(int newStock) {
        return new ShopItem(name, price, description, newStock, resaleValue);
    }
}
