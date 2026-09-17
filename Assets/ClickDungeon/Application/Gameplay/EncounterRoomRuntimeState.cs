using System;
using System.Collections.Generic;
using ClickDungeon.Combat;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Application.Gameplay
{
    public sealed class EncounterMonsterRuntimeState
    {
        public EncounterMonsterSpawn Spawn { get; }
        public string EntityId => Spawn.EntityId;
        public ContentId DefinitionId => Spawn.DefinitionId;
        public FloorCoordinate Position => Spawn.Position;
        public CombatantState Combatant { get; }
        public bool IsDefeated => Combatant.IsDefeated;

        public EncounterMonsterRuntimeState(EncounterMonsterSpawn spawn)
        {
            Spawn = spawn ?? throw new ArgumentNullException(nameof(spawn));
            var definition = CanonicalEnemies.GetRequired(spawn.DefinitionId);
            Combatant = new CombatantState(
                spawn.EntityId,
                definition.BaseHealth,
                new CombatStats(definition.Attack, definition.Defense, definition.Initiative));
        }

        public bool MarkDefeated()
        {
            if (Combatant.IsDefeated) return false;
            Combatant.ApplyDamage(Combatant.CurrentHealth);
            return true;
        }
    }

    public sealed class EncounterChestRuntimeState
    {
        public int Index { get; }
        public string TransactionId { get; }
        public bool IsUnlocked { get; private set; }
        public bool IsClaimed { get; private set; }

        public EncounterChestRuntimeState(int index, string transactionId)
        {
            if (index < 0) throw new ArgumentOutOfRangeException(nameof(index));
            if (string.IsNullOrWhiteSpace(transactionId)) throw new ArgumentException("Transaction ID is required.", nameof(transactionId));
            Index = index;
            TransactionId = transactionId;
        }

        public void Unlock() => IsUnlocked = true;

        public void MarkClaimed()
        {
            if (!IsUnlocked) throw new InvalidOperationException("Locked encounter chest cannot be claimed.");
            IsClaimed = true;
        }
    }

    public sealed class EncounterRoomRuntimeState
    {
        private readonly List<EncounterMonsterRuntimeState> monsters = new List<EncounterMonsterRuntimeState>();
        private readonly List<EncounterChestRuntimeState> chests = new List<EncounterChestRuntimeState>();

        public EncounterRoomLayout Layout { get; }
        public FloorState ParentFloor { get; }
        public bool IsDoorOpen { get; private set; }
        public bool IsCleared { get; private set; }
        public IReadOnlyList<EncounterMonsterRuntimeState> Monsters => monsters;
        public IReadOnlyList<EncounterChestRuntimeState> Chests => chests;

        public EncounterRoomRuntimeState(EncounterRoomLayout layout, FloorState parentFloor = null)
        {
            Layout = layout ?? throw new ArgumentNullException(nameof(layout));
            if (parentFloor != null && parentFloor.FloorIndex != layout.FloorIndex)
                throw new ArgumentException("Parent floor does not match encounter-room floor.", nameof(parentFloor));
            ParentFloor = parentFloor;

            for (var i = 0; i < layout.Monsters.Count; i++)
                monsters.Add(new EncounterMonsterRuntimeState(layout.Monsters[i]));

            var chestCount = layout.RewardMode == EncounterRewardMode.PremiumChest
                ? 1
                : (layout.RewardTier == EncounterRewardTier.Higher ? 3 : 2);
            for (var i = 0; i < chestCount; i++)
            {
                var transactionId = $"room:{layout.FloorIndex}:{layout.RoomId}:chest:{i}";
                chests.Add(new EncounterChestRuntimeState(i, transactionId));
            }
        }

        public void MarkDoorOpen()
        {
            IsDoorOpen = true;
            if (ParentFloor != null)
                ParentFloor.CellAt(Layout.Doorway).Structure = ContentId.Parse("tile.door_open");
        }

        public bool TryMarkMonsterDefeated(string entityId)
        {
            if (string.IsNullOrWhiteSpace(entityId)) return false;
            for (var i = 0; i < monsters.Count; i++)
            {
                if (!string.Equals(monsters[i].EntityId, entityId, StringComparison.Ordinal)) continue;
                return monsters[i].MarkDefeated();
            }
            return false;
        }

        public void MarkClearedAndUnlockChests()
        {
            for (var i = 0; i < monsters.Count; i++)
                if (!monsters[i].IsDefeated)
                    throw new InvalidOperationException("Encounter room cannot clear while monsters survive.");

            IsCleared = true;
            for (var i = 0; i < chests.Count; i++) chests[i].Unlock();
        }

        public EncounterChestRuntimeState GetChest(int index)
        {
            if (index < 0 || index >= chests.Count) throw new ArgumentOutOfRangeException(nameof(index));
            return chests[index];
        }
    }
}
