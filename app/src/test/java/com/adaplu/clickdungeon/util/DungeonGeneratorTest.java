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
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(5, 2, () -> new Monster("Goblin", 8, 3, 1, "👺"));

        assertNotNull(result.grid);
        assertEquals(TileType.STAIR_DOWN_LOCKED, result.lockedStair);
        assertEquals(DungeonGenerator.getBigKeyNameForFloor(2), result.keyName);

        int enemies = 0;
        int gold = 0;
        for (Tile[] row : result.grid) {
            for (Tile tile : row) {
                if (tile.getType() == TileType.ENEMY) {
                    enemies++;
                    assertTrue(tile.hasMonster());
                }
                if (tile.getType() == TileType.GOLD) {
                    gold++;
                }
            }
        }

        assertEquals(5, enemies);
        assertEquals(5, gold);
        assertTrue(result.safeTiles > enemies);
    }
}
