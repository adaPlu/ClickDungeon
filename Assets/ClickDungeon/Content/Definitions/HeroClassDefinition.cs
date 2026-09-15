using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public sealed class HeroClassDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public IReadOnlyList<string> RoleTags { get; }
        public IReadOnlyList<string> CombatTags { get; }
        public IReadOnlyList<string> EquipmentAffinityTags { get; }

        public HeroClassDefinition(
            ContentId id,
            string displayName,
            IReadOnlyList<string> roleTags,
            IReadOnlyList<string> combatTags,
            IReadOnlyList<string> equipmentAffinityTags)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            Id = id;
            DisplayName = displayName;
            RoleTags = CopyRequired(roleTags, nameof(roleTags));
            CombatTags = CopyRequired(combatTags, nameof(combatTags));
            EquipmentAffinityTags = CopyRequired(equipmentAffinityTags, nameof(equipmentAffinityTags));
        }

        private static IReadOnlyList<string> CopyRequired(IReadOnlyList<string> source, string parameterName)
        {
            if (source == null || source.Count == 0) throw new ArgumentException("At least one value is required.", parameterName);
            var copy = new string[source.Count];
            for (var i = 0; i < source.Count; i++)
            {
                if (string.IsNullOrWhiteSpace(source[i])) throw new ArgumentException("Values must be non-empty.", parameterName);
                copy[i] = source[i];
            }
            return copy;
        }
    }
}
