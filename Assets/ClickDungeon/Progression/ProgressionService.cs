using System;

namespace ClickDungeon.Progression
{
    public sealed class ProgressionService
    {
        private readonly HeroProgressionService heroProgression = new HeroProgressionService();

        public bool CompleteFloor(
            RunProgressionState run,
            HeroProgressionState hero,
            AccountProgressionState account,
            int runSeed,
            int floorIndex,
            int heroExperienceReward)
        {
            if (run == null) throw new ArgumentNullException(nameof(run));
            if (hero == null) throw new ArgumentNullException(nameof(hero));
            if (account == null) throw new ArgumentNullException(nameof(account));
            var floorTransactionId = RunProgressionState.BuildFloorTransactionId(runSeed, floorIndex);
            if (!run.TryCompleteFloor(floorTransactionId)) return false;

            heroProgression.AwardExperience(hero, heroExperienceReward);
            account.RecordFloorReached(run.CurrentFloorIndex);
            return true;
        }
    }
}
