//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon.model;

import com.example.clickdungeon.util.ItemCatalog;

/**
 * CharacterProfile manages the persistent state of the player's character.
 * It tracks core attributes (STR, DEX, CON, INT), equipment, experience, and derived vitals (HP, MP).
 * It also handles level-up logic and equip slot validation.
 */
public class CharacterProfile {
    /** Maximum allowed level for any character. */
    public static final int MAX_LEVEL = 20;
    /** Stat points granted per level-up. */
    private static final int STAT_POINTS_PER_LEVEL = 2;
    /** HP contribution per point of constitution. */
    private static final int HP_PER_CON = 2;
    /** HP contribution per character level. */
    private static final int HP_PER_LEVEL = 2;
    /** MP contribution per point of intelligence. */
    private static final int MP_PER_INT = 2;
    /** MP contribution per character level. */
    private static final int MP_PER_LEVEL = 2;
    
    /** Player-facing name used in UI. */
    private String name;
    /** Hero class that controls base stats and primary scaling. */
    private PlayerClass playerClass;
    /** Maximum HP derived from stats/level. */
    private int maxHP;
    /** Current HP value clamped to maxHP. */
    private int currentHP;
    /** Maximum MP derived from stats/level. */
    private int maxMP;
    /** Current MP value clamped to maxMP. */
    private int currentMP;
    /** Current hero level. */
    private int level;
    /** XP progress toward the next level. */
    private int xp;
    /** Unspent stat points available for allocation. */
    private int availableStatPoints;

    // Core attributes impacting combat performance.
    private int strength;
    private int intelligence;
    private int constitution;
    private int dexterity;
    
    // Identifier strings for currently equipped gear.
    private String equippedWeaponName;
    private String equippedArmorName;
    
    // Non-persistent controller for player sprite animations.
    private transient AnimatedPlayer animatedPlayer;
    private boolean usesMp;

    /**
     * Initializes a new character with base stats defined by their class.
     */
    public CharacterProfile(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass;
        this.level = 1;
        this.xp = 0;
        this.availableStatPoints = 0;
        applyBaseStatsForClass(playerClass);
        recalculateDerivedStats(true);
    }

    /** Returns the player-visible name for this character. */
    public String getName() {
        return name;
    }

    /** Updates the player-visible name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the selected class for this character. */
    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    /** Updates the character class (used for stat scaling). */
    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    /** Returns the maximum HP based on stats and level. */
    public int getMaxHP() {
        return maxHP;
    }

    /** Sets max HP directly and derives constitution from the new value. */
    public void setMaxHP(int maxHP) {
        int desired = Math.max(1, maxHP);
        int base = desired - (level * 2);
        this.constitution = Math.max(0, (int) Math.ceil(base / 2.0));
        recalculateDerivedStats(true);
    }

    /** Returns the current HP value. */
    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        // Clamp currentHP between 0 and maxHP.
        this.currentHP = Math.max(0, Math.min(getMaxHP(), currentHP));
    }

    /** Returns the current level. */
    public int getLevel() {
        return level;
    }

    /** Forces a level update and triggers stat recalculation. */
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
        recalculateDerivedStats(false);
    }

    /** Returns the current XP toward the next level. */
    public int getXp() {
        return xp;
    }

    /** Updates XP without triggering level-up processing. */
    public void setXp(int xp) {
        this.xp = xp;
    }

    /** Returns the number of unspent stat points. */
    public int getAvailableStatPoints() {
        return availableStatPoints;
    }

    /** Sets the available stat point pool, clamped to non-negative. */
    public void setAvailableStatPoints(int points) {
        availableStatPoints = Math.max(0, points);
    }

    /** Decrements stat point pool if enough points remain. */
    public boolean spendStatPoints(int points) {
        if (points <= 0 || points > availableStatPoints) {
            return false;
        }
        availableStatPoints -= points;
        return true;
    }

    // Attribute incrementers called from the UI.
    
    public boolean increaseStrength(int points) {
        if (!spendStatPoints(points)) return false;
        strength += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseIntelligence(int points) {
        if (!spendStatPoints(points)) return false;
        intelligence += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseConstitution(int points) {
        if (!spendStatPoints(points)) return false;
        constitution += points;
        recalculateDerivedStats(false);
        return true;
    }

    public boolean increaseDexterity(int points) {
        if (!spendStatPoints(points)) return false;
        dexterity += points;
        recalculateDerivedStats(false);
        return true;
    }

    /** Gets the current raw attack value based on class primary stat. */
    public int getAttack() {
        return getPrimaryAttackStat();
    }

    public void setAttack(int attack) {
        setPrimaryAttackStat(Math.max(0, attack));
    }

    /** Returns the raw base defense value. */
    public int getDefense() {
        return computeBaseDefense();
    }

    /** Returns the base attack value (before gear bonuses). */
    public int getBaseAttack() {
        return getAttack();
    }

    /** Returns the base defense value (before gear bonuses). */
    public int getBaseDefense() {
        return getDefense();
    }

    /** Returns the equipped weapon name or null. */
    public String getEquippedWeaponName() {
        return equippedWeaponName;
    }

    /** Returns the equipped armor name or null. */
    public String getEquippedArmorName() {
        return equippedArmorName;
    }

    /** Sets the weapon slot to the provided item name. */
    public void equipWeapon(String name) {
        this.equippedWeaponName = name;
    }

    /** Sets the armor slot to the provided item name. */
    public void equipArmor(String name) {
        this.equippedArmorName = name;
    }

    /** Clears the weapon slot. */
    public void unequipWeapon() {
        this.equippedWeaponName = null;
    }

    /** Clears the armor slot. */
    public void unequipArmor() {
        this.equippedArmorName = null;
    }

    /** Returns the flat attack bonus provided by the equipped weapon. */
    public int getAttackBonus() {
        return getBonusForItem(equippedWeaponName, true);
    }

    /** Returns the flat defense bonus provided by the equipped armor. */
    public int getDefenseBonus() {
        return getBonusForItem(equippedArmorName, false);
    }

    /** Returns the final effective attack power (base + gear). */
    public int getTotalAttack() {
        return Math.max(1, getAttack() + getAttackBonus());
    }

    /** Returns the final effective defense value (base + gear). */
    public int getTotalDefense() {
        return Math.max(0, getDefense() + getDefenseBonus());
    }

    /** Lookups item definitions to retrieve specific stat bonuses. */
    private int getBonusForItem(String name, boolean attackBonus) {
        if (name == null) return 0;
        ItemDefinition definition = ItemCatalog.getItemDefinition(name);
        if (definition == null) return 0;
        return attackBonus ? definition.getAttackBonus() : definition.getDefenseBonus();
    }

    /** Sets defense by adjusting dexterity to match the requested value. */
    public void setDefense(int defense) {
        this.dexterity = Math.max(0, defense - level);
        recalculateDerivedStats(false);
    }

    /** Returns true if current HP is zero. */
    public boolean isDead() {
        return currentHP <= 0;
    }

    /** Reduces current HP by the specified amount. */
    public void takeDamage(int damageToPlayer) {
        setCurrentHP(currentHP - damageToPlayer);
    }

    /**
     * Awards experience and checks for level transitions.
     * Each level adds stat points and refreshes vitals.
     */
    public void addExperience(int xpReward) {
        xp += xpReward;
        int xpNeeded = level * 100; // Linear scale: Level 1 needs 100, Level 2 needs 200, etc.
        while (xp >= xpNeeded && level < MAX_LEVEL) {
            xp -= xpNeeded;
            level++;
            availableStatPoints += STAT_POINTS_PER_LEVEL;
            recalculateDerivedStats(true);
            xpNeeded = level * 100;
        }
    }

    /** Attaches an animation controller instance (non-persistent). */
    public void setAnimatedPlayer(AnimatedPlayer animatedPlayer) {
        this.animatedPlayer = animatedPlayer;
    }

    /** Returns the attached animation controller instance. */
    public AnimatedPlayer getAnimatedPlayer() {
        return animatedPlayer;
    }
    
    /** Returns the class name for display purposes. */
    public String getCharClass() {
        return playerClass != null ? playerClass.toString() : "Unknown";
    }

    /** Returns the current strength attribute. */
    public int getStrength() {
        return strength;
    }

    /** Updates strength and recalculates derived stats. */
    public void setStrength(int strength) {
        this.strength = Math.max(0, strength);
        recalculateDerivedStats(false);
    }

    /** Returns the current intelligence attribute. */
    public int getIntelligence() {
        return intelligence;
    }

    /** Updates intelligence and recalculates derived stats. */
    public void setIntelligence(int intelligence) {
        this.intelligence = Math.max(0, intelligence);
        recalculateDerivedStats(false);
    }

    /** Returns the current constitution attribute. */
    public int getConstitution() {
        return constitution;
    }

    /** Updates constitution and recalculates derived stats. */
    public void setConstitution(int constitution) {
        this.constitution = Math.max(0, constitution);
        recalculateDerivedStats(false);
    }

    /** Returns the current dexterity attribute. */
    public int getDexterity() {
        return dexterity;
    }

    /** Updates dexterity and recalculates derived stats. */
    public void setDexterity(int dexterity) {
        this.dexterity = Math.max(0, dexterity);
        recalculateDerivedStats(false);
    }

    /** Returns the maximum MP based on stats and level. */
    public int getMaxMP() {
        return maxMP;
    }

    /** Returns the current MP value. */
    public int getCurrentMP() {
        return currentMP;
    }

    /** Sets current MP clamped between 0 and maxMP. */
    public void setCurrentMP(int currentMP) {
        this.currentMP = Math.max(0, Math.min(getMaxMP(), currentMP));
    }

    /** Returns whether this class uses MP at all. */
    public boolean usesMp() {
        return usesMp;
    }

    /** Returns the attribute used for base attack power calculation. */
    private int getPrimaryAttackStat() {
        if (playerClass == null) return strength;
        switch (playerClass) {
            case THIEF: return dexterity;
            case WIZARD: return intelligence;
            case KNIGHT:
            default: return strength;
        }
    }

    /** Updates the attribute used for base attack power. */
    private void setPrimaryAttackStat(int value) {
        if (playerClass == null) {
            strength = value;
        } else {
            switch (playerClass) {
                case THIEF: dexterity = value; break;
                case WIZARD: intelligence = value; break;
                case KNIGHT:
                default: strength = value; break;
            }
        }
        recalculateDerivedStats(false);
    }

    /** Base defense formula: Level + Dexterity. */
    private int computeBaseDefense() {
        return Math.max(0, level + dexterity);
    }

    /** Applies starting values from the Hero Class definition. */
    private void applyBaseStatsForClass(PlayerClass playerClass) {
        if (playerClass == null) {
            strength = 1; dexterity = 1; constitution = 1; intelligence = 1;
            usesMp = false;
            return;
        }
        strength = playerClass.getBaseStrength();
        dexterity = playerClass.getBaseDexterity();
        constitution = playerClass.getBaseConstitution();
        intelligence = playerClass.getBaseIntelligence();
        usesMp = playerClass.usesMp();
    }

    /**
     * Recalculates HP and MP maximums based on level and core attributes.
     * @param refillVitals If true, current HP/MP are restored to full.
     */
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
