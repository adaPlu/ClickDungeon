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

    /**
     * Generates a randomized grid for a floor using fixed counts per tile type.
     *
     * @param gridSize width/height of the square grid
     * @param floor current floor index used to label the big key
     * @param monsterFactory factory for creating enemy instances
     * @return result bundle with grid, locked stair, key name, and safe tile count
     */
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
        for (int i = 0; i < 2; i++) {
            pool.add(new Tile(TileType.CHEST));
        }
        for (int i = 0; i < 2; i++) {
            pool.add(new Tile(TileType.SMALL_KEY));
        }
        pool.add(new Tile(TileType.TRAP_FIRE));
        pool.add(new Tile(TileType.TRAP_FIRE));
        pool.add(new Tile(TileType.TRAP_POISON));
        pool.add(new Tile(TileType.TRAP_POISON));
        pool.add(new Tile(TileType.TRAP_ACID));
        pool.add(new Tile(TileType.TRAP_FREEZE));
        pool.add(new Tile(TileType.TRAP_PITFALL));

        String keyName = getBigKeyNameForFloor(floor);
        TileType lockedStair = TileType.STAIR_DOWN_LOCKED;
        pool.add(new Tile(TileType.BIG_KEY, keyName));
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
                // Count tiles that are safe to reveal for victory tracking.
                if (tile.getType() != TileType.ENEMY) {
                    safeTiles++;
                }
            }
        }

        return new Result(grid, lockedStair, keyName, safeTiles);
    }

    /**
     * Functional interface used to provide a monster instance for enemy tiles.
     */
    public interface MonsterFactory {
        @NonNull
        Monster create();
    }

    /**
     * Builds the display name for the big key on the given floor.
     */
    public static String getBigKeyNameForFloor(int floor) {
        return "BIG KEY (F" + Math.max(1, floor) + ")";
    }

    /**
     * Container for generator outputs.
     */
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
