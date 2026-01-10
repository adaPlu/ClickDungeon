package com.example.clickdungeon.model;

public class ItemDefinition {

    public enum ItemType {
        CONSUMABLE,
        WEAPON,
        ARMOR,
        MAGIC
    }

    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY
    }

    public enum DamageType {
        NONE,
        FIRE,
        ICE,
        POISON,
        LIGHTNING
    }

    public enum EquipSlot {
        NONE,
        WEAPON,
        ARMOR
    }

    private final String name;
    private final ItemType type;
    private final Rarity rarity;
    private final EquipSlot equipSlot;
    private final int attackBonus;
    private final int defenseBonus;
    private final int hpBonus;
    private final int abilityBonus;
    private final DamageType damageType;
    private final int resaleValue;

    public ItemDefinition(String name,
                          ItemType type,
                          Rarity rarity,
                          EquipSlot equipSlot,
                          int attackBonus,
                          int defenseBonus,
                          int hpBonus,
                          int abilityBonus,
                          DamageType damageType,
                          int resaleValue) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        this.equipSlot = equipSlot;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.hpBonus = hpBonus;
        this.abilityBonus = abilityBonus;
        this.damageType = damageType;
        this.resaleValue = resaleValue;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public EquipSlot getEquipSlot() {
        return equipSlot;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public int getHpBonus() {
        return hpBonus;
    }

    public int getAbilityBonus() {
        return abilityBonus;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public int getResaleValue() {
        return resaleValue;
    }
}
