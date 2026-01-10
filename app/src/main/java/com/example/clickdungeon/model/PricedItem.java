package com.example.clickdungeon.model;

public class PricedItem {
    private final String name;
    private final int price;
    private final int quantity;

    public PricedItem(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public PricedItem withQuantity(int newQuantity) {
        return new PricedItem(name, price, newQuantity);
    }
}
