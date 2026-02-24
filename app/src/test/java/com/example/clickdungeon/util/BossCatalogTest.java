package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.MonsterAffinity;
import com.example.clickdungeon.model.MonsterFamily;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BossCatalogTest {

    @Test
    public void isBossFloorRecognizesConfiguredAndNonConfiguredFloors() {
        assertTrue(BossCatalog.isBossFloor(5));
        assertTrue(BossCatalog.isBossFloor(10));
        assertTrue(BossCatalog.isBossFloor(15));
        assertFalse(BossCatalog.isBossFloor(1));
        assertFalse(BossCatalog.isBossFloor(16));
    }

    @Test
    public void createBossForFloorReturnsNullWhenFloorHasNoBoss() {
        assertNull(BossCatalog.createBossForFloor(3));
    }

    @Test
    public void createBossForFloorBuildsExpectedBossMetadata() {
        Monster lich = BossCatalog.createBossForFloor(5);
        assertNotNull(lich);
        assertEquals("Lich", lich.getMonsterType());
        assertEquals(14, lich.getMaxHP());
        assertEquals(6, lich.getAttack());
        assertEquals(4, lich.getDefense());
        assertTrue(lich.isBoss());
        assertEquals(2, lich.getBossPhaseCount());
        assertEquals(MonsterFamily.UNDEAD, lich.getFamily());
        assertEquals(MonsterAffinity.ARCANE, lich.getAffinity());
    }
}
