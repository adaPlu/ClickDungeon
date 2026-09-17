using System;
using System.Runtime.CompilerServices;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

internal static class EncounterRewardAssertions
{
    [ModuleInitializer]
    internal static void Run()
    {
        var room = new EncounterRoomRuntimeState(BuildLayout());
        var service = new EncounterRoomService();
        var inventory = new InventoryState();
        var currency = new CurrencyState();
        var ledger = new RewardLedger();
        var grants = new RewardGrantService(ledger, inventory, currency);

        Require(!service.TryClaimChest(room, 0, grants, out _), "reward must be sealed while monsters survive");

        for (var i = 0; i < room.Monsters.Count; i++)
            Require(service.RecordMonsterDefeated(room, room.Monsters[i].EntityId), "monster defeat must be recorded");

        Require(room.IsCleared, "last monster must clear room");
        for (var i = 0; i < room.Chests.Count; i++)
            Require(room.Chests[i].IsUnlocked, "clearing room must unlock chests");

        Require(service.TryClaimChest(room, 0, grants, out var first), "first room chest claim must succeed");
        var expectedId = $"room:{room.Layout.FloorIndex}:{room.Layout.RoomId}:chest:0";
        Require(first.TransactionId == expectedId, "room transaction ID must be stable");
        Require(!service.TryClaimChest(room, 0, grants, out _), "duplicate room claim must fail");
    }

    private static EncounterRoomLayout BuildLayout()
    {
        var monsters = new[]
        {
            new EncounterMonsterSpawn("bot-room-monster-0", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(0, 0)),
            new EncounterMonsterSpawn("bot-room-monster-1", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(1, 0)),
            new EncounterMonsterSpawn("bot-room-monster-2", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(2, 0)),
        };

        return new EncounterRoomLayout(
            "bot-reward-room",
            2,
            new FloorCoordinate(2, 2),
            EncounterDoorKind.Closed,
            456UL,
            ContentId.Parse("tile.floor_stone"),
            monsters,
            EncounterRewardMode.MultiChest,
            EncounterRewardTier.Normal);
    }

    private static void Require(bool condition, string message)
    {
        if (!condition) throw new InvalidOperationException(message);
    }
}
