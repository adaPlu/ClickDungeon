//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon.model;

public class CharacterProfile {
    private String name;
    private PlayerClass playerClass;
    private int maxHP;
    private int currentHP;
    private int level;
    private int xp;

    // New instance variables for attack and defense.
    private int attack;
    private int defense;
    // Add at the top
    private transient AnimatedPlayer animatedPlayer;

    public CharacterProfile(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
        this.level = 1;
        this.xp = 0;
        switch (playerClass) {
            case KNIGHT:
                this.maxHP = 10 * level;
                this.currentHP = maxHP;
                break;
            case THIEF:
                this.maxHP = 6 * level;
                this.currentHP = maxHP;
                break;
            case WIZARD:
                this.maxHP = 4 * level;
                this.currentHP = maxHP;
                break;
            default:
                this.maxHP = 10;
                this.currentHP = maxHP;
                break;
        }
        // Set initial attack and defense based on the player's class.
        switch (playerClass) {
            case KNIGHT:
                this.attack = level * 2;
                this.defense = level * 3;
                break;
            case THIEF:
                this.attack = level * 2 + 2;
                this.defense = level + 1;
                break;
            case WIZARD:
                this.attack = level * 2 + 1;
                this.defense = level;
                break;
            default:
                this.attack = level * 2;
                this.defense = level;
                break;
        }

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
        // Clamp currentHP between 0 and maxHP
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

    // Getter and setter for attack.
    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    // Getter and setter for defense.
    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    // Returns true if the player's current HP is zero or less.
    public boolean isDead() {
        return currentHP <= 0;
    }

    // Subtracts the specified damage from currentHP using the setter to clamp the value.
    public void takeDamage(int damageToPlayer) {
        setCurrentHP(currentHP - damageToPlayer);
    }

    // Adds experience points, and levels up the character if the xp threshold is reached.
    // Also increases maxHP, attack, and defense upon leveling up.
    public void addExperience(int xpReward) {
        xp += xpReward;
        int xpNeeded = level * 100; // Example threshold: level * 100 XP to level up.
        while (xp >= xpNeeded) {
            xp -= xpNeeded;
            level++;
            // Increase maxHP by 10 for each level up, and restore currentHP to new maxHP.
            maxHP += 10;
            currentHP = maxHP;
            // Increase attack and defense (adjust these increments as desired)
            attack += 2;
            defense += 1;
            xpNeeded = level * 100;
        }
    }
    // Add setter/getter
    public void setAnimatedPlayer(AnimatedPlayer animatedPlayer) {
        this.animatedPlayer = animatedPlayer;
    }

    public AnimatedPlayer getAnimatedPlayer() {
        return animatedPlayer;
    }
    public String getCharClass() {
        return playerClass.toString();
    }



}
