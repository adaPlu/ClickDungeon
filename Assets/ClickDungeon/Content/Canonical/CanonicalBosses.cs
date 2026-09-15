using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalBosses
    {
        private const string GroundSlam = "mechanic.boss.ground_slam";
        private const string DarkDive = "mechanic.boss.dark_dive";
        private const string CurtainCall = "mechanic.boss.curtain_call";
        private const string GoldenSplash = "mechanic.boss.golden_splash";
        private const string SummonMinions = "mechanic.boss.summon_minions";
        private const string PuffsUp = "mechanic.boss.puffs_up";

        private static readonly IReadOnlyList<BossDefinition> Definitions = Build();
        private static readonly IReadOnlyDictionary<ContentId, BossDefinition> DefinitionsById = Index(Definitions);

        public static IReadOnlyList<BossDefinition> All => Definitions;

        public static BossDefinition GetRequired(ContentId id)
        {
            if (!DefinitionsById.TryGetValue(id, out var definition))
                throw new KeyNotFoundException($"Unknown boss ID: {id}");
            return definition;
        }

        private static IReadOnlyList<BossDefinition> Build()
        {
            var items = new List<BossDefinition>(4);

            Add("boss.goblin_brute_king", "Goblin Brute King", "enemy.goblin_brute_king",
                new[] { GroundSlam, "mechanic.boss.knockback" },
                new[] { "telegraph.boss.ground_slam" },
                new[] { "phase.boss.goblin_enraged" },
                new string[0],
                new[] { "arena.boss.cracked_floor" },
                new[] { "dialogue_trigger.boss.goblin_intro" },
                "loot.boss.goblin_brute_king", items);

            Add("boss.bat_swarm_leader", "Bat Swarm Leader", "enemy.bat_swarm_leader",
                new[] { DarkDive, "mechanic.boss.swarm_screen" },
                new[] { "telegraph.boss.dark_dive" },
                new[] { "phase.boss.swarm_escalation" },
                new[] { "summon.boss.bat_minions" },
                new[] { "arena.boss.shadow_pressure" },
                new[] { "dialogue_trigger.boss.bat_intro" },
                "loot.boss.bat_swarm_leader", items);

            Add("boss.theater_curtain_demon", "Theater Curtain Demon", "enemy.theater_curtain_demon",
                new[] { CurtainCall, "mechanic.boss.mask_cycle" },
                new[] { "telegraph.boss.curtain_call" },
                new[] { "phase.boss.comedy", "phase.boss.tragedy" },
                new[] { "summon.boss.stage_minions" },
                new[] { "arena.boss.curtain_hazard" },
                new[] { "dialogue_trigger.boss.theater_intro", "dialogue_trigger.boss.theater_phase" },
                "loot.boss.theater_curtain_demon", items);

            // Lord Blobert is a campaign identity, not Crowned Slime renamed at lookup time.
            // He reuses the slime visual/combat archetype while owning a unique boss ID and mechanic package.
            Add("boss.lord_blobert", "Lord Blobert", "enemy.crowned_slime",
                new[] { GoldenSplash, SummonMinions, PuffsUp },
                new[] { "telegraph.boss.golden_splash", "telegraph.boss.summon_minions" },
                new[] { "phase.boss.blobert_boast", "phase.boss.blobert_puffed" },
                new[] { "summon.boss.blobert_minions" },
                new[] { "arena.boss.golden_splash" },
                new[] { "dialogue_trigger.boss.blobert_intro", "dialogue_trigger.boss.blobert_boast" },
                "loot.boss.lord_blobert", items);

            return items;
        }

        private static void Add(
            string id,
            string displayName,
            string baseEnemyId,
            IReadOnlyList<string> mechanicIds,
            IReadOnlyList<string> telegraphIds,
            IReadOnlyList<string> phaseIds,
            IReadOnlyList<string> summonIds,
            IReadOnlyList<string> arenaEffectIds,
            IReadOnlyList<string> dialogueTriggerIds,
            string rewardTableId,
            ICollection<BossDefinition> items)
        {
            items.Add(new BossDefinition(
                ContentId.Parse(id),
                displayName,
                ContentId.Parse(baseEnemyId),
                Parse(mechanicIds),
                Parse(telegraphIds),
                Parse(phaseIds),
                Parse(summonIds),
                Parse(arenaEffectIds),
                Parse(dialogueTriggerIds),
                ContentId.Parse(rewardTableId)));
        }

        private static IReadOnlyList<ContentId> Parse(IReadOnlyList<string> values)
        {
            var result = new ContentId[values.Count];
            for (var i = 0; i < values.Count; i++) result[i] = ContentId.Parse(values[i]);
            return result;
        }

        private static IReadOnlyDictionary<ContentId, BossDefinition> Index(IReadOnlyList<BossDefinition> source)
        {
            var result = new Dictionary<ContentId, BossDefinition>(source.Count);
            for (var i = 0; i < source.Count; i++) result.Add(source[i].Id, source[i]);
            return result;
        }
    }
}
