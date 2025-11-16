package com.example.clickdungeon.util;

import androidx.annotation.NonNull;

import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds shuffled dungeon layouts so the generation rules can be validated via tests.
 */
public final class DungeonGenerator {

    private DungeonGenerator() {
    }

    public static Result generateFloor(int gridSize,
                                       int floor,
                                       @NonNull MonsterFactory monsterFactory) {
        List<Tile> pool = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            pool.add(new Tile(TileType.GOLD));
        }
        for (int i = 0; i < 5; i++) {
            pool.add(new Tile(TileType.ENEMY, monsterFactory.create()));
        }
        pool.add(new Tile(TileType.TRAP_FIRE));
        pool.add(new Tile(TileType.TRAP_FIRE));
        pool.add(new Tile(TileType.TRAP_POISON));
        pool.add(new Tile(TileType.TRAP_POISON));
        pool.add(new Tile(TileType.TRAP_ACID));
        pool.add(new Tile(TileType.TRAP_FREEZE));
        pool.add(new Tile(TileType.TRAP_PITFALL));

        TileType[] keyTypes = {TileType.RED_KEY, TileType.BLUE_KEY, TileType.GREEN_KEY};
        TileType[] lockTypes = {TileType.STAIR_DOWN_LOCKED_RED, TileType.STAIR_DOWN_LOCKED_BLUE, TileType.STAIR_DOWN_LOCKED_GREEN};
        int keyIndex = (floor - 1) % keyTypes.length;
        TileType selectedKeyType = keyTypes[keyIndex];
        TileType lockedStair = lockTypes[keyIndex];

        pool.add(new Tile(selectedKeyType));
        pool.add(new Tile(lockedStair));
        pool.add(new Tile(TileType.STAIR_DOWN));
        if (floor > 1) {
            pool.add(new Tile(TileType.STAIR_UP));
        }

        while (pool.size() < gridSize * gridSize) {
            pool.add(new Tile(TileType.EMPTY));
        }

        Collections.shuffle(pool);

        Tile[][] grid = new Tile[gridSize][gridSize];
        int index = 0;
        int safeTiles = 0;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Tile tile = pool.get(index++);
                grid[row][col] = tile;
                if (tile.getType() != TileType.ENEMY) {
                    safeTiles++;
                }
            }
        }

        String keyName = selectedKeyType.name().replace("_KEY", " Key (F" + floor + ")");

        return new Result(grid, lockedStair, keyName, safeTiles);
    }

    public interface MonsterFactory {
        @NonNull
        Monster create();
    }

    public static final class Result {
        public final Tile[][] grid;
        public final TileType lockedStair;
        public final String keyName;
        public final int safeTiles;

        Result(Tile[][] grid, TileType lockedStair, String keyName, int safeTiles) {
            this.grid = grid;
            this.lockedStair = lockedStair;
            this.keyName = keyName;
            this.safeTiles = safeTiles;
        }
    }
}
