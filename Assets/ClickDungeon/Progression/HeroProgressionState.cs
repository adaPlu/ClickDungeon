using System;

namespace ClickDungeon.Progression
{
    public sealed class HeroProgressionState
    {
        public string HeroId { get; }
        public int Level { get; internal set; }
        public int ExperienceInLevel { get; internal set; }
        public long TotalExperience { get; internal set; }
        public int TalentPoints { get; internal set; }

        public HeroProgressionState(
            string heroId,
            int level = 1,
            int experienceInLevel = 0,
            long totalExperience = 0,
            int talentPoints = 0)
        {
            if (string.IsNullOrWhiteSpace(heroId)) throw new ArgumentException("Hero ID is required.", nameof(heroId));
            if (level < 1) throw new ArgumentOutOfRangeException(nameof(level));
            if (experienceInLevel < 0) throw new ArgumentOutOfRangeException(nameof(experienceInLevel));
            if (totalExperience < 0) throw new ArgumentOutOfRangeException(nameof(totalExperience));
            if (talentPoints < 0) throw new ArgumentOutOfRangeException(nameof(talentPoints));
            HeroId = heroId;
            Level = level;
            ExperienceInLevel = experienceInLevel;
            TotalExperience = totalExperience;
            TalentPoints = talentPoints;
        }
    }
}
