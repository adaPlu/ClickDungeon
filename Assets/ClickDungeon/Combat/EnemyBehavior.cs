using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Combat
{
    public sealed class EnemyBehaviorDefinition
    {
        public ContentId Id { get; }
        public IReadOnlyList<string> BehaviorTags { get; }
        public IReadOnlyList<ContentId> AbilityIds { get; }
        public int InitiativeBias { get; }

        public EnemyBehaviorDefinition(
            ContentId id,
            IReadOnlyList<string> behaviorTags,
            IReadOnlyList<ContentId> abilityIds,
            int initiativeBias = 0)
        {
            if (behaviorTags == null || behaviorTags.Count == 0) throw new ArgumentException("Behavior tags are required.", nameof(behaviorTags));
            if (abilityIds == null || abilityIds.Count == 0) throw new ArgumentException("At least one ability is required.", nameof(abilityIds));
            Id = id;
            BehaviorTags = CopyStrings(behaviorTags);
            AbilityIds = CopyIds(abilityIds);
            InitiativeBias = initiativeBias;
        }

        private static IReadOnlyList<string> CopyStrings(IReadOnlyList<string> source)
        {
            var copy = new string[source.Count];
            for (var i = 0; i < source.Count; i++)
            {
                if (string.IsNullOrWhiteSpace(source[i])) throw new ArgumentException("Behavior tags must be non-empty.", nameof(source));
                copy[i] = source[i];
            }
            return copy;
        }

        private static IReadOnlyList<ContentId> CopyIds(IReadOnlyList<ContentId> source)
        {
            var copy = new ContentId[source.Count];
            for (var i = 0; i < source.Count; i++) copy[i] = source[i];
            return copy;
        }
    }
}
