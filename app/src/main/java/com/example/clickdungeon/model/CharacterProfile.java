//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon.model;

import com.example.clickdungeon.util.ItemCatalog;

public class CharacterProfile {
    public static final int MAX_LEVEL = 20;
    private static final int STAT_POINTS_PER_LEVEL = 2;
    private static final int HP_PER_CON = 2;
    private static final int HP_PER_LEVEL = 2;
    private static final int MP_PER_INT = 2;
    private static final int MP_PER_LEVEL = 2;
    private String name;
    private PlayerClass playerClass;
    private int maxHP;
    private int currentHP;
    private int maxMP;
    private int currentMP;
    private int level;
    private int xp;
    private int availableStatPoints;

    // New instance variables for attack and defense.
    private int strength;
    private int intelligence;
    private int constitution;
    private int dexterity;
    private String equippedWeaponName;
    private String equippedArmorName;
    // Add at the top
    private transient AnimatedPlayer animatedPlayer;
    private boolean usesMp;

    public CharacterProfile(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
        this.level = 1;
        this.xp = 0;
        this.availableStatPoints = 0;
        applyBaseStatsForClass(playerClass);
        recalculateDerivedStats(true);

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
        int desired = Math.max(1, maxHP);
        int base = desired - (level * 2);
        this.constitution = Math.max(0, (int) Math.ceil(base / 2.0));
        recalculateDerivedStats(true);
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        // Clamp currentHP between 0 and maxHP
        this.currentHP = Math.max(0, Math.min(getMaxHP(), currentHP));
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
        recalculateDerivedStats(false);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getAvailableStatPoints() {
        return availableStatPoints;
    }

    public boolean spendStatPoints(int points) {
        if (points <= 0 || points > availableStatPoints) {
            return false;
        }
        availableStatPoints -= points;
        return true;
    }

    public boolean increaseStrength(int points) {
        if (!spendStatPoints(points)) {
            return false;
        }
        strength += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseIntelligence(int points) {
        if (!spendStatPoints(points)) {
            return false;
        }
        intelligence += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseConstitution(int points) {
        if (!spendStatPoints(points)) {
            return false;
        }
        constitution += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseDexterity(int points) {
        if (!spendStatPoints(points)) {
            return false;
        }
        dexterity += points;
        recalculateDerivedStats(false);
        return true;
    }

    // Getter and setter for attack.
    public int getAttack() {
        return getPrimaryAttackStat();
    }

    public void setAttack(int attack) {
        setPrimaryAttackStat(Math.max(0, attack));
    }

    // Getter and setter for defense.
    public int getDefense() {
        return computeBaseDefense();
    }

    public int getBaseAttack() {
        return getAttack();
    }

    public int getBaseDefense() {
        return getDefense();
    }

    public String getEquippedWeaponName() {
        return equippedWeaponName;
    }

    public String getEquippedArmorName() {
        return equippedArmorName;
    }

    public void equipWeapon(String name) {
        this.equippedWeaponName = name;
    }

    public void equipArmor(String name) {
        this.equippedArmorName = name;
    }

    public void unequipWeapon() {
        this.equippedWeaponName = null;
    }

    public void unequipArmor() {
        this.equippedArmorName = null;
    }

    public int getAttackBonus() {
        return getBonusForItem(equippedWeaponName, true);
    }

    public int getDefenseBonus() {
        return getBonusForItem(equippedArmorName, false);
    }

    public int getTotalAttack() {
        return Math.max(1, getAttack() + getAttackBonus());
    }

    public int getTotalDefense() {
        return Math.max(0, getDefense() + getDefenseBonus());
    }

    private int getBonusForItem(String name, boolean attackBonus) {
        if (name == null) {
            return 0;
        }
        ItemDefinition definition = ItemCatalog.getItemDefinition(name);
        if (definition == null) {
            return 0;
        }
        return attackBonus ? definition.getAttackBonus() : definition.getDefenseBonus();
    }

    public void setDefense(int defense) {
        this.dexterity = Math.max(0, defense - level);
        recalculateDerivedStats(false);
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
        while (xp >= xpNeeded && level < MAX_LEVEL) {
            xp -= xpNeeded;
            level++;
            availableStatPoints += STAT_POINTS_PER_LEVEL;
            recalculateDerivedStats(true);
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

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = Math.max(0, strength);
        recalculateDerivedStats(false);
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = Math.max(0, intelligence);
        recalculateDerivedStats(false);
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = Math.max(0, constitution);
        recalculateDerivedStats(false);
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = Math.max(0, dexterity);
        recalculateDerivedStats(false);
    }

    public int getMaxMP() {
        return maxMP;
    }

    public int getCurrentMP() {
        return currentMP;
    }

    public void setCurrentMP(int currentMP) {
        this.currentMP = Math.max(0, Math.min(getMaxMP(), currentMP));
    }

    public boolean usesMp() {
        return usesMp;
    }

    private int getPrimaryAttackStat() {
        switch (playerClass) {
            case THIEF:
                return dexterity;
            case WIZARD:
                return intelligence;
            case KNIGHT:
            default:
                return strength;
        }
    }

    private void setPrimaryAttackStat(int value) {
        switch (playerClass) {
            case THIEF:
                dexterity = value;
                break;
            case WIZARD:
                intelligence = value;
                break;
            case KNIGHT:
            default:
                strength = value;
                break;
        }
        recalculateDerivedStats(false);
    }

    private int computeBaseDefense() {
        return Math.max(0, level + dexterity);
    }

    private void applyBaseStatsForClass(PlayerClass playerClass) {
        if (playerClass == null) {
            strength = 1;
            dexterity = 1;
            constitution = 1;
            intelligence = 1;
            usesMp = false;
            return;
        }
        strength = playerClass.getBaseStrength();
        dexterity = playerClass.getBaseDexterity();
        constitution = playerClass.getBaseConstitution();
        intelligence = playerClass.getBaseIntelligence();
        usesMp = playerClass.usesMp();
    }

    private void recalculateDerivedStats(boolean refillVitals) {
        int newMaxHp = Math.max(1, (constitution * HP_PER_CON) + (level * HP_PER_LEVEL));
        int newMaxMp = usesMp
                ? Math.max(0, (intelligence * MP_PER_INT) + (level * MP_PER_LEVEL))
                : 0;
        maxHP = newMaxHp;
        maxMP = newMaxMp;
        if (refillVitals) {
            currentHP = maxHP;
            currentMP = maxMP;
        } else {
            currentHP = Math.min(currentHP, maxHP);
            currentMP = Math.min(currentMP, maxMP);
        }
    }



}
