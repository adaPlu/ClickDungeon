package com.example.clickdungeon.model;

public class Tile {
    private TileType type;
    private boolean revealed;
    private Monster monster;
    private String customName;

    public Tile(TileType type) {
        this.type = type;
        this.revealed = false;
    }

    public Tile(TileType type, Monster monster) {
        this.type = type;
        this.monster = monster;
        this.revealed = false;
    }
    public Tile(Monster monster) {
        this.type = TileType.ENEMY;
        this.monster = monster;
        this.revealed = false;
    }

    public Tile(TileType type, String customName) {
        this.type = type;
        this.customName = customName;
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

    public void reveal() {
        this.revealed = true;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public String getCustomName() {
        return customName;
    }
}
