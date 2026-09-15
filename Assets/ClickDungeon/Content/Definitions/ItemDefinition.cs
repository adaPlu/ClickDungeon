using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public enum ItemCategory
    {
        Weapon,
        Armor,
        Offhand,
        Accessory,
        Consumable,
        Key,
        Currency,
        QuestItem,
        Relic
    }

    public enum ItemRarity
    {
        Common,
        Uncommon,
        Rare,
        Epic,
        Legendary
    }

    public enum EquipmentSlot
    {
        None,
        MainHand,
        OffHand,
        Body,
        Head,
        Feet,
        Ring,
        Amulet,
        Relic
    }

    public sealed class ItemDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public ItemCategory Category { get; }
        public ItemRarity Rarity { get; }
        public EquipmentSlot EquipmentSlot { get; }
        public bool Stackable { get; }
        public int MaxStack { get; }
        public IReadOnlyList<string> Tags { get; }
        public string SpriteContractPath { get; }

        public ItemDefinition(
            ContentId id,
            string displayName,
            ItemCategory category,
            ItemRarity rarity,
            EquipmentSlot equipmentSlot,
            bool stackable,
            int maxStack,
            IReadOnlyList<string> tags,
            string spriteContractPath)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            if (stackable && maxStack < 1) throw new ArgumentOutOfRangeException(nameof(maxStack));
            if (!stackable && maxStack != 1) throw new ArgumentException("Non-stackable items must use MaxStack 1.", nameof(maxStack));
            if (tags == null || tags.Count == 0) throw new ArgumentException("At least one item tag is required.", nameof(tags));
            if (string.IsNullOrWhiteSpace(spriteContractPath)) throw new ArgumentException("Sprite contract path is required.", nameof(spriteContractPath));

            Id = id;
            DisplayName = displayName;
            Category = category;
            Rarity = rarity;
            EquipmentSlot = equipmentSlot;
            Stackable = stackable;
            MaxStack = maxStack;
            Tags = Copy(tags);
            SpriteContractPath = spriteContractPath;
        }

        private static IReadOnlyList<string> Copy(IReadOnlyList<string> values)
        {
            var result = new string[values.Count];
            for (var i = 0; i < values.Count; i++)
            {
                if (string.IsNullOrWhiteSpace(values[i])) throw new ArgumentException("Item tags must be non-empty.", nameof(values));
                result[i] = values[i];
            }
            return result;
        }
    }
}
