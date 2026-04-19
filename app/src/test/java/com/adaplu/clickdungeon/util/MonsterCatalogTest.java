package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MonsterCatalogTest {

    // -----------------------------------------------------------------------
    // Template existence and field values
    // -----------------------------------------------------------------------

    @Test
    public void allTemplatesAreNonNull() {
        assertNotNull(MonsterCatalog.SLIME);
        assertNotNull(MonsterCatalog.GOBLIN);
        assertNotNull(MonsterCatalog.SKELETON);
        assertNotNull(MonsterCatalog.ORC);
        assertNotNull(MonsterCatalog.TROLL);
        assertNotNull(MonsterCatalog.WITCH);
        assertNotNull(MonsterCatalog.VAMPIRE);
        assertNotNull(MonsterCatalog.DEMON);
        assertNotNull(MonsterCatalog.DRAGON);
        assertNotNull(MonsterCatalog.RAT);
        assertNotNull(MonsterCatalog.BAT);
        assertNotNull(MonsterCatalog.SPIDER);
        assertNotNull(MonsterCatalog.WOLF);
        assertNotNull(MonsterCatalog.BANDIT);
        assertNotNull(MonsterCatalog.CULTIST);
        assertNotNull(MonsterCatalog.WARLOCK);
        assertNotNull(MonsterCatalog.WRAITH);
        assertNotNull(MonsterCatalog.GOLEM);
        assertNotNull(MonsterCatalog.LICH);
        assertNotNull(MonsterCatalog.HELLHOUND);
        assertNotNull(MonsterCatalog.REVENANT);
        assertNotNull(MonsterCatalog.ARCHDEMON);
        assertNotNull(MonsterCatalog.ANCIENT_WYRM);
    }

    @Test
    public void slimeBaseStatsMatch() {
        MonsterCatalog.MonsterTemplate t = MonsterCatalog.SLIME;
        assertEquals("Slime", t.name);
        assertEquals(3, t.baseHp);
        assertEquals(1, t.baseAttack);
        assertEquals(0, t.baseDefense);
        assertEquals(MonsterFamily.ELEMENTAL, t.family);
        assertEquals(MonsterAffinity.POISON, t.affinity);
    }

    @Test
    public void ancientWyrmIsStrongest() {
        MonsterCatalog.MonsterTemplate t = MonsterCatalog.ANCIENT_WYRM;
        assertEquals("Ancient Wyrm", t.name);
        assertEquals(12, t.baseHp);
        assertEquals(7, t.baseAttack);
        assertEquals(5, t.baseDefense);
        assertEquals(MonsterFamily.DRACONIC, t.family);
        assertEquals(MonsterAffinity.FIRE, t.affinity);
    }

    @Test
    public void familyAndAffinityDefaultToSafeValuesWhenNull() {
        // MonsterTemplate constructor guards null family/affinity.
        MonsterCatalog.MonsterTemplate t = new MonsterCatalog.MonsterTemplate(
                "Test", "T", 5, 2, 1, null, null);
        assertEquals(MonsterFamily.UNKNOWN, t.family);
        assertEquals(MonsterAffinity.NONE, t.affinity);
    }

    // -----------------------------------------------------------------------
    // spawnForFloor — floor 1 baseline
    // -----------------------------------------------------------------------

    @Test
    public void spawnForFloor1ReturnsAtLeastBaseStats() {
        // Floor 1: scaling = 0, so hpBonus/attackBonus/defenseBonus start at 0.
        // GameBalance scale for floor 1 should be 1.0, difficulty NORMAL scale = 1.0.
        Random seeded = new Random(42);
        Monster m = MonsterCatalog.GOBLIN.spawnForFloor(seeded, 1, SettingsManager.Difficulty.NORMAL);
        assertNotNull(m);
        assertTrue("HP must be at least base HP", m.getMaxHP() >= MonsterCatalog.GOBLIN.baseHp);
        assertTrue("Attack must be >= 1", m.getAttack() >= 1);
        assertTrue("Defense must be >= 0", m.getDefense() >= 0);
        assertEquals("Goblin", m.getMonsterType());
        assertEquals(MonsterFamily.HUMANOID, m.getFamily());
        assertEquals(MonsterAffinity.NONE, m.getAffinity());
    }

    @Test
    public void spawnForFloorScalesStatsUpWithHigherFloors() {
        Random r1 = new Random(0);
        Random r10 = new Random(0);
        Monster floor1 = MonsterCatalog.GOBLIN.spawnForFloor(r1, 1, SettingsManager.Difficulty.NORMAL);
        Monster floor10 = MonsterCatalog.GOBLIN.spawnForFloor(r10, 10, SettingsManager.Difficulty.NORMAL);
        assertTrue("Floor 10 HP should exceed floor 1 HP", floor10.getMaxHP() > floor1.getMaxHP());
        assertTrue("Floor 10 attack should exceed floor 1 attack", floor10.getAttack() >= floor1.getAttack());
    }

    @Test
    public void spawnForFloorHpNeverDropsBelowOne() {
        Random r = new Random(0);
        Monster m = MonsterCatalog.RAT.spawnForFloor(r, 1, SettingsManager.Difficulty.NORMAL);
        assertTrue("HP must be at least 1", m.getMaxHP() >= 1);
        assertTrue("Attack must be at least 1", m.getAttack() >= 1);
    }

    // -----------------------------------------------------------------------
    // Difficulty scaling
    // -----------------------------------------------------------------------

    @Test
    public void casualDifficultyProducesWeakerMonstersOrEqual() {
        Random r1 = new Random(99);
        Random r2 = new Random(99);
        Monster casual = MonsterCatalog.ORC.spawnForFloor(r1, 5, SettingsManager.Difficulty.CASUAL);
        Monster normal = MonsterCatalog.ORC.spawnForFloor(r2, 5, SettingsManager.Difficulty.NORMAL);
        assertTrue("Casual HP <= Normal HP", casual.getMaxHP() <= normal.getMaxHP());
        assertTrue("Casual attack <= Normal attack", casual.getAttack() <= normal.getAttack());
    }

    @Test
    public void hardcoreDifficultyProducesStrongerMonsters() {
        Random r1 = new Random(7);
        Random r2 = new Random(7);
        Monster normal = MonsterCatalog.ORC.spawnForFloor(r1, 5, SettingsManager.Difficulty.NORMAL);
        Monster hardcore = MonsterCatalog.ORC.spawnForFloor(r2, 5, SettingsManager.Difficulty.HARDCORE);
        assertTrue("Hardcore HP >= Normal HP", hardcore.getMaxHP() >= normal.getMaxHP());
        assertTrue("Hardcore attack >= Normal attack", hardcore.getAttack() >= normal.getAttack());
    }

    @Test
    public void nullDifficultyDoesNotCrash() {
        Random r = new Random(0);
        Monster m = MonsterCatalog.GOBLIN.spawnForFloor(r, 3, null);
        assertNotNull(m);
        assertTrue(m.getMaxHP() >= 1);
    }
}
