package com.adaplu.clickdungeon.model;

/**
 * Represents an item with a price and quantity, used for shops and buyback.
 */
public class PricedItem {
    /** Item identifier or display name. */
    private final String name;
    /** Price per unit for buying or selling. */
    private final int price;
    /** Quantity available or owned. */
    private final int quantity;

    /**
     * Creates a priced item entry.
     *
     * @param name item identifier or display name
     * @param price price per unit
     * @param quantity stack size
     */
    public PricedItem(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /** Returns the item name. */
    public String getName() {
        return name;
    }

    /** Returns the price per unit. */
    public int getPrice() {
        return price;
    }

    /** Returns the quantity in this entry. */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns a new item instance with the updated quantity.
     */
    public PricedItem withQuantity(int newQuantity) {
        return new PricedItem(name, price, newQuantity);
    }
}
