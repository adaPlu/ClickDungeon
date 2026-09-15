using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalHeroClasses
    {
        private static readonly IReadOnlyList<HeroClassDefinition> Definitions = Build();
        public static IReadOnlyList<HeroClassDefinition> All => Definitions;

        private static IReadOnlyList<HeroClassDefinition> Build()
        {
            var items = new List<HeroClassDefinition>(8);

            Add("class.knight", "Knight",
                new[] { "tank" },
                new[] { "melee", "durable", "control" },
                new[] { "sword", "shield", "heavy_armor" }, items);

            // Shadowcut's dedicated identity sheet explicitly names classId: thief;
            // the roster's "Rogue" wording remains presentation metadata on the hero identity.
            Add("class.thief", "Thief",
                new[] { "damage" },
                new[] { "melee", "critical", "burst", "evasion" },
                new[] { "daggers", "light_armor", "stealth_gear" }, items);

            Add("class.wizard", "Wizard",
                new[] { "damage" },
                new[] { "magic", "battlefield_control" },
                new[] { "staff", "wand", "robes", "magical_focus" }, items);

            Add("class.ranger", "Ranger",
                new[] { "damage" },
                new[] { "ranged", "precision", "mobility" },
                new[] { "longbow", "quiver", "ranger_leathers", "forest_charm" }, items);

            Add("class.cleric", "Cleric",
                new[] { "healer", "support" },
                new[] { "healing", "protection", "support" },
                new[] { "staff", "holy_tome", "vestments", "relic" }, items);

            Add("class.berserker", "Berserker",
                new[] { "damage" },
                new[] { "melee", "rage", "momentum", "endurance" },
                new[] { "great_axe", "fur_armor", "war_belt", "rage_totem" }, items);

            Add("class.engineer", "Engineer",
                new[] { "utility", "support" },
                new[] { "control", "devices", "tactics", "adaptability" },
                new[] { "wrench", "goggles", "tool_belt", "mechanical_core" }, items);

            Add("class.paladin", "Paladin",
                new[] { "tank", "support" },
                new[] { "melee", "protection", "holy" },
                new[] { "warhammer", "shield", "heavy_armor", "holy_relic" }, items);

            return items;
        }

        private static void Add(
            string id,
            string displayName,
            IReadOnlyList<string> roles,
            IReadOnlyList<string> combatTags,
            IReadOnlyList<string> equipmentAffinities,
            ICollection<HeroClassDefinition> items)
        {
            items.Add(new HeroClassDefinition(
                ContentId.Parse(id),
                displayName,
                roles,
                combatTags,
                equipmentAffinities));
        }
    }
}
