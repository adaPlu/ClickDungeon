package com.example.clickdungeon.model;

public class Tile {
    private TileType type;
    private boolean revealed;

    public Tile(TileType type) {
        this.type = type;
        this.revealed = false;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public void reveal() {
        this.revealed = true;
    }
}
