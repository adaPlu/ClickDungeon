using System;
using System.Runtime.CompilerServices;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Application.Persistence;
using ClickDungeon.Combat;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Save;

internal static class EncounterSaveAssertions
{
    [ModuleInitializer]
    internal static void Run()
    {
        VerifyMigration();
        VerifyRoundTrip();
    }

    private static void VerifyMigration()
    {
        var legacy = new RunSave
        {
            SchemaVersion = 1,
            EncounterRooms = null,
        };
        var migrated = new RunSaveV1ToV2Migration().Migrate(legacy);
        Require(migrated.SchemaVersion == 2, "v1->v2 migration must advance the schema");
        Require(migrated.EncounterRooms != null && migrated.EncounterRooms.Count == 0,
            "v1->v2 migration must initialize an empty encounter-room collection");
    }

    private static void VerifyRoundTrip()
    {
        var layout = BuildLayout();
        var room = new EncounterRoomRuntimeState(layout);
        room.MarkDoorOpen();

        room.Monsters[0].Combatant.ApplyDamage(3);
        room.Monsters[0].Combatant.ApplyStatus(StatusEffectKind.Poison, 4, 2);
        Require(room.TryMarkMonsterDefeated(room.Monsters[1].EntityId), "setup defeat must succeed");
        Require(!room.IsCleared, "partially cleared room must remain uncleared");

        var save = EncounterRoomSaveMapper.ToSave(room);
        var restored = EncounterRoomSaveMapper.FromSave(save, layout);

        Require(restored.IsDoorOpen, "door-open state must round-trip");
        Require(!restored.IsCleared, "partial-clear state must round-trip");
        Require(restored.Monsters.Count == room.Monsters.Count, "monster roster size must round-trip");
        Require(restored.Chests.Count == room.Chests.Count, "chest roster size must round-trip");

        for (var i = 0; i < room.Monsters.Count; i++)
        {
            var before = room.Monsters[i];
            var after = restored.Monsters[i];
            Require(after.EntityId == before.EntityId, "monster identity must round-trip");
            Require(after.DefinitionId == before.DefinitionId, "monster definition must round-trip");
            Require(after.Position == before.Position, "monster position must round-trip");
            Require(after.Combatant.CurrentHealth == before.Combatant.CurrentHealth,
                "monster health must round-trip exactly");
            Require(after.Combatant.Statuses.Count == before.Combatant.Statuses.Count,
                "monster status count must round-trip");
            for (var statusIndex = 0; statusIndex < before.Combatant.Statuses.Count; statusIndex++)
            {
                var beforeStatus = before.Combatant.Statuses[statusIndex];
                var afterStatus = after.Combatant.Statuses[statusIndex];
                Require(afterStatus.Kind == beforeStatus.Kind, "status kind must round-trip");
                Require(afterStatus.RemainingTurns == beforeStatus.RemainingTurns, "status turns must round-trip");
                Require(afterStatus.Stacks == beforeStatus.Stacks, "status stacks must round-trip");
            }
        }

        for (var i = 0; i < restored.Chests.Count; i++)
        {
            Require(!restored.Chests[i].IsUnlocked, "partial room rewards must remain sealed after restore");
            Require(!restored.Chests[i].IsClaimed, "partial room rewards must remain unclaimed after restore");
        }
    }

    private static EncounterRoomLayout BuildLayout()
    {
        var monsters = new[]
        {
            new EncounterMonsterSpawn("save-room-monster-0", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(0, 0)),
            new EncounterMonsterSpawn("save-room-monster-1", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(1, 0)),
            new EncounterMonsterSpawn("save-room-monster-2", ContentId.Parse("enemy.crowned_slime"), new FloorCoordinate(2, 0)),
        };

        return new EncounterRoomLayout(
            "save-room",
            2,
            new FloorCoordinate(2, 2),
            EncounterDoorKind.Locked,
            0x1234UL,
            ContentId.Parse("tile.floor_stone"),
            monsters,
            EncounterRewardMode.MultiChest,
            EncounterRewardTier.Higher);
    }

    private static void Require(bool condition, string message)
    {
        if (!condition) throw new InvalidOperationException(message);
    }
}
