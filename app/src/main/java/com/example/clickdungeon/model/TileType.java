package com.example.clickdungeon.model;

public enum TileType {
    EMPTY,
    GOLD,
    ENEMY,

    // Trap types
    TRAP_FIRE,
    TRAP_POISON,
    TRAP_ACID,
    TRAP_FREEZE,
    TRAP_PITFALL,

    // Stairs
    STAIR_UP,
    STAIR_DOWN,
    STAIR_DOWN_LOCKED_BLUE,
    STAIR_DOWN_LOCKED_GREEN,
    STAIR_DOWN_LOCKED_RED,

    // Keys
    RED_KEY,
    BLUE_KEY,
    GREEN_KEY
}
