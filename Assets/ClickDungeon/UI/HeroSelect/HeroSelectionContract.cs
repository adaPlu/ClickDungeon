using System.Collections.Generic;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.UI.HeroSelect
{
    public sealed class HeroSelectionEntry
    {
        public HeroIdentityDefinition Identity { get; }
        public ContentId MechanicsClassId => Identity.MechanicsClassId;
        public string PresentationClassLabel => Identity.PresentationClassLabel;
        public ContentId ArtSetId => Identity.ArtSetId;
        public bool IsUnlocked { get; }

        public HeroSelectionEntry(HeroIdentityDefinition identity, bool isUnlocked)
        {
            Identity = identity;
            IsUnlocked = isUnlocked;
        }
    }

    public static class HeroSelectionContract
    {
        public static IReadOnlyList<HeroSelectionEntry> Build(ISet<ContentId> unlockedHeroIds)
        {
            var source = CanonicalHeroes.All;
            var result = new HeroSelectionEntry[source.Count];
            for (var i = 0; i < source.Count; i++)
            {
                var unlocked = unlockedHeroIds != null && unlockedHeroIds.Contains(source[i].Id);
                result[i] = new HeroSelectionEntry(source[i], unlocked);
            }
            return result;
        }
    }
}
