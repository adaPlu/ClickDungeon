package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TileTest {

    @Test
    public void revealMarksTileVisible() {
        Tile tile = new Tile(TileType.EMPTY);
        assertFalse(tile.isRevealed());
        assertTrue(tile.isDirty());
        tile.reveal();
        assertTrue(tile.isRevealed());
        assertTrue(tile.isDirty());
    }

    @Test
    public void monsterStateTracksPresence() {
        Tile tile = new Tile(TileType.ENEMY);
        assertFalse(tile.hasMonster());

        Monster monster = new Monster("Slime", 3, 1, 0, "S");
        tile.setMonster(monster);
        assertTrue(tile.isDirty());
        assertTrue(tile.hasMonster());
        assertEquals(monster, tile.getMonster());

        tile.setDirty(false);
        tile.setMonster(null);
        assertTrue(tile.isDirty());
        assertFalse(tile.hasMonster());
    }

    @Test
    public void customNameDefaultsToNull() {
        Tile tile = new Tile(TileType.GOLD);
        assertNull(tile.getCustomName());
    }

    @Test
    public void setTypeMarksDirtyOnChange() {
        Tile tile = new Tile(TileType.EMPTY);
        tile.setDirty(false);
        tile.setType(TileType.EMPTY);
        assertFalse(tile.isDirty());
        tile.setType(TileType.GOLD);
        assertTrue(tile.isDirty());
    }

    @Test
    public void setHasPlayerMarksDirtyOnChange() {
        Tile tile = new Tile(TileType.EMPTY);
        tile.setDirty(false);
        tile.setHasPlayer(false);
        assertFalse(tile.isDirty());
        tile.setHasPlayer(true);
        assertTrue(tile.isDirty());
    }

    @Test
    public void cachedMonsterFieldsAreMutable() {
        Tile tile = new Tile(TileType.ENEMY);
        tile.setMonsterSpriteKey("slime_idle");
        tile.setCachedMonsterHp(2);
        tile.setCachedMonsterMaxHp(6);

        assertEquals("slime_idle", tile.getMonsterSpriteKey());
        assertEquals(2, tile.getCachedMonsterHp());
        assertEquals(6, tile.getCachedMonsterMaxHp());
    }
}
