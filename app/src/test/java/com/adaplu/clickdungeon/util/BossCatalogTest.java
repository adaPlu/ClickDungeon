package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class BossCatalogTest {

    @Test
    public void isBossFloorRecognizesConfiguredAndNonConfiguredFloors() {
        assertTrue(BossCatalog.isBossFloor(11));
        assertTrue(BossCatalog.isBossFloor(22));
        assertTrue(BossCatalog.isBossFloor(99));
        assertFalse(BossCatalog.isBossFloor(1));
        assertFalse(BossCatalog.isBossFloor(12));
        assertFalse(BossCatalog.isBossFloor(100));
    }

    @Test
    public void createBossForFloorReturnsNullWhenFloorHasNoBoss() {
        assertNull(BossCatalog.createBossForFloor(3));
    }

    @Test
    public void createBossForFloorBuildsExpectedBossMetadata() {
        Monster lich = BossCatalog.createBossForFloor(11);
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

    @Test
    public void lateGameBossesScaleBeyondTheOpeningBoss() {
        Monster openingBoss = BossCatalog.createBossForFloor(11);
        Monster lateBoss = BossCatalog.createBossForFloor(99);

        assertNotNull(openingBoss);
        assertNotNull(lateBoss);
        assertTrue(lateBoss.getMaxHP() > openingBoss.getMaxHP());
        assertTrue(lateBoss.getAttack() >= openingBoss.getAttack());
        assertTrue(lateBoss.getDefense() >= openingBoss.getDefense());
        assertEquals(5, lateBoss.getBossPhaseCount());
    }
}
