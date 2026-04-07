package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PricedItemTest {

    @Test
    public void exposesFieldsAndCreatesUpdatedQuantity() {
        PricedItem item = new PricedItem("Potion", 12, 3);

        assertEquals("Potion", item.getName());
        assertEquals(12, item.getPrice());
        assertEquals(3, item.getQuantity());

        PricedItem updated = item.withQuantity(7);
        assertEquals("Potion", updated.getName());
        assertEquals(12, updated.getPrice());
        assertEquals(7, updated.getQuantity());
    }
}
