package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShopItemTest {

    @Test
    public void withStockReturnsUpdatedCopy() {
        ShopItem item = new ShopItem("Potion", 50, "Heals", 3);

        ShopItem updated = item.withStock(1);

        assertEquals(3, item.getStock());
        assertEquals(1, updated.getStock());
        assertEquals(item.getName(), updated.getName());
        assertEquals(item.getPrice(), updated.getPrice());
        assertEquals(item.getDescription(), updated.getDescription());
    }

    @Test
    public void quantityAliasMatchesStock() {
        ShopItem item = new ShopItem("Key", 10, 2);
        assertEquals(item.getStock(), item.getQuantity());
    }
}
