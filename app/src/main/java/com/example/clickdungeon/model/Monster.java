package com.example.clickdungeon.model;

public class Monster {
    private String name;
    private int maxHP;
    private int currentHP;
    private int attack;

    private String image;

    public Monster(String name, int maxHP, int attack, String image) {
        this.name = name;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int hp) {
        this.currentHP = hp;
    }

    public int getAttack() {
        return attack;
    }
}
