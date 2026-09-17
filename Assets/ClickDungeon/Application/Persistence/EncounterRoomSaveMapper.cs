using System;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Combat;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Save;

namespace ClickDungeon.Application.Persistence
{
    public static class EncounterRoomSaveMapper
    {
        public static EncounterRoomSave ToSave(EncounterRoomRuntimeState room)
        {
            if (room == null) throw new ArgumentNullException(nameof(room));

            var result = new EncounterRoomSave
            {
                RoomId = room.Layout.RoomId,
                ParentFloorIndex = room.Layout.FloorIndex,
                Doorway = new CoordinateSave { X = room.Layout.Doorway.X, Y = room.Layout.Doorway.Y },
                DoorKind = room.Layout.DoorKind.ToString(),
                DoorOpened = room.IsDoorOpen,
                RoomSeed = room.Layout.RoomSeed,
                Cleared = room.IsCleared,
                RewardMode = room.Layout.RewardMode.ToString(),
                RewardTier = room.Layout.RewardTier.ToString(),
            };

            for (var i = 0; i < room.Monsters.Count; i++)
            {
                var monster = room.Monsters[i];
                var savedMonster = new EnemyStateSave
                {
                    EntityId = monster.EntityId,
                    DefinitionId = monster.DefinitionId.Value,
                    Position = new CoordinateSave { X = monster.Position.X, Y = monster.Position.Y },
                    CurrentHealth = monster.Combatant.CurrentHealth,
                };

                for (var statusIndex = 0; statusIndex < monster.Combatant.Statuses.Count; statusIndex++)
                {
                    var status = monster.Combatant.Statuses[statusIndex];
                    savedMonster.Statuses.Add(new StatusEffectSave
                    {
                        StatusId = status.Kind.ToString(),
                        Stacks = status.Stacks,
                        RemainingTurns = status.RemainingTurns,
                    });
                }

                result.Monsters.Add(savedMonster);
            }

            for (var i = 0; i < room.Chests.Count; i++)
            {
                var chest = room.Chests[i];
                result.Chests.Add(new EncounterChestSave
                {
                    Index = chest.Index,
                    TransactionId = chest.TransactionId,
                    Unlocked = chest.IsUnlocked,
                    Claimed = chest.IsClaimed,
                });
            }

            return result;
        }

        public static EncounterRoomRuntimeState FromSave(EncounterRoomSave save, EncounterRoomLayout layout)
        {
            if (save == null) throw new ArgumentNullException(nameof(save));
            if (layout == null) throw new ArgumentNullException(nameof(layout));
            ValidateImmutableIdentity(save, layout);

            var room = new EncounterRoomRuntimeState(layout);

            for (var i = 0; i < save.Monsters.Count; i++)
            {
                var savedMonster = save.Monsters[i];
                var runtimeMonster = FindMonster(room, savedMonster.EntityId);
                if (runtimeMonster.DefinitionId.Value != savedMonster.DefinitionId ||
                    runtimeMonster.Position.X != savedMonster.Position.X ||
                    runtimeMonster.Position.Y != savedMonster.Position.Y)
                    throw new InvalidOperationException("Saved encounter monster does not match the generated layout.");
                if (savedMonster.CurrentHealth < 0 || savedMonster.CurrentHealth > runtimeMonster.Combatant.MaxHealth)
                    throw new InvalidOperationException("Saved encounter monster health is outside valid bounds.");

                var damage = runtimeMonster.Combatant.MaxHealth - savedMonster.CurrentHealth;
                if (damage > 0) runtimeMonster.Combatant.ApplyDamage(damage);

                if (savedMonster.Statuses == null)
                    throw new InvalidOperationException("Saved encounter monster statuses are required.");
                for (var statusIndex = 0; statusIndex < savedMonster.Statuses.Count; statusIndex++)
                {
                    var savedStatus = savedMonster.Statuses[statusIndex];
                    if (!Enum.TryParse(savedStatus.StatusId, false, out StatusEffectKind kind))
                        throw new InvalidOperationException("Saved encounter monster status is unknown.");
                    runtimeMonster.Combatant.ApplyStatus(kind, savedStatus.RemainingTurns, savedStatus.Stacks);
                }
            }

            if (save.DoorOpened) room.MarkDoorOpen();
            if (save.Cleared) room.MarkClearedAndUnlockChests();

            for (var i = 0; i < save.Chests.Count; i++)
            {
                var savedChest = save.Chests[i];
                var runtimeChest = room.GetChest(savedChest.Index);
                if (!string.Equals(runtimeChest.TransactionId, savedChest.TransactionId, StringComparison.Ordinal))
                    throw new InvalidOperationException("Saved encounter chest transaction does not match the generated room.");
                if (savedChest.Unlocked && !runtimeChest.IsUnlocked) runtimeChest.Unlock();
                if (savedChest.Claimed) runtimeChest.MarkClaimed();
            }

            if (room.IsCleared != save.Cleared)
                throw new InvalidOperationException("Saved encounter clear state could not be restored exactly.");
            return room;
        }

        private static void ValidateImmutableIdentity(EncounterRoomSave save, EncounterRoomLayout layout)
        {
            if (!string.Equals(save.RoomId, layout.RoomId, StringComparison.Ordinal) ||
                save.ParentFloorIndex != layout.FloorIndex ||
                save.Doorway == null ||
                save.Doorway.X != layout.Doorway.X ||
                save.Doorway.Y != layout.Doorway.Y ||
                !string.Equals(save.DoorKind, layout.DoorKind.ToString(), StringComparison.Ordinal) ||
                save.RoomSeed != layout.RoomSeed ||
                !string.Equals(save.RewardMode, layout.RewardMode.ToString(), StringComparison.Ordinal) ||
                !string.Equals(save.RewardTier, layout.RewardTier.ToString(), StringComparison.Ordinal))
                throw new InvalidOperationException("Saved encounter room identity does not match the generated layout.");

            if (save.Monsters == null || save.Monsters.Count != layout.Monsters.Count)
                throw new InvalidOperationException("Saved encounter monster roster does not match the generated layout.");
            if (save.Chests == null)
                throw new InvalidOperationException("Saved encounter chest state is required.");
        }

        private static EncounterMonsterRuntimeState FindMonster(EncounterRoomRuntimeState room, string entityId)
        {
            for (var i = 0; i < room.Monsters.Count; i++)
                if (string.Equals(room.Monsters[i].EntityId, entityId, StringComparison.Ordinal))
                    return room.Monsters[i];
            throw new InvalidOperationException("Saved encounter monster is not present in the generated layout.");
        }
    }
}
