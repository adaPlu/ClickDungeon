package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ItemDefinitionTest {

    @Test
    public void exposesConstructorValues() {
        ItemDefinition item = new ItemDefinition(
                "Ember Blade",
                ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.RARE,
                ItemDefinition.EquipSlot.WEAPON,
                4,
                1,
                2,
                3,
                ItemDefinition.DamageType.FIRE,
                45
        );

        assertEquals("Ember Blade", item.getName());
        assertEquals(ItemDefinition.ItemType.WEAPON, item.getType());
        assertEquals(ItemDefinition.Rarity.RARE, item.getRarity());
        assertEquals(ItemDefinition.EquipSlot.WEAPON, item.getEquipSlot());
        assertEquals(4, item.getAttackBonus());
        assertEquals(1, item.getDefenseBonus());
        assertEquals(2, item.getHpBonus());
        assertEquals(3, item.getAbilityBonus());
        assertEquals(ItemDefinition.DamageType.FIRE, item.getDamageType());
        assertEquals(45, item.getResaleValue());
    }
}
