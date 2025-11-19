package com.example.clickdungeon.model;

public class Monster {
    private int maxHP;
    private int currentHP;
    private int attack;

    private String image;

    private int defense;

    private String monsterType;

    public Monster(String monsterType, int maxHP, int attack,int defense, String image) {
        this.monsterType = monsterType;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.image = image;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public String getMonsterType() {
        return monsterType;
    }

    public String getImage() {
        return image;
    }

    public boolean isDead() {
        return currentHP <= 0;
    }

    public void takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP < 0) {
            currentHP = 0;
        }
    }
}
