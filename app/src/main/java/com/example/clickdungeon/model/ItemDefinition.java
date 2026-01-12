package com.example.clickdungeon.model;

/**
 * Immutable definition for an item, including bonuses and classification metadata.
 */
public class ItemDefinition {

    /** High-level category that drives UI and inventory behavior. */
    public enum ItemType {
        CONSUMABLE,
        WEAPON,
        ARMOR,
        MAGIC
    }

    /** Rarity tier used for loot presentation. */
    public enum Rarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY
    }

    /** Elemental damage type granted by the item. */
    public enum DamageType {
        NONE,
        FIRE,
        ICE,
        POISON,
        LIGHTNING
    }

    /** Equip slot used to validate gear changes. */
    public enum EquipSlot {
        NONE,
        WEAPON,
        ARMOR
    }

    /** Item display name and lookup key. */
    private final String name;
    /** Item category that drives behavior. */
    private final ItemType type;
    /** Rarity tier for UI labeling. */
    private final Rarity rarity;
    /** Equip slot or NONE for non-equippable items. */
    private final EquipSlot equipSlot;
    /** Flat attack bonus granted when equipped. */
    private final int attackBonus;
    /** Flat defense bonus granted when equipped. */
    private final int defenseBonus;
    /** Flat HP bonus granted when equipped. */
    private final int hpBonus;
    /** Bonus applied to ability power or related stat. */
    private final int abilityBonus;
    /** Elemental damage type applied by this item. */
    private final DamageType damageType;
    /** Gold value when selling the item. */
    private final int resaleValue;

    /**
     * Creates a new item definition.
     */
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

    /** Returns the item display name. */
    public String getName() {
        return name;
    }

    /** Returns the item category. */
    public ItemType getType() {
        return type;
    }

    /** Returns the rarity tier. */
    public Rarity getRarity() {
        return rarity;
    }

    /** Returns the equip slot for the item. */
    public EquipSlot getEquipSlot() {
        return equipSlot;
    }

    /** Returns the flat attack bonus. */
    public int getAttackBonus() {
        return attackBonus;
    }

    /** Returns the flat defense bonus. */
    public int getDefenseBonus() {
        return defenseBonus;
    }

    /** Returns the flat HP bonus. */
    public int getHpBonus() {
        return hpBonus;
    }

    /** Returns the ability bonus value. */
    public int getAbilityBonus() {
        return abilityBonus;
    }

    /** Returns the elemental damage type. */
    public DamageType getDamageType() {
        return damageType;
    }

    /** Returns the resale value in gold. */
    public int getResaleValue() {
        return resaleValue;
    }
}
