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
            if (save.EncounterRooms == null) throw new InvalidOperationException("EncounterRooms is required.");
            if (save.EncounterRooms.Count > 1) throw new InvalidOperationException("A floor may contain at most one encounter room.");

            EnsureUnique(save.CommittedRewardTransactionIds, "Committed reward transaction IDs");
            EnsureUnique(save.RunProgression.CompletedFloorTransactionIds, "Completed floor transaction IDs");
            ValidateEncounterRooms(save);
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

        private static void ValidateEncounterRooms(RunSave save)
        {
            var roomIds = new HashSet<string>(StringComparer.Ordinal);
            for (var i = 0; i < save.EncounterRooms.Count; i++)
            {
                var room = save.EncounterRooms[i];
                if (room == null) throw new InvalidOperationException("Encounter room cannot be null.");
                if (string.IsNullOrWhiteSpace(room.RoomId) || !roomIds.Add(room.RoomId))
                    throw new InvalidOperationException("Encounter room IDs must be non-empty and unique.");
                if (room.ParentFloorIndex != save.FloorIndex)
                    throw new InvalidOperationException("Encounter room parent floor must match the run floor.");
                if (room.Doorway == null || room.Doorway.X < 0 || room.Doorway.X >= 5 || room.Doorway.Y < 0 || room.Doorway.Y >= 5)
                    throw new InvalidOperationException("Encounter room doorway must be on the 5x5 parent floor.");
                if (room.DoorKind != "Closed" && room.DoorKind != "Locked")
                    throw new InvalidOperationException("Encounter room door kind is invalid.");
                if (room.RewardMode != "PremiumChest" && room.RewardMode != "MultiChest")
                    throw new InvalidOperationException("Encounter room reward mode is invalid.");
                if (room.RewardTier != "Normal" && room.RewardTier != "Higher")
                    throw new InvalidOperationException("Encounter room reward tier is invalid.");
                if (room.DoorKind == "Locked" && room.RewardTier != "Higher")
                    throw new InvalidOperationException("Locked encounter rooms require the higher reward tier.");
                if (room.Monsters == null || room.Monsters.Count < 3 || room.Monsters.Count > 5)
                    throw new InvalidOperationException("Encounter room must contain 3 to 5 monsters.");
                if (room.Chests == null || room.Chests.Count < 1 || room.Chests.Count > 3)
                    throw new InvalidOperationException("Encounter room must contain 1 to 3 chests.");

                ValidateEncounterMonsters(room);
                ValidateEncounterChests(room);
            }
        }

        private static void ValidateEncounterMonsters(EncounterRoomSave room)
        {
            var entityIds = new HashSet<string>(StringComparer.Ordinal);
            var positions = new HashSet<string>(StringComparer.Ordinal);
            var survivors = 0;
            for (var i = 0; i < room.Monsters.Count; i++)
            {
                var monster = room.Monsters[i];
                if (monster == null || string.IsNullOrWhiteSpace(monster.EntityId) || !entityIds.Add(monster.EntityId))
                    throw new InvalidOperationException("Encounter monster entity IDs must be non-empty and unique.");
                if (string.IsNullOrWhiteSpace(monster.DefinitionId))
                    throw new InvalidOperationException("Encounter monster definition ID is required.");
                if (monster.Position == null || monster.Position.X < 0 || monster.Position.X >= 3 || monster.Position.Y < 0 || monster.Position.Y >= 3)
                    throw new InvalidOperationException("Encounter monster position must be inside the 3x3 room.");
                var positionKey = monster.Position.X + ":" + monster.Position.Y;
                if (!positions.Add(positionKey)) throw new InvalidOperationException("Encounter monsters cannot share a room position.");
                if (monster.CurrentHealth < 0) throw new InvalidOperationException("Encounter monster health cannot be negative.");
                if (monster.CurrentHealth > 0) survivors++;
                if (monster.Statuses == null) throw new InvalidOperationException("Encounter monster statuses are required.");
                for (var statusIndex = 0; statusIndex < monster.Statuses.Count; statusIndex++)
                {
                    var status = monster.Statuses[statusIndex];
                    if (status == null || string.IsNullOrWhiteSpace(status.StatusId) || status.Stacks <= 0 || status.RemainingTurns <= 0)
                        throw new InvalidOperationException("Encounter monster status state is invalid.");
                }
            }

            if (room.Cleared && survivors != 0)
                throw new InvalidOperationException("Cleared encounter rooms cannot contain surviving monsters.");
            if (!room.Cleared && survivors == 0)
                throw new InvalidOperationException("Uncleared encounter rooms must contain a surviving monster.");
        }

        private static void ValidateEncounterChests(EncounterRoomSave room)
        {
            var transactionIds = new HashSet<string>(StringComparer.Ordinal);
            var indexes = new HashSet<int>();
            for (var i = 0; i < room.Chests.Count; i++)
            {
                var chest = room.Chests[i];
                if (chest == null) throw new InvalidOperationException("Encounter chest cannot be null.");
                if (chest.Index < 0 || !indexes.Add(chest.Index))
                    throw new InvalidOperationException("Encounter chest indexes must be non-negative and unique.");
                var expected = $"room:{room.ParentFloorIndex}:{room.RoomId}:chest:{chest.Index}";
                if (!string.Equals(chest.TransactionId, expected, StringComparison.Ordinal) || !transactionIds.Add(chest.TransactionId))
                    throw new InvalidOperationException("Encounter chest transaction IDs must be stable and unique.");
                if (chest.Claimed && !chest.Unlocked)
                    throw new InvalidOperationException("Claimed encounter chests must be unlocked.");
                if (room.Cleared && !chest.Unlocked)
                    throw new InvalidOperationException("Cleared encounter rooms must have unlocked chests.");
                if (!room.Cleared && chest.Unlocked)
                    throw new InvalidOperationException("Encounter chests must remain sealed while monsters survive.");
            }
        }

        private static void EnsureUnique(IEnumerable<string> values, string label)
        {
            if (values == null) throw new InvalidOperationException(label + " are required.");
            var seen = new HashSet<string>(StringComparer.Ordinal);
            foreach (var value in values)
            {
                if (string.IsNullOrWhiteSpace(value) || !seen.Add(value))
                    throw new InvalidOperationException(label + " must be non-empty and unique.");
            }
        }
    }
}
