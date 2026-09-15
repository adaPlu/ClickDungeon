using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public sealed class BossDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public ContentId BaseEnemyId { get; }
        public IReadOnlyList<ContentId> MechanicIds { get; }
        public IReadOnlyList<ContentId> TelegraphIds { get; }
        public IReadOnlyList<ContentId> PhaseIds { get; }
        public IReadOnlyList<ContentId> SummonIds { get; }
        public IReadOnlyList<ContentId> ArenaEffectIds { get; }
        public IReadOnlyList<ContentId> DialogueTriggerIds { get; }
        public ContentId RewardTableId { get; }

        public BossDefinition(
            ContentId id,
            string displayName,
            ContentId baseEnemyId,
            IReadOnlyList<ContentId> mechanicIds,
            IReadOnlyList<ContentId> telegraphIds,
            IReadOnlyList<ContentId> phaseIds,
            IReadOnlyList<ContentId> summonIds,
            IReadOnlyList<ContentId> arenaEffectIds,
            IReadOnlyList<ContentId> dialogueTriggerIds,
            ContentId rewardTableId)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            if (mechanicIds == null || mechanicIds.Count == 0) throw new ArgumentException("Bosses require at least one mechanic beyond statistics.", nameof(mechanicIds));
            if (telegraphIds == null || telegraphIds.Count == 0) throw new ArgumentException("Boss mechanics require telegraphs.", nameof(telegraphIds));
            Id = id;
            DisplayName = displayName;
            BaseEnemyId = baseEnemyId;
            MechanicIds = Copy(mechanicIds);
            TelegraphIds = Copy(telegraphIds);
            PhaseIds = Copy(phaseIds);
            SummonIds = Copy(summonIds);
            ArenaEffectIds = Copy(arenaEffectIds);
            DialogueTriggerIds = Copy(dialogueTriggerIds);
            RewardTableId = rewardTableId;
        }

        private static IReadOnlyList<ContentId> Copy(IReadOnlyList<ContentId> source)
        {
            if (source == null) return Array.Empty<ContentId>();
            var copy = new ContentId[source.Count];
            for (var i = 0; i < source.Count; i++) copy[i] = source[i];
            return copy;
        }
    }
}
