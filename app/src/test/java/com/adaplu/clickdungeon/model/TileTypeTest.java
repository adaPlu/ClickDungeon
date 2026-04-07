package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TileTypeTest {

    @Test
    public void includesExpectedTrapAndKeyTypes() {
        assertNotNull(TileType.valueOf("TRAP_FIRE"));
        assertNotNull(TileType.valueOf("TRAP_FREEZE"));
        assertNotNull(TileType.valueOf("SMALL_KEY"));
        assertNotNull(TileType.valueOf("BIG_KEY"));
        assertNotNull(TileType.valueOf("STAIR_DOWN_LOCKED"));
    }

    @Test
    public void valueOfMatchesEnumConstant() {
        assertEquals(TileType.CHEST, TileType.valueOf("CHEST"));
    }
}
