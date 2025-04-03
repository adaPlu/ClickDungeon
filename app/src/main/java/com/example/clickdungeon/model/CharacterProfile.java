package com.example.clickdungeon.model;

import com.example.clickdungeon.model.PlayerClass;

public class CharacterProfile {
    private String name;
    private PlayerClass playerClass;
    private int level;
    private int currentHP;
    private int maxHP;
    private int xp;

    public CharacterProfile(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
        this.level = 1;
        this.xp = 0;

        switch (playerClass) {
            case KNIGHT:
                this.maxHP = 10;
                break;
            case THIEF:
                this.maxHP = 6;
                break;
            case WIZARD:
                this.maxHP = 4;
                break;
        }
        this.currentHP = this.maxHP;
    }

    public String getName() {
        return name;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getXp() {
        return xp;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
