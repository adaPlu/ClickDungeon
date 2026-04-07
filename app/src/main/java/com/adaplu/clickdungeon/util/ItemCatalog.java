package com.adaplu.clickdungeon.util;

import androidx.annotation.NonNull;

import com.adaplu.clickdungeon.model.ItemDefinition;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Catalog of item definitions and random roll helpers for loot generation.
 */
public final class ItemCatalog {

    /** Weapon tier list used for random drops. */
    private static final List<ItemDefinition> WEAPON_TIERS = new ArrayList<>();
    /** Armor tier list used for random drops. */
    private static final List<ItemDefinition> ARMOR_TIERS = new ArrayList<>();
    /** Consumable list used for random drops. */
    private static final List<ItemDefinition> CONSUMABLES = new ArrayList<>();
    /** Magic affixes used to build rolled magic items. */
    private static final List<MagicAffix> MAGIC_AFFIXES = new ArrayList<>();
    /** Index of all base item definitions by name. */
    private static final Map<String, ItemDefinition> ITEM_INDEX = new HashMap<>();

    static {
        WEAPON_TIERS.add(new ItemDefinition("Rusty Sword", ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.COMMON, ItemDefinition.EquipSlot.WEAPON,
                1, 0, 0, 0, ItemDefinition.DamageType.NONE, 20));
        WEAPON_TIERS.add(new ItemDefinition("Iron Sword", ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.UNCOMMON, ItemDefinition.EquipSlot.WEAPON,
                2, 0, 0, 0, ItemDefinition.DamageType.NONE, 40));
        WEAPON_TIERS.add(new ItemDefinition("Steel Blade", ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.RARE, ItemDefinition.EquipSlot.WEAPON,
                3, 0, 0, 0, ItemDefinition.DamageType.NONE, 70));
        WEAPON_TIERS.add(new ItemDefinition("Knight Blade", ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.EPIC, ItemDefinition.EquipSlot.WEAPON,
                4, 0, 0, 0, ItemDefinition.DamageType.NONE, 110));
        WEAPON_TIERS.add(new ItemDefinition("Mythic Edge", ItemDefinition.ItemType.WEAPON,
                ItemDefinition.Rarity.LEGENDARY, ItemDefinition.EquipSlot.WEAPON,
                5, 0, 0, 0, ItemDefinition.DamageType.NONE, 160));

        ARMOR_TIERS.add(new ItemDefinition("Cloth Armor", ItemDefinition.ItemType.ARMOR,
                ItemDefinition.Rarity.COMMON, ItemDefinition.EquipSlot.ARMOR,
                0, 1, 0, 0, ItemDefinition.DamageType.NONE, 20));
        ARMOR_TIERS.add(new ItemDefinition("Leather Armor", ItemDefinition.ItemType.ARMOR,
                ItemDefinition.Rarity.UNCOMMON, ItemDefinition.EquipSlot.ARMOR,
                0, 2, 0, 0, ItemDefinition.DamageType.NONE, 40));
        ARMOR_TIERS.add(new ItemDefinition("Chainmail", ItemDefinition.ItemType.ARMOR,
                ItemDefinition.Rarity.RARE, ItemDefinition.EquipSlot.ARMOR,
                0, 3, 0, 0, ItemDefinition.DamageType.NONE, 70));
        ARMOR_TIERS.add(new ItemDefinition("Plate Mail", ItemDefinition.ItemType.ARMOR,
                ItemDefinition.Rarity.EPIC, ItemDefinition.EquipSlot.ARMOR,
                0, 4, 0, 0, ItemDefinition.DamageType.NONE, 110));
        ARMOR_TIERS.add(new ItemDefinition("Dragonplate", ItemDefinition.ItemType.ARMOR,
                ItemDefinition.Rarity.LEGENDARY, ItemDefinition.EquipSlot.ARMOR,
                0, 5, 0, 0, ItemDefinition.DamageType.NONE, 160));

        CONSUMABLES.add(new ItemDefinition("Healing Potion", ItemDefinition.ItemType.CONSUMABLE,
                ItemDefinition.Rarity.COMMON, ItemDefinition.EquipSlot.NONE,
                0, 0, 0, 0, ItemDefinition.DamageType.NONE, 15));
        CONSUMABLES.add(new ItemDefinition("Trap Disarm Kit", ItemDefinition.ItemType.CONSUMABLE,
                ItemDefinition.Rarity.UNCOMMON, ItemDefinition.EquipSlot.NONE,
                0, 0, 0, 0, ItemDefinition.DamageType.NONE, 25));

        MAGIC_AFFIXES.add(new MagicAffix("Flaming", ItemDefinition.DamageType.FIRE, 1, 0, 0, 0));
        MAGIC_AFFIXES.add(new MagicAffix("Frost", ItemDefinition.DamageType.ICE, 1, 0, 0, 0));
        MAGIC_AFFIXES.add(new MagicAffix("Storm", ItemDefinition.DamageType.LIGHTNING, 1, 0, 0, 0));
        MAGIC_AFFIXES.add(new MagicAffix("Vital", ItemDefinition.DamageType.NONE, 0, 0, 2, 0));
        MAGIC_AFFIXES.add(new MagicAffix("Keen", ItemDefinition.DamageType.NONE, 1, 0, 0, 1));

        indexAll(WEAPON_TIERS);
        indexAll(ARMOR_TIERS);
        indexAll(CONSUMABLES);
    }

    private ItemCatalog() {
    }

    /**
     * Returns a randomized base item from the tier lists.
     */
    @NonNull
    public static ItemDefinition randomBaseItem(@NonNull Random random) {
        int roll = random.nextInt(100);
        if (roll < 20) {
            return CONSUMABLES.get(random.nextInt(CONSUMABLES.size()));
        } else if (roll < 60) {
            return WEAPON_TIERS.get(random.nextInt(WEAPON_TIERS.size()));
        }
        return ARMOR_TIERS.get(random.nextInt(ARMOR_TIERS.size()));
    }

    /**
     * Returns a randomized magic item by combining a base item with an affix.
     */
    @NonNull
    public static ItemDefinition randomMagicItem(@NonNull Random random) {
        ItemDefinition base = random.nextBoolean()
                ? WEAPON_TIERS.get(random.nextInt(WEAPON_TIERS.size()))
                : ARMOR_TIERS.get(random.nextInt(ARMOR_TIERS.size()));
        MagicAffix affix = MAGIC_AFFIXES.get(random.nextInt(MAGIC_AFFIXES.size()));
        String name = affix.name + " " + base.getName();
        return new ItemDefinition(name, ItemDefinition.ItemType.MAGIC,
                bumpRarity(base.getRarity()), base.getEquipSlot(),
                base.getAttackBonus() + affix.attackBonus,
                base.getDefenseBonus() + affix.defenseBonus,
                base.getHpBonus() + affix.hpBonus,
                base.getAbilityBonus() + affix.abilityBonus, affix.damageType,
                Math.max(20, base.getResaleValue() + 40));
    }

    /**
     * Promotes rarity by one tier for magic items.
     */
    private static ItemDefinition.Rarity bumpRarity(ItemDefinition.Rarity rarity) {
        switch (rarity) {
            case COMMON:
                return ItemDefinition.Rarity.UNCOMMON;
            case UNCOMMON:
                return ItemDefinition.Rarity.RARE;
            case RARE:
                return ItemDefinition.Rarity.EPIC;
            case EPIC:
                return ItemDefinition.Rarity.LEGENDARY;
            default:
                return ItemDefinition.Rarity.LEGENDARY;
        }
    }

    /**
     * Resolves an item definition by exact name or magic prefix.
     */
    public static ItemDefinition getItemDefinition(@NonNull String name) {
        ItemDefinition exact = ITEM_INDEX.get(name);
        if (exact != null) {
            return exact;
        }
        return parseMagicItem(name);
    }

    /**
     * Parses a magic item name into a generated definition if it matches a known affix.
     */
    private static ItemDefinition parseMagicItem(String name) {
        if (name == null) {
            return null;
        }
        for (MagicAffix affix : MAGIC_AFFIXES) {
            String prefix = affix.name + " ";
            if (name.startsWith(prefix)) {
                String baseName = name.substring(prefix.length());
                ItemDefinition base = ITEM_INDEX.get(baseName);
                if (base == null) {
                    return null;
                }
                return new ItemDefinition(name, ItemDefinition.ItemType.MAGIC,
                        bumpRarity(base.getRarity()), base.getEquipSlot(),
                        base.getAttackBonus() + affix.attackBonus,
                        base.getDefenseBonus() + affix.defenseBonus,
                        base.getHpBonus() + affix.hpBonus,
                        base.getAbilityBonus() + affix.abilityBonus,
                        affix.damageType,
                        Math.max(20, base.getResaleValue() + 40));
            }
        }
        return null;
    }

    /**
     * Adds a list of item definitions to the name index.
     */
    private static void indexAll(List<ItemDefinition> items) {
        for (ItemDefinition item : items) {
            ITEM_INDEX.put(item.getName(), item);
        }
    }

    /**
     * Descriptor for a magic item affix and its stat modifications.
     */
    private static final class MagicAffix {
        final String name;
        final ItemDefinition.DamageType damageType;
        final int attackBonus;
        final int defenseBonus;
        final int hpBonus;
        final int abilityBonus;

        MagicAffix(String name,
                   ItemDefinition.DamageType damageType,
                   int attackBonus,
                   int defenseBonus,
                   int hpBonus,
                   int abilityBonus) {
            this.name = name;
            this.damageType = damageType;
            this.attackBonus = attackBonus;
            this.defenseBonus = defenseBonus;
            this.hpBonus = hpBonus;
            this.abilityBonus = abilityBonus;
        }
    }
}
