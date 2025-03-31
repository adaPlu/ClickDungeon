package com.example.clickdungeon.model;

public class InventoryItem {
    private final String name;
    private final int quantity;

    public InventoryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
}
