using System;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

namespace ClickDungeon.Application.Gameplay
{
    public sealed class EncounterRoomService
    {
        private static readonly ContentId DungeonKeyId = ContentId.Parse("item.key.dungeon");

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
    }
}
