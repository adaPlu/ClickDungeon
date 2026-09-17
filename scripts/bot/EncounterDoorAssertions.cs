using System;
using System.Runtime.CompilerServices;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

internal static class EncounterDoorAssertions
{
    [ModuleInitializer]
    internal static void Run()
    {
        var service = new EncounterRoomService();
        var inventory = new InventoryState();

        var closedRoom = new EncounterRoomRuntimeState(BuildLayout(EncounterDoorKind.Closed, EncounterRewardTier.Normal));
        Require(service.TryOpenDoor(closedRoom, inventory), "closed door must open freely");

        var lockedRoom = new EncounterRoomRuntimeState(BuildLayout(EncounterDoorKind.Locked, EncounterRewardTier.Higher));
        Require(!service.TryOpenDoor(lockedRoom, inventory), "locked door must reject missing key");

        inventory.Add("key-stack", ContentId.Parse("item.key.dungeon"), 2);
        Require(service.TryOpenDoor(lockedRoom, inventory), "locked door must open with dungeon key");
        Require(inventory.GetRequired("key-stack").Quantity == 1, "exactly one key must be consumed");
        Require(service.TryOpenDoor(lockedRoom, inventory), "already-open door is idempotent success");
        Require(inventory.GetRequired("key-stack").Quantity == 1, "re-entry must not consume another key");
    }

    private static EncounterRoomLayout BuildLayout(EncounterDoorKind kind, EncounterRewardTier tier)
    {
        return new EncounterRoomLayout(
            kind == EncounterDoorKind.Locked ? "bot-room-locked" : "bot-room-closed",
            2,
            new FloorCoordinate(2, 2),
            kind,
            123UL,
            ContentId.Parse("tile.floor_stone"),
            Array.Empty<EncounterMonsterSpawn>(),
            EncounterRewardMode.PremiumChest,
            tier);
    }

    private static void Require(bool condition, string message)
    {
        if (!condition) throw new InvalidOperationException(message);
    }
}
