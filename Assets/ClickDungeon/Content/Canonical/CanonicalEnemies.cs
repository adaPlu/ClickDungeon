using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalEnemies
    {
        private static readonly IReadOnlyList<EnemyAnimationState> StandardAnimationStates = new[]
        {
            EnemyAnimationState.Spawn,
            EnemyAnimationState.Idle,
            EnemyAnimationState.Attack,
            EnemyAnimationState.Hit,
            EnemyAnimationState.Defeat
        };

        private static readonly IReadOnlyList<EnemyDefinition> Definitions = Build();
        private static readonly IReadOnlyDictionary<ContentId, EnemyDefinition> DefinitionsById = Index(Definitions);

        public static IReadOnlyList<EnemyDefinition> All => Definitions;

        public static EnemyDefinition GetRequired(ContentId id)
        {
            if (!DefinitionsById.TryGetValue(id, out var definition))
                throw new KeyNotFoundException($"Unknown enemy ID: {id}");
            return definition;
        }

        private static IReadOnlyList<EnemyDefinition> Build()
        {
            var items = new List<EnemyDefinition>(10);

            // Numeric values are launch tuning defaults; the reference art defines identity/type, not final balance.
            Add("enemy.goblin_brute_king", "Goblin Brute King", new[] { EnemyThreatTag.Brute, EnemyThreatTag.Boss },
                180, 24, 10, 5, 150, 5, 5, "loot.enemy.goblin_brute_king", "behavior.enemy.brute_boss", "Art/Runtime/Monsters/goblin_brute_king", items);
            Add("enemy.crowned_slime", "Crowned Slime", new[] { EnemyThreatTag.Brute },
                65, 10, 4, 4, 40, 1, 20, "loot.enemy.crowned_slime", "behavior.enemy.slime_brute", "Art/Runtime/Monsters/crowned_slime", items);
            Add("enemy.skeleton_warrior", "Skeleton Warrior", new[] { EnemyThreatTag.Undead },
                80, 14, 8, 5, 50, 1, 18, "loot.enemy.skeleton_warrior", "behavior.enemy.skeleton_guard", "Art/Runtime/Monsters/skeleton_warrior", items);
            Add("enemy.bat_swarm_leader", "Bat Swarm Leader", new[] { EnemyThreatTag.Swarm, EnemyThreatTag.Boss },
                120, 18, 5, 8, 120, 4, 7, "loot.enemy.bat_swarm_leader", "behavior.enemy.swarm_boss", "Art/Runtime/Monsters/bat_swarm_leader", items);
            Add("enemy.mimic_chest", "Mimic Chest", new[] { EnemyThreatTag.Trickster },
                90, 20, 6, 7, 70, 2, 10, "loot.enemy.mimic_chest", "behavior.enemy.mimic_ambush", "Art/Runtime/Monsters/mimic_chest", items);
            Add("enemy.fire_imp", "Fire Imp", new[] { EnemyThreatTag.Magic },
                70, 16, 3, 7, 55, 2, 16, "loot.enemy.fire_imp", "behavior.enemy.fire_caster", "Art/Runtime/Monsters/fire_imp", items);
            Add("enemy.armored_boar", "Armored Boar", new[] { EnemyThreatTag.Beast },
                110, 18, 10, 4, 65, 3, 14, "loot.enemy.armored_boar", "behavior.enemy.armored_charge", "Art/Runtime/Monsters/armored_boar", items);
            Add("enemy.spooky_spellbook", "Spooky Spellbook", new[] { EnemyThreatTag.Magic },
                75, 17, 4, 6, 60, 3, 13, "loot.enemy.spooky_spellbook", "behavior.enemy.spellbook_control", "Art/Runtime/Monsters/spooky_spellbook", items);
            Add("enemy.cave_spider", "Cave Spider", new[] { EnemyThreatTag.Beast },
                85, 15, 5, 7, 55, 2, 15, "loot.enemy.cave_spider", "behavior.enemy.web_ambush", "Art/Runtime/Monsters/cave_spider", items);
            Add("enemy.theater_curtain_demon", "Theater Curtain Demon", new[] { EnemyThreatTag.Boss, EnemyThreatTag.Magic },
                240, 28, 12, 6, 220, 10, 3, "loot.enemy.theater_curtain_demon", "behavior.enemy.theater_boss", "Art/Runtime/Bosses/theater_curtain_demon", items);

            return items;
        }

        private static void Add(
            string id,
            string displayName,
            IReadOnlyList<EnemyThreatTag> threatTags,
            int health,
            int attack,
            int defense,
            int initiative,
            int xp,
            int minDepth,
            int rarityWeight,
            string lootTableId,
            string behaviorId,
            string spritePath,
            ICollection<EnemyDefinition> items)
        {
            items.Add(new EnemyDefinition(
                ContentId.Parse(id), displayName, threatTags,
                health, attack, defense, initiative, xp, minDepth, rarityWeight,
                ContentId.Parse(lootTableId), ContentId.Parse(behaviorId), spritePath,
                StandardAnimationStates));
        }

        private static IReadOnlyDictionary<ContentId, EnemyDefinition> Index(IReadOnlyList<EnemyDefinition> source)
        {
            var result = new Dictionary<ContentId, EnemyDefinition>(source.Count);
            for (var i = 0; i < source.Count; i++) result.Add(source[i].Id, source[i]);
            return result;
        }
    }
}
