using System;
using System.Collections.Generic;

namespace ClickDungeon.Progression
{
    public sealed class AccountProgressionState
    {
        private readonly HashSet<string> unlockedHeroIds = new HashSet<string>(StringComparer.Ordinal);

        public int HighestFloorReached { get; private set; }
        public int CompletedRuns { get; private set; }
        public IReadOnlyCollection<string> UnlockedHeroIds => unlockedHeroIds;

        public AccountProgressionState(int highestFloorReached = 0, int completedRuns = 0)
        {
            if (highestFloorReached < 0) throw new ArgumentOutOfRangeException(nameof(highestFloorReached));
            if (completedRuns < 0) throw new ArgumentOutOfRangeException(nameof(completedRuns));
            HighestFloorReached = highestFloorReached;
            CompletedRuns = completedRuns;
        }

        public void RecordFloorReached(int floorIndex)
        {
            if (floorIndex < 0) throw new ArgumentOutOfRangeException(nameof(floorIndex));
            if (floorIndex > HighestFloorReached) HighestFloorReached = floorIndex;
        }

        public void RecordRunCompleted()
        {
            CompletedRuns = checked(CompletedRuns + 1);
        }

        public bool UnlockHero(string heroId)
        {
            if (string.IsNullOrWhiteSpace(heroId)) throw new ArgumentException("Hero ID is required.", nameof(heroId));
            return unlockedHeroIds.Add(heroId);
        }

        public void RestoreUnlockedHeroes(IEnumerable<string> heroIds)
        {
            if (heroIds == null) throw new ArgumentNullException(nameof(heroIds));
            foreach (var heroId in heroIds) UnlockHero(heroId);
        }
    }
}
