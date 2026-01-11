package com.example.clickdungeon.model;

public class Monster {
    private int maxHP;
    private int currentHP;
    private int attack;
    private boolean hasRangedAttack;

    private String image;

    private int defense;

    private String monsterType;
    private MonsterFamily family;
    private MonsterAffinity affinity;

    public Monster(String monsterType, int maxHP, int attack,int defense, String image) {
        this.monsterType = monsterType;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.image = image;
        this.hasRangedAttack = false;
        this.family = MonsterFamily.UNKNOWN;
        this.affinity = MonsterAffinity.NONE;
    }

    public Monster(String monsterType,
                   int maxHP,
                   int attack,
                   int defense,
                   String image,
                   MonsterFamily family,
                   MonsterAffinity affinity) {
        this(monsterType, maxHP, attack, defense, image);
        this.family = family != null ? family : MonsterFamily.UNKNOWN;
        this.affinity = affinity != null ? affinity : MonsterAffinity.NONE;
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

    public boolean hasRangedAttack() {
        return hasRangedAttack;
    }

    public void setHasRangedAttack(boolean hasRangedAttack) {
        this.hasRangedAttack = hasRangedAttack;
    }

    public MonsterFamily getFamily() {
        return family != null ? family : MonsterFamily.UNKNOWN;
    }

    public void setFamily(MonsterFamily family) {
        this.family = family != null ? family : MonsterFamily.UNKNOWN;
    }

    public MonsterAffinity getAffinity() {
        return affinity != null ? affinity : MonsterAffinity.NONE;
    }

    public void setAffinity(MonsterAffinity affinity) {
        this.affinity = affinity != null ? affinity : MonsterAffinity.NONE;
    }

    public void setAttack(int attack) {
        this.attack = Math.max(1, attack);
    }

    public void setDefense(int defense) {
        this.defense = Math.max(0, defense);
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
