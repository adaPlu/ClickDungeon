//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.adaplu.clickdungeon.model;

import com.adaplu.clickdungeon.util.ItemCatalog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CharacterProfile stores the persistent hero state for the simplified progression model.
 * Combat stats are limited to HP, ATK, and DEF. Class controls starting HP and class abilities,
 * while persistent boosts and equipment determine the rest of the hero's power.
 */
public class CharacterProfile {
    private static final int BASE_ATTACK = 2;
    private static final int BASE_DEFENSE = 1;
    private static final int MAX_ABILITY_CHARGES = 3;

    private String name;
    private PlayerClass playerClass;

    private int currentHP;
    private int healthBoost;
    private int attackBoost;
    private int defenseBoost;

    private String equippedWeaponName;
    private String equippedArmorName;

    private Map<String, ClassProgress> classProgressByName;

    private transient AnimatedPlayer animatedPlayer;

    public CharacterProfile(String name, PlayerClass playerClass) {
        this.name = name;
        this.playerClass = playerClass != null ? playerClass : PlayerClass.KNIGHT;
        this.classProgressByName = new HashMap<>();
        ensureAllClassProgress();
        this.currentHP = getMaxHP();
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
        PlayerClass nextClass = playerClass != null ? playerClass : PlayerClass.KNIGHT;
        if (this.playerClass == nextClass) {
            return;
        }
        int oldMaxHp = getMaxHP();
        float hpRatio = oldMaxHp > 0 ? currentHP / (float) oldMaxHp : 1f;
        this.playerClass = nextClass;
        ensureAllClassProgress();
        setCurrentHP(Math.max(1, Math.round(getMaxHP() * hpRatio)));
    }

    public int getMaxHP() {
        return Math.max(1, getBaseStartingHealth() + healthBoost);
    }

    public void setMaxHP(int maxHP) {
        healthBoost = Math.max(0, maxHP - getBaseStartingHealth());
        setCurrentHP(Math.min(currentHP, getMaxHP()));
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = Math.max(0, Math.min(getMaxHP(), currentHP));
    }

    public int getAttack() {
        return Math.max(1, BASE_ATTACK + attackBoost);
    }

    public void setAttack(int attack) {
        attackBoost = Math.max(0, attack - BASE_ATTACK);
    }

    public int getDefense() {
        return Math.max(0, BASE_DEFENSE + defenseBoost);
    }

    public void setDefense(int defense) {
        defenseBoost = Math.max(0, defense - BASE_DEFENSE);
    }

    public int getBaseAttack() {
        return getAttack();
    }

    public int getBaseDefense() {
        return getDefense();
    }

    public int getTotalAttack() {
        return Math.max(1, getAttack() + getAttackBonus());
    }

    public int getTotalDefense() {
        return Math.max(0, getDefense() + getDefenseBonus());
    }

    public int getHealthBoost() {
        return Math.max(0, healthBoost);
    }

    public int getAttackBoost() {
        return Math.max(0, attackBoost);
    }

    public int getDefenseBoost() {
        return Math.max(0, defenseBoost);
    }

    public void addHealthBoost(int amount) {
        if (amount <= 0) {
            return;
        }
        int oldMaxHp = getMaxHP();
        healthBoost += amount;
        int missingHp = Math.max(0, oldMaxHp - currentHP);
        setCurrentHP(Math.max(0, getMaxHP() - missingHp));
    }

    public void addAttackBoost(int amount) {
        if (amount > 0) {
            attackBoost += amount;
        }
    }

    public void addDefenseBoost(int amount) {
        if (amount > 0) {
            defenseBoost += amount;
        }
    }

    public String getEquippedWeaponName() {
        return equippedWeaponName;
    }

    public String getEquippedArmorName() {
        return equippedArmorName;
    }

    public void equipWeapon(String name) {
        equippedWeaponName = name;
    }

    public void equipArmor(String name) {
        equippedArmorName = name;
    }

    public void unequipWeapon() {
        equippedWeaponName = null;
    }

    public void unequipArmor() {
        equippedArmorName = null;
    }

    public int getAttackBonus() {
        return getBonusForItem(equippedWeaponName, true);
    }

    public int getDefenseBonus() {
        return getBonusForItem(equippedArmorName, false);
    }

    public boolean isDead() {
        return currentHP <= 0;
    }

    public void takeDamage(int damageToPlayer) {
        setCurrentHP(currentHP - damageToPlayer);
    }

    public void heal(int amount) {
        if (amount > 0) {
            setCurrentHP(currentHP + amount);
        }
    }

    public void addExperience(int xpReward) {
        addClassXp(playerClass, xpReward);
    }

    public void addClassXp(PlayerClass playerClass, int xpReward) {
        if (playerClass == null || xpReward <= 0) {
            return;
        }
        ClassProgress progress = getOrCreateProgress(playerClass);
        progress.earnedXp += xpReward;
        ensureStarterAbilityUnlocked(playerClass, progress);
    }

    public int getXp() {
        return getCurrentClassXp();
    }

    public void setXp(int xp) {
        ClassProgress progress = getOrCreateProgress(playerClass);
        progress.earnedXp = Math.max(0, xp);
        progress.spentXp = 0;
        ensureStarterAbilityUnlocked(playerClass, progress);
    }

    public int getCurrentClassXp() {
        return getClassXp(playerClass);
    }

    public int getClassXp(PlayerClass playerClass) {
        if (playerClass == null) {
            return 0;
        }
        ClassProgress progress = getOrCreateProgress(playerClass);
        return Math.max(0, progress.earnedXp - progress.spentXp);
    }

    public int getClassXpEarned(PlayerClass playerClass) {
        if (playerClass == null) {
            return 0;
        }
        return getOrCreateProgress(playerClass).earnedXp;
    }

    public boolean unlockAbility(PlayerClass playerClass, String abilityName) {
        PlayerClass.AbilityDefinition definition = playerClass != null
                ? playerClass.findAbility(abilityName)
                : null;
        if (definition == null) {
            return false;
        }
        ClassProgress progress = getOrCreateProgress(playerClass);
        ensureStarterAbilityUnlocked(playerClass, progress);
        if (progress.unlockedAbilityNames.contains(definition.getName())) {
            return true;
        }
        int cost = Math.max(0, definition.getUnlockXpCost());
        if (getClassXp(playerClass) < cost) {
            return false;
        }
        progress.spentXp += cost;
        progress.unlockedAbilityNames.add(definition.getName());
        progress.ensureAbilityState(definition.getName());
        return true;
    }

    public boolean isAbilityUnlocked(PlayerClass playerClass, String abilityName) {
        if (playerClass == null) {
            return false;
        }
        ClassProgress progress = getOrCreateProgress(playerClass);
        ensureStarterAbilityUnlocked(playerClass, progress);
        return progress.unlockedAbilityNames.contains(abilityName);
    }

    public PlayerClass.AbilityDefinition[] getUnlockedAbilities(PlayerClass playerClass) {
        if (playerClass == null) {
            return new PlayerClass.AbilityDefinition[0];
        }
        ensureStarterAbilityUnlocked(playerClass, getOrCreateProgress(playerClass));
        java.util.List<PlayerClass.AbilityDefinition> unlocked = new java.util.ArrayList<>();
        for (PlayerClass.AbilityDefinition ability : playerClass.getAbilityProgression()) {
            if (isAbilityUnlocked(playerClass, ability.getName())) {
                unlocked.add(ability);
            }
        }
        return unlocked.toArray(new PlayerClass.AbilityDefinition[0]);
    }

    public int getAbilityCharges(PlayerClass playerClass, String abilityName, long nowMillis) {
        AbilityChargeState state = getAbilityState(playerClass, abilityName);
        PlayerClass.AbilityDefinition definition = playerClass != null
                ? playerClass.findAbility(abilityName)
                : null;
        if (state == null || definition == null) {
            return 0;
        }
        syncAbilityCharges(state, definition, nowMillis);
        return state.charges;
    }

    public long getAbilityRechargeRemainingMillis(PlayerClass playerClass,
                                                  String abilityName,
                                                  long nowMillis) {
        AbilityChargeState state = getAbilityState(playerClass, abilityName);
        PlayerClass.AbilityDefinition definition = playerClass != null
                ? playerClass.findAbility(abilityName)
                : null;
        if (state == null || definition == null) {
            return 0L;
        }
        syncAbilityCharges(state, definition, nowMillis);
        if (state.charges >= MAX_ABILITY_CHARGES || state.nextChargeAtMillis <= 0L) {
            return 0L;
        }
        return Math.max(0L, state.nextChargeAtMillis - nowMillis);
    }

    public boolean consumeAbilityCharge(PlayerClass playerClass, String abilityName, long nowMillis) {
        AbilityChargeState state = getAbilityState(playerClass, abilityName);
        PlayerClass.AbilityDefinition definition = playerClass != null
                ? playerClass.findAbility(abilityName)
                : null;
        if (state == null || definition == null) {
            return false;
        }
        syncAbilityCharges(state, definition, nowMillis);
        if (state.charges <= 0) {
            return false;
        }
        state.charges--;
        if (state.charges < MAX_ABILITY_CHARGES && state.nextChargeAtMillis <= 0L) {
            state.nextChargeAtMillis = nowMillis + definition.getRechargeDurationMillis();
        }
        return true;
    }

    public void restoreAbilityChargeDefaults(PlayerClass playerClass) {
        if (playerClass == null) {
            return;
        }
        ClassProgress progress = getOrCreateProgress(playerClass);
        for (String abilityName : progress.unlockedAbilityNames) {
            AbilityChargeState state = progress.ensureAbilityState(abilityName);
            state.charges = MAX_ABILITY_CHARGES;
            state.nextChargeAtMillis = 0L;
        }
    }

    public void setAnimatedPlayer(AnimatedPlayer animatedPlayer) {
        this.animatedPlayer = animatedPlayer;
    }

    public AnimatedPlayer getAnimatedPlayer() {
        return animatedPlayer;
    }

    public String getCharClass() {
        return playerClass != null ? playerClass.toString() : "Unknown";
    }

    public int getLevel() {
        return 1;
    }

    public int getXpRequiredForNextLevel() {
        return 0;
    }

    public void setLevel(int level) {
        // Legacy no-op kept for save/test compatibility after progression simplification.
    }

    public int getAvailableStatPoints() {
        return 0;
    }

    public void setAvailableStatPoints(int points) {
        // Legacy no-op kept for save/test compatibility after progression simplification.
    }

    public boolean spendStatPoints(int points) {
        return false;
    }

    public boolean increaseStrength(int points) {
        addAttackBoost(Math.max(0, points));
        return points > 0;
    }

    public boolean increaseDexterity(int points) {
        addDefenseBoost(Math.max(0, points));
        return points > 0;
    }

    public boolean increaseConstitution(int points) {
        addHealthBoost(Math.max(0, points) * 2);
        return points > 0;
    }

    public boolean increaseIntelligence(int points) {
        addAttackBoost(Math.max(0, points));
        return points > 0;
    }

    public int getStrength() {
        return getAttack();
    }

    public void setStrength(int strength) {
        setAttack(strength);
    }

    public int getDexterity() {
        return getDefense();
    }

    public void setDexterity(int dexterity) {
        setDefense(dexterity);
    }

    public int getConstitution() {
        return getMaxHP();
    }

    public void setConstitution(int constitution) {
        setMaxHP(constitution);
    }

    public int getIntelligence() {
        return getAttack();
    }

    public void setIntelligence(int intelligence) {
        setAttack(intelligence);
    }

    public int getMaxMP() {
        return 0;
    }

    public int getCurrentMP() {
        return 0;
    }

    public void setCurrentMP(int currentMP) {
        // MP is phased out in favor of charge-based abilities.
    }

    public boolean usesMp() {
        return false;
    }

    private int getBaseStartingHealth() {
        return playerClass != null ? playerClass.getStartingHealth() : PlayerClass.KNIGHT.getStartingHealth();
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

    private void ensureAllClassProgress() {
        if (classProgressByName == null) {
            classProgressByName = new HashMap<>();
        }
        for (PlayerClass value : PlayerClass.values()) {
            ensureStarterAbilityUnlocked(value, getOrCreateProgress(value));
        }
    }

    private ClassProgress getOrCreateProgress(PlayerClass playerClass) {
        ensureAllClassProgressMapOnly();
        String key = playerClass != null ? playerClass.name() : PlayerClass.KNIGHT.name();
        ClassProgress progress = classProgressByName.get(key);
        if (progress == null) {
            progress = new ClassProgress();
            classProgressByName.put(key, progress);
        }
        if (progress.unlockedAbilityNames == null) {
            progress.unlockedAbilityNames = new HashSet<>();
        }
        if (progress.abilityChargesByName == null) {
            progress.abilityChargesByName = new HashMap<>();
        }
        return progress;
    }

    private void ensureAllClassProgressMapOnly() {
        if (classProgressByName == null) {
            classProgressByName = new HashMap<>();
        }
    }

    private void ensureStarterAbilityUnlocked(PlayerClass playerClass, ClassProgress progress) {
        if (playerClass == null || progress == null) {
            return;
        }
        PlayerClass.AbilityDefinition starter = playerClass.getStarterAbility();
        if (starter == null) {
            return;
        }
        progress.unlockedAbilityNames.add(starter.getName());
        progress.ensureAbilityState(starter.getName());
    }

    private AbilityChargeState getAbilityState(PlayerClass playerClass, String abilityName) {
        if (playerClass == null || abilityName == null || !isAbilityUnlocked(playerClass, abilityName)) {
            return null;
        }
        return getOrCreateProgress(playerClass).ensureAbilityState(abilityName);
    }

    private void syncAbilityCharges(AbilityChargeState state,
                                    PlayerClass.AbilityDefinition definition,
                                    long nowMillis) {
        if (state == null || definition == null) {
            return;
        }
        if (state.charges >= MAX_ABILITY_CHARGES) {
            state.charges = MAX_ABILITY_CHARGES;
            state.nextChargeAtMillis = 0L;
            return;
        }
        long rechargeDuration = Math.max(1L, definition.getRechargeDurationMillis());
        while (state.charges < MAX_ABILITY_CHARGES
                && state.nextChargeAtMillis > 0L
                && nowMillis >= state.nextChargeAtMillis) {
            state.charges++;
            if (state.charges >= MAX_ABILITY_CHARGES) {
                state.charges = MAX_ABILITY_CHARGES;
                state.nextChargeAtMillis = 0L;
            } else {
                state.nextChargeAtMillis += rechargeDuration;
            }
        }
    }

    private static final class ClassProgress {
        int earnedXp;
        int spentXp;
        Set<String> unlockedAbilityNames = new HashSet<>();
        Map<String, AbilityChargeState> abilityChargesByName = new HashMap<>();

        AbilityChargeState ensureAbilityState(String abilityName) {
            AbilityChargeState state = abilityChargesByName.get(abilityName);
            if (state == null) {
                state = new AbilityChargeState();
                abilityChargesByName.put(abilityName, state);
            }
            return state;
        }
    }

    private static final class AbilityChargeState {
        int charges = MAX_ABILITY_CHARGES;
        long nextChargeAtMillis;
    }
}
