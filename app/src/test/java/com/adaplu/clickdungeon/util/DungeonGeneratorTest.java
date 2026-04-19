package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;

import org.junit.Test;

public class DungeonGeneratorTest {

    @Test
    public void floorGenerationProducesExpectedCounts() {
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(
                5, 2, () -> new Monster("Goblin", 8, 3, 1, "G"));

        assertNotNull(result.grid);
        assertEquals(TileType.STAIR_DOWN_LOCKED, result.lockedStair);
        assertEquals(DungeonGenerator.getBigKeyNameForFloor(2), result.keyName);

        int enemies = 0;
        int gold = 0;
        int boostHealth = 0;
        int boostAttack = 0;
        int boostDefense = 0;
        int traps = 0;
        int empty = 0;
        for (Tile[] row : result.grid) {
            for (Tile tile : row) {
                if (tile.getType() == TileType.ENEMY) {
                    enemies++;
                    assertTrue(tile.hasMonster());
                }
                if (tile.getType() == TileType.GOLD) {
                    gold++;
                }
                if (tile.getType() == TileType.BOOST_HEALTH) {
                    boostHealth++;
                }
                if (tile.getType() == TileType.BOOST_ATTACK) {
                    boostAttack++;
                }
                if (tile.getType() == TileType.BOOST_DEFENSE) {
                    boostDefense++;
                }
                if (tile.getType().name().startsWith("TRAP_")) {
                    traps++;
                }
                if (tile.getType() == TileType.EMPTY) {
                    empty++;
                }
            }
        }

        assertEquals(4, enemies);
        assertEquals(4, gold);
        assertEquals(1, boostHealth);
        assertEquals(1, boostAttack);
        assertEquals(1, boostDefense);
        assertEquals(6, traps);
        assertEquals(0, empty);
        assertEquals(21, result.safeTiles);
    }

    @Test
    public void floorOneLeavesRoomForAnEmptyTileAfterGuaranteedPieces() {
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(
                5, 1, () -> new Monster("Goblin", 8, 3, 1, "G"));

        int empty = 0;
        for (Tile[] row : result.grid) {
            for (Tile tile : row) {
                if (tile.getType() == TileType.EMPTY) {
                    empty++;
                }
            }
        }

        assertEquals(1, empty);
    }
}
