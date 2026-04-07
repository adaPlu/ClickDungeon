package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
public class MonsterLootRollTest {

    @Test
    public void hasLootReturnsFalseForEmptyRoll() {
        MonsterLootRoll roll = new MonsterLootRoll(0, 0, Collections.emptyList(), Collections.emptyList());
        assertFalse(roll.hasLoot());
    }

    @Test
    public void hasLootReturnsTrueForGoldOrItems() {
        MonsterLootRoll goldRoll = new MonsterLootRoll(2, 0, Collections.emptyList(), Collections.emptyList());
        assertTrue(goldRoll.hasLoot());

        MonsterLootRoll itemRoll = new MonsterLootRoll(0, 0, Collections.singletonList("Potion"), Collections.emptyList());
        assertTrue(itemRoll.hasLoot());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void itemNamesListIsImmutable() {
        MonsterLootRoll roll = new MonsterLootRoll(0, 0, Collections.singletonList("Potion"), Collections.emptyList());
        roll.getItemNames().add("Elixir");
    }
}
