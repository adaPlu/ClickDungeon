package com.adaplu.clickdungeon.model;

/**
 * Tile represents a single cell in the dungeon grid.
 * It holds metadata about its type (trap, gold, exit, etc.), whether it's revealed, 
 * and any entities (player or monster) currently occupying it.
 */
public class Tile {
    /** Primary tile type (trap, gold, exit, etc.). */
    private TileType type;
    /** True once the player has revealed this tile. */
    private boolean revealed;
    /** Monster occupying the tile, or null if empty. */
    private Monster monster;
    /** Optional custom name used for special tiles. */
    private String customName;
    /** True if the player is currently on this tile. */
    private boolean hasPlayer;
    
    // Cached state used for rendering optimizations and UI synchronization.
    /** Sprite key to avoid recalculating lookups when only HP changes. */
    private String monsterSpriteKey;
    /** Cached HP value for UI binding optimizations. */
    private int cachedMonsterHp;
    /** Cached max HP value for UI binding optimizations. */
    private int cachedMonsterMaxHp;
    
    /** 
     * dirty flag indicates that the tile's state has changed and its View 
     * should be rebound during the next render pass.
     */
    private boolean dirty = true;

    public Tile(TileType type) {
        this.type = type;
        this.monster = null; // null = no monster present on tile
        this.revealed = false;
        this.dirty = true;
    }

    public Tile(TileType type, Monster monster) {
        this.type = type;
        this.monster = monster;
        this.revealed = false;
        this.dirty = true;
    }

    public Tile(TileType type, String customName) {
        this.type = type;
        this.customName = customName;
        this.revealed = false;
        this.dirty = true;
    }

    public Tile() {
        this.dirty = true;
    }

    /** Returns whether the tile needs rebind in the UI. */
    public boolean isDirty() {
        return dirty;
    }

    /** Updates the dirty flag for render scheduling. */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /** Returns the current tile type. */
    public TileType getType() {
        return type;
    }

    /** Updates the tile type and marks dirty when changed. */
    public void setType(TileType type) {
        if (this.type != type) {
            this.type = type;
            this.dirty = true;
        }
    }

    /** Returns true if the tile is revealed. */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Marks the tile as explored by the player.
     */
    public void reveal() {
        if (!this.revealed) {
            this.revealed = true;
            this.dirty = true;
        }
    }

    /** Returns the monster occupying this tile, or null. */
    public Monster getMonster() {
        return monster;
    }

    /** Sets the monster on this tile and marks dirty. */
    public void setMonster(Monster monster) {
        this.monster = monster;
        this.dirty = true;
    }

    /** Returns the custom name, if any. */
    public String getCustomName() {
        return customName;
    }

    /** Updates the custom name and marks dirty when changed. */
    public void setCustomName(String customName) {
        if (this.customName == null || !this.customName.equals(customName)) {
            this.customName = customName;
            this.dirty = true;
        }
    }
    
    /** Returns true if a monster is present on this tile. */
    public boolean hasMonster() {
        return monster != null;
    }

    /** Returns true if the player is standing on this tile. */
    public boolean hasPlayer() {
        return hasPlayer;
    }

    /** Updates whether the player is on this tile and marks dirty. */
    public void setHasPlayer(boolean hasPlayer) {
        if (this.hasPlayer != hasPlayer) {
            this.hasPlayer = hasPlayer;
            this.dirty = true;
        }
    }

    /** Returns the cached monster sprite key for UI binding. */
    public String getMonsterSpriteKey() {
        return monsterSpriteKey;
    }

    /** Updates the cached monster sprite key. */
    public void setMonsterSpriteKey(String monsterSpriteKey) {
        this.monsterSpriteKey = monsterSpriteKey;
    }

    /** Returns cached monster HP used by the renderer. */
    public int getCachedMonsterHp() {
        return cachedMonsterHp;
    }

    /** Updates cached monster HP used by the renderer. */
    public void setCachedMonsterHp(int cachedMonsterHp) {
        this.cachedMonsterHp = cachedMonsterHp;
    }

    /** Returns cached monster max HP used by the renderer. */
    public int getCachedMonsterMaxHp() {
        return cachedMonsterMaxHp;
    }

    /** Updates cached monster max HP used by the renderer. */
    public void setCachedMonsterMaxHp(int cachedMonsterMaxHp) {
        this.cachedMonsterMaxHp = cachedMonsterMaxHp;
    }
}
