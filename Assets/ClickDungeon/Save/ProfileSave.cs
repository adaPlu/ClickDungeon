using System.Collections.Generic;

namespace ClickDungeon.Save
{
    public sealed class HeroProgressionSave
    {
        public string HeroId { get; set; } = string.Empty;
        public int Level { get; set; } = 1;
        public int ExperienceInLevel { get; set; }
        public long TotalExperience { get; set; }
        public int TalentPoints { get; set; }
    }

    public sealed class AccountProgressionSave
    {
        public int HighestFloorReached { get; set; }
        public int CompletedRuns { get; set; }
        public List<string> UnlockedHeroIds { get; set; } = new List<string>();
    }

    public sealed class CurrencyBalanceSave
    {
        public string CurrencyId { get; set; } = string.Empty;
        public long Amount { get; set; }
    }

    public sealed class ProfileSave
    {
        public int SchemaVersion { get; set; } = SaveSchema.ProfileVersion;
        public List<HeroProgressionSave> HeroProgression { get; set; } = new List<HeroProgressionSave>();
        public AccountProgressionSave AccountProgression { get; set; } = new AccountProgressionSave();
        public List<CurrencyBalanceSave> Currencies { get; set; } = new List<CurrencyBalanceSave>();
    }
}
