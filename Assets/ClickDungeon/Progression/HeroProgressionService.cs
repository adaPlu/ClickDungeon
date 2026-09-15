using System;

namespace ClickDungeon.Progression
{
    public sealed class HeroProgressionService
    {
        public int ExperienceRequiredForLevel(int level)
        {
            if (level < 1) throw new ArgumentOutOfRangeException(nameof(level));
            return checked(100 + ((level - 1) * 50));
        }

        public int AwardExperience(HeroProgressionState state, int amount)
        {
            if (state == null) throw new ArgumentNullException(nameof(state));
            if (amount < 0) throw new ArgumentOutOfRangeException(nameof(amount));

            state.TotalExperience = checked(state.TotalExperience + amount);
            state.ExperienceInLevel = checked(state.ExperienceInLevel + amount);

            var levelsGained = 0;
            while (state.ExperienceInLevel >= ExperienceRequiredForLevel(state.Level))
            {
                var required = ExperienceRequiredForLevel(state.Level);
                state.ExperienceInLevel -= required;
                state.Level = checked(state.Level + 1);
                state.TalentPoints = checked(state.TalentPoints + 1);
                levelsGained = checked(levelsGained + 1);
            }

            return levelsGained;
        }
    }
}
