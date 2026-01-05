package com.example.clickdungeon.model;

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
        tile.reveal();
        assertTrue(tile.isRevealed());
    }

    @Test
    public void monsterStateTracksPresence() {
        Tile tile = new Tile(TileType.ENEMY);
        assertFalse(tile.hasMonster());

        Monster monster = new Monster("Slime", 3, 1, 0, "S");
        tile.setMonster(monster);
        assertTrue(tile.hasMonster());
        assertEquals(monster, tile.getMonster());

        tile.setMonster(null);
        assertFalse(tile.hasMonster());
    }

    @Test
    public void customNameDefaultsToNull() {
        Tile tile = new Tile(TileType.GOLD);
        assertNull(tile.getCustomName());
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
