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

    @Test
    public void familyAndAffinityDefaultSafely() {
        Monster monster = new Monster("Slime", 3, 1, 0, "S");

        assertEquals(MonsterFamily.UNKNOWN, monster.getFamily());
        assertEquals(MonsterAffinity.NONE, monster.getAffinity());
    }

    @Test
    public void setAttackAndDefenseClampToValidRanges() {
        Monster monster = new Monster("Orc", 6, 2, 1, "O");

        monster.setAttack(0);
        monster.setDefense(-5);

        assertEquals(1, monster.getAttack());
        assertEquals(0, monster.getDefense());
    }

    @Test
    public void setFamilyAndAffinityHandleNull() {
        Monster monster = new Monster("Bat", 3, 1, 0, "b");

        monster.setFamily(null);
        monster.setAffinity(null);

        assertEquals(MonsterFamily.UNKNOWN, monster.getFamily());
        assertEquals(MonsterAffinity.NONE, monster.getAffinity());
    }
}
