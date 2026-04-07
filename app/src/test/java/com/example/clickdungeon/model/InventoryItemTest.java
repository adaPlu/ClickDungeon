package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InventoryItemTest {

    @Test
    public void gettersExposeConstructorValues() {
        InventoryItem item = new InventoryItem("Elixir", 4);

        assertEquals("Elixir", item.getName());
        assertEquals(4, item.getQuantity());
    }
}
