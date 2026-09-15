using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalItems
    {
        private static readonly IReadOnlyList<ItemDefinition> Definitions = Build();
        private static readonly IReadOnlyDictionary<ContentId, ItemDefinition> DefinitionsById = Index(Definitions);

        public static IReadOnlyList<ItemDefinition> All => Definitions;

        public static ItemDefinition GetRequired(ContentId id)
        {
            if (!DefinitionsById.TryGetValue(id, out var definition))
                throw new KeyNotFoundException($"Unknown item ID: {id}");
            return definition;
        }

        private static IReadOnlyList<ItemDefinition> Build()
        {
            var items = new List<ItemDefinition>();

            Add("item.weapon.dragonslayer", "Dragonslayer", ItemCategory.Weapon, ItemRarity.Legendary, EquipmentSlot.MainHand, false, 1,
                new[] { "weapon", "sword", "melee" }, "Art/Runtime/Items/dragonslayer", items);
            Add("item.armor.void_plate", "Void Plate", ItemCategory.Armor, ItemRarity.Epic, EquipmentSlot.Body, false, 1,
                new[] { "armor", "heavy_armor", "void" }, "Art/Runtime/Items/void_plate", items);
            Add("item.weapon.celestial_staff", "Celestial Staff", ItemCategory.Weapon, ItemRarity.Legendary, EquipmentSlot.MainHand, false, 1,
                new[] { "weapon", "staff", "magic" }, "Art/Runtime/Items/celestial_staff", items);
            Add("item.offhand.aegis_of_dawn", "Aegis of Dawn", ItemCategory.Offhand, ItemRarity.Legendary, EquipmentSlot.OffHand, false, 1,
                new[] { "offhand", "shield", "holy" }, "Art/Runtime/Items/aegis_of_dawn", items);
            Add("item.armor.crown_of_kings", "Crown of Kings", ItemCategory.Armor, ItemRarity.Legendary, EquipmentSlot.Head, false, 1,
                new[] { "armor", "helmet", "crown" }, "Art/Runtime/Items/crown_of_kings", items);
            Add("item.armor.boots_of_swiftness", "Boots of Swiftness", ItemCategory.Armor, ItemRarity.Epic, EquipmentSlot.Feet, false, 1,
                new[] { "armor", "boots", "speed" }, "Art/Runtime/Items/boots_of_swiftness", items);

            Add("item.accessory.ring_sapphire", "Sapphire Ring", ItemCategory.Accessory, ItemRarity.Rare, EquipmentSlot.Ring, false, 1,
                new[] { "accessory", "ring", "sapphire" }, "Art/Runtime/Items/ring_sapphire", items);
            Add("item.accessory.amulet_ruby", "Ruby Amulet", ItemCategory.Accessory, ItemRarity.Rare, EquipmentSlot.Amulet, false, 1,
                new[] { "accessory", "amulet", "ruby" }, "Art/Runtime/Items/amulet_ruby", items);
            Add("item.consumable.health_potion", "Health Potion", ItemCategory.Consumable, ItemRarity.Common, EquipmentSlot.None, true, 20,
                new[] { "consumable", "potion", "healing" }, "Art/Runtime/Items/potion_health", items);
            Add("item.consumable.arcane_scroll", "Arcane Scroll", ItemCategory.Consumable, ItemRarity.Uncommon, EquipmentSlot.None, true, 10,
                new[] { "consumable", "scroll", "magic" }, "Art/Runtime/Items/scroll_arcane", items);
            Add("item.consumable.bomb", "Bomb", ItemCategory.Consumable, ItemRarity.Common, EquipmentSlot.None, true, 10,
                new[] { "consumable", "bomb", "damage" }, "Art/Runtime/Items/bomb", items);
            Add("item.consumable.ration", "Dungeon Ration", ItemCategory.Consumable, ItemRarity.Common, EquipmentSlot.None, true, 20,
                new[] { "consumable", "food", "recovery" }, "Art/Runtime/Items/food_ration", items);
            Add("item.key.dungeon", "Dungeon Key", ItemCategory.Key, ItemRarity.Common, EquipmentSlot.None, true, 99,
                new[] { "key", "utility", "lock" }, "Art/Runtime/Items/key_dungeon", items);
            Add("item.key.royal", "Royal Key", ItemCategory.Key, ItemRarity.Epic, EquipmentSlot.None, true, 20,
                new[] { "key", "utility", "special_key" }, "Art/Runtime/Items/key_royal", items);
            Add("item.utility.rope", "Dungeon Rope", ItemCategory.Consumable, ItemRarity.Uncommon, EquipmentSlot.None, true, 5,
                new[] { "utility", "mobility" }, "Art/Runtime/Items/utility_rope", items);
            Add("item.currency.gold", "Gold", ItemCategory.Currency, ItemRarity.Common, EquipmentSlot.None, true, 999999,
                new[] { "currency", "gold" }, "Art/Runtime/Items/currency_gold", items);
            Add("item.currency.gem", "Gem", ItemCategory.Currency, ItemRarity.Rare, EquipmentSlot.None, true, 999999,
                new[] { "currency", "gem" }, "Art/Runtime/Items/currency_gem", items);
            Add("item.quest.ancient_seal", "Ancient Seal", ItemCategory.QuestItem, ItemRarity.Rare, EquipmentSlot.None, true, 99,
                new[] { "quest", "utility", "seal" }, "Art/Runtime/Items/quest_ancient_seal", items);
            Add("item.relic.sun_emblem", "Sun Emblem", ItemCategory.Relic, ItemRarity.Epic, EquipmentSlot.Relic, false, 1,
                new[] { "relic", "holy" }, "Art/Runtime/Items/relic_sun_emblem", items);
            Add("item.quest.chest_token", "Chest Token", ItemCategory.QuestItem, ItemRarity.Uncommon, EquipmentSlot.None, true, 99,
                new[] { "quest", "chest", "reward" }, "Art/Runtime/Items/chest_token", items);

            return items;
        }

        private static void Add(
            string id,
            string displayName,
            ItemCategory category,
            ItemRarity rarity,
            EquipmentSlot slot,
            bool stackable,
            int maxStack,
            IReadOnlyList<string> tags,
            string spritePath,
            ICollection<ItemDefinition> items)
        {
            items.Add(new ItemDefinition(
                ContentId.Parse(id), displayName, category, rarity, slot,
                stackable, maxStack, tags, spritePath));
        }

        private static IReadOnlyDictionary<ContentId, ItemDefinition> Index(IReadOnlyList<ItemDefinition> source)
        {
            var result = new Dictionary<ContentId, ItemDefinition>(source.Count);
            for (var i = 0; i < source.Count; i++) result.Add(source[i].Id, source[i]);
            return result;
        }
    }
}
