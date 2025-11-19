package com.example.clickdungeon.model;

public class ShopItem {
    private final String name;
    private final int price;

    public ShopItem(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
}
