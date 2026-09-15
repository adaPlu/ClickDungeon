using System;
using System.Collections.Generic;

namespace ClickDungeon.Save
{
    public static class SaveValidator
    {
        public static void ValidateRun(RunSave save)
        {
            if (save == null) throw new ArgumentNullException(nameof(save));
            if (save.SchemaVersion > SaveSchema.RunVersion) throw new InvalidOperationException("future schema is not supported");
            if (save.SchemaVersion < 1) throw new InvalidOperationException("Run schema version is invalid.");
            _ = save.RunSeed;
            if (string.IsNullOrWhiteSpace(save.HeroId)) throw new InvalidOperationException("HeroId is required.");
            if (save.GenerationVersion < 0) throw new InvalidOperationException("GenerationVersion is invalid.");
            if (save.FloorIndex < 0) throw new InvalidOperationException("FloorIndex is invalid.");
            if (save.Board == null || save.Board.Count != 25) throw new InvalidOperationException("Board must contain exactly 25 cells.");
            if (save.PlayerPosition == null) throw new InvalidOperationException("PlayerPosition is required.");
            if (save.Inventory == null || save.Statuses == null || save.Enemies == null || save.TileStates == null || save.Objectives == null)
                throw new InvalidOperationException("Run collections cannot be null.");
            if (save.CommittedRewardTransactionIds == null) throw new InvalidOperationException("Committed reward transactions are required.");
            if (save.RunProgression == null) throw new InvalidOperationException("RunProgression is required.");

            EnsureUnique(save.CommittedRewardTransactionIds, "Committed reward transaction IDs");
            EnsureUnique(save.RunProgression.CompletedFloorTransactionIds, "Completed floor transaction IDs");
        }

        public static void ValidateProfile(ProfileSave save)
        {
            if (save == null) throw new ArgumentNullException(nameof(save));
            if (save.SchemaVersion > SaveSchema.ProfileVersion) throw new InvalidOperationException("future schema is not supported");
            if (save.SchemaVersion < 1) throw new InvalidOperationException("Profile schema version is invalid.");
            if (save.HeroProgression == null || save.AccountProgression == null || save.Currencies == null)
                throw new InvalidOperationException("Profile progression data is incomplete.");
        }

        public static void ValidateSettings(SettingsSave save)
        {
            if (save == null) throw new ArgumentNullException(nameof(save));
            if (save.SchemaVersion > SaveSchema.SettingsVersion) throw new InvalidOperationException("future schema is not supported");
            if (save.SchemaVersion < 1) throw new InvalidOperationException("Settings schema version is invalid.");
            if (save.MasterVolume < 0 || save.MasterVolume > 100 || save.MusicVolume < 0 || save.MusicVolume > 100 || save.SfxVolume < 0 || save.SfxVolume > 100)
                throw new InvalidOperationException("Audio volume must be between 0 and 100.");
            if (save.TextScale < 50 || save.TextScale > 200 || save.ScreenShakeScale < 0 || save.ScreenShakeScale > 100)
                throw new InvalidOperationException("Accessibility settings are out of range.");
        }

        private static void EnsureUnique(IEnumerable<string> values, string label)
        {
            var seen = new HashSet<string>(StringComparer.Ordinal);
            foreach (var value in values)
            {
                if (string.IsNullOrWhiteSpace(value) || !seen.Add(value))
                    throw new InvalidOperationException(label + " must be non-empty and unique.");
            }
        }
    }
}
