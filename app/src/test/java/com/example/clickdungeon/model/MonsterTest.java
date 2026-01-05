package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MonsterTest {

    @Test
    public void takeDamageClampsAtZero() {
        Monster monster = new Monster("Slime", 5, 1, 0, "S");
        monster.takeDamage(10);

        assertEquals(0, monster.getCurrentHP());
        assertTrue(monster.isDead());
    }

    @Test
    public void isDeadReflectsCurrentHp() {
        Monster monster = new Monster("Goblin", 4, 2, 1, "G");
        assertFalse(monster.isDead());

        monster.takeDamage(4);
        assertTrue(monster.isDead());
    }
}
