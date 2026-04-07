package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TerrainTypeTest {

    @Test
    public void fromNameMatchesEnumAndDisplayName() {
        assertEquals(TerrainType.CRYPT, TerrainType.fromName("crypt"));
        assertEquals(TerrainType.LAVA_FIELD, TerrainType.fromName("Lava Field"));
    }

    @Test
    public void fromNameReturnsNullForEmptyOrUnknown() {
        assertNull(TerrainType.fromName(null));
        assertNull(TerrainType.fromName(""));
        assertNull(TerrainType.fromName("Unknown Terrain"));
    }
}
