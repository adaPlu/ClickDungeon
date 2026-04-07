package com.adaplu.clickdungeon.model;

/**
 * Represents a stackable inventory entry for a named item.
 */
public class InventoryItem {
    /** Item identifier or display name. */
    private final String name;
    /** Quantity of the item stack. */
    private final int quantity;

    /**
     * Creates an immutable inventory stack.
     *
     * @param name item identifier or display name
     * @param quantity number of items in the stack
     */
    public InventoryItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    /** Returns the item name for this stack. */
    public String getName() {
        return name;
    }

    /** Returns the number of items in the stack. */
    public int getQuantity() {
        return quantity;
    }
}
