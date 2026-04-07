package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.adaplu.clickdungeon.model.ItemDefinition;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Random;

@RunWith(RobolectricTestRunner.class)
public class ItemCatalogTest {

    @Test
    public void getItemDefinitionReturnsBaseItem() {
        ItemDefinition item = ItemCatalog.getItemDefinition("Iron Sword");
        assertNotNull(item);
        assertEquals(ItemDefinition.ItemType.WEAPON, item.getType());
        assertEquals(ItemDefinition.Rarity.UNCOMMON, item.getRarity());
        assertTrue(item.getAttackBonus() >= 2);
    }

    @Test
    public void randomMagicItemReturnsParsableDefinition() {
        ItemDefinition magicItem = ItemCatalog.randomMagicItem(new Random(42));
        assertNotNull(magicItem);
        assertEquals(ItemDefinition.ItemType.MAGIC, magicItem.getType());
        ItemDefinition parsed = ItemCatalog.getItemDefinition(magicItem.getName());
        assertNotNull(parsed);
        assertEquals(ItemDefinition.ItemType.MAGIC, parsed.getType());
    }
}
