using System;

namespace ClickDungeon.Progression
{
    public readonly struct DepthDifficultyBudget
    {
        public int ThreatBudget { get; }
        public int TrapComplexity { get; }
        public int EliteBudget { get; }
        public int SpecialInteractionBudget { get; }
        public int RewardBudget { get; }

        public DepthDifficultyBudget(
            int threatBudget,
            int trapComplexity,
            int eliteBudget,
            int specialInteractionBudget,
            int rewardBudget)
        {
            ThreatBudget = threatBudget;
            TrapComplexity = trapComplexity;
            EliteBudget = eliteBudget;
            SpecialInteractionBudget = specialInteractionBudget;
            RewardBudget = rewardBudget;
        }

        public static DepthDifficultyBudget ForDepth(int depth)
        {
            if (depth < 0) throw new ArgumentOutOfRangeException(nameof(depth));
            return new DepthDifficultyBudget(
                checked(100 + (depth * 15)),
                checked(1 + (depth / 3)),
                checked(depth / 5),
                checked(1 + (depth / 4)),
                checked(100 + (depth * 10)));
        }
    }
}
