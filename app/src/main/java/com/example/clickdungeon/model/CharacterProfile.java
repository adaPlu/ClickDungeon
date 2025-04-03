// Latest version of CharacterProfile.java
package com.example.clickdungeon.model;

public class CharacterProfile {
    private String name;
    private PlayerClass playerClass;
    private int maxHP;
    private int currentHP;
    private int level;
    private int xp;

    public CharacterProfile(String name, PlayerClass playerClass, int maxHP) {
        this.name = name;
        this.playerClass = playerClass;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.level = 1;
        this.xp = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = Math.max(0, Math.min(maxHP, currentHP));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }
}
