using System;
using System.Collections.Generic;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

namespace ClickDungeon.Application.Gameplay
{
    public sealed class EncounterRoomService
    {
        private static readonly ContentId DungeonKeyId = ContentId.Parse("item.key.dungeon");
        private readonly LootResolver lootResolver = new LootResolver();

        public bool TryOpenDoor(EncounterRoomRuntimeState room, InventoryState inventory)
        {
            if (room == null) throw new ArgumentNullException(nameof(room));
            if (inventory == null) throw new ArgumentNullException(nameof(inventory));
            if (room.IsDoorOpen) return true;

            if (room.Layout.DoorKind == EncounterDoorKind.Locked &&
                !inventory.TryRemoveDefinition(DungeonKeyId, 1))
                return false;

            room.MarkDoorOpen();
            return true;
        }

        public bool RecordMonsterDefeated(EncounterRoomRuntimeState room, string entityId)
        {
            if (room == null) throw new ArgumentNullException(nameof(room));
            if (!room.TryMarkMonsterDefeated(entityId)) return false;

            for (var i = 0; i < room.Monsters.Count; i++)
                if (!room.Monsters[i].IsDefeated)
                    return true;

            room.MarkClearedAndUnlockChests();
            return true;
        }

        public bool TryClaimChest(
            EncounterRoomRuntimeState room,
            int chestIndex,
            RewardGrantService grants,
            out RewardGrant reward)
        {
            if (room == null) throw new ArgumentNullException(nameof(room));
            if (grants == null) throw new ArgumentNullException(nameof(grants));
            reward = null;

            var chest = room.GetChest(chestIndex);
            if (!room.IsCleared || !chest.IsUnlocked || chest.IsClaimed) return false;

            var loot = ResolveRoomLoot(room, chestIndex);
            var transactionId = chest.TransactionId;
            var candidate = new RewardGrant(
                transactionId,
                transactionId + ":item",
                loot.ItemId,
                loot.Quantity);
            if (!grants.Grant(candidate)) return false;

            chest.MarkClaimed();
            reward = candidate;
            return true;
        }

        private LootResult ResolveRoomLoot(EncounterRoomRuntimeState room, int chestIndex)
        {
            var candidates = new List<LootCandidate>();
            for (var i = 0; i < CanonicalItems.All.Count; i++)
            {
                var item = CanonicalItems.All[i];
                if (item.Category == ItemCategory.Currency) continue;
                var weight = WeightFor(item.Rarity, room.Layout.RewardTier);
                if (weight <= 0) continue;
                candidates.Add(new LootCandidate(item.Id, weight, 1, 1));
            }

            if (candidates.Count == 0)
                throw new InvalidOperationException("No canonical items are eligible for encounter-room loot.");

            unchecked
            {
                var seed = room.Layout.RoomSeed ^ ((ulong)(uint)chestIndex * 0x9E3779B97F4A7C15UL);
                return lootResolver.Resolve(candidates, seed);
            }
        }

        private static int WeightFor(ItemRarity rarity, EncounterRewardTier tier)
        {
            if (tier == EncounterRewardTier.Higher)
            {
                switch (rarity)
                {
                    case ItemRarity.Uncommon: return 45;
                    case ItemRarity.Rare: return 30;
                    case ItemRarity.Epic: return 20;
                    case ItemRarity.Legendary: return 5;
                    default: return 0;
                }
            }

            switch (rarity)
            {
                case ItemRarity.Common: return 60;
                case ItemRarity.Uncommon: return 30;
                case ItemRarity.Rare: return 10;
                default: return 0;
            }
        }
    }
}
