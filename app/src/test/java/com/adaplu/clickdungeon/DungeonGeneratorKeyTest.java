package com.adaplu.clickdungeon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.adaplu.clickdungeon.util.DungeonGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DungeonGeneratorKeyTest {

    @Test
    public void testBigKeyConsistency() {
        int floor = 5;
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(5, floor, () -> null);
        
        boolean foundBigKey = false;
        boolean foundLockedStair = false;
        String expectedKeyName = DungeonGenerator.getBigKeyNameForFloor(floor);

        for (Tile[] row : result.grid) {
            for (Tile tile : row) {
                if (tile.getType() == TileType.BIG_KEY) {
                    foundBigKey = true;
                    assertNotNull("Big key should have a custom name", tile.getCustomName());
                    assertTrue("Key name should match floor pattern", tile.getCustomName().contains(String.valueOf(floor)));
                }
                if (tile.getType() == TileType.STAIR_DOWN_LOCKED) {
                    foundLockedStair = true;
                }
            }
        }
        
        if (result.lockedStair != null) {
            assertTrue("If stair is locked, big key must exist", foundBigKey);
            assertTrue("If result says locked, grid must have locked stair", foundLockedStair);
        }
    }
}
