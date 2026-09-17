using System;
using System.Collections.Generic;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Dungeon.Generation
{
    public sealed class EncounterRoomValidator
    {
        public void Validate(FloorState floor, EncounterRoomLayout room)
        {
            if (floor == null) throw new ArgumentNullException(nameof(floor));
            if (room == null) throw new ArgumentNullException(nameof(room));
            if (room.FloorIndex != floor.FloorIndex)
                throw new InvalidOperationException("Encounter room parent floor mismatch.");
            if (!floor.IsInBounds(room.Doorway))
                throw new InvalidOperationException("Encounter room doorway is out of bounds.");
            if (room.Doorway == floor.Start)
                throw new InvalidOperationException("Encounter room doorway cannot overwrite Start.");
            if (room.Doorway == floor.Exit)
                throw new InvalidOperationException("Encounter room doorway cannot overwrite Exit.");
            if (EncounterRoomLayout.Width != 3 || EncounterRoomLayout.Height != 3)
                throw new InvalidOperationException("Encounter room must be 3x3.");
            if (room.Monsters.Count < 3 || room.Monsters.Count > 5)
                throw new InvalidOperationException("Encounter room monster count must be between 3 and 5.");
            if (room.DoorKind == EncounterDoorKind.Locked && room.RewardTier != EncounterRewardTier.Higher)
                throw new InvalidOperationException("Locked encounter rooms require EncounterRewardTier.Higher.");

            var positions = new HashSet<FloorCoordinate>();
            var entrance = new FloorCoordinate(1, 2);
            for (var i = 0; i < room.Monsters.Count; i++)
            {
                var position = room.Monsters[i].Position;
                if (!room.IsInBounds(position))
                    throw new InvalidOperationException("Encounter monster position is out of bounds.");
                if (position == entrance)
                    throw new InvalidOperationException("Encounter monster cannot occupy the room entrance.");
                if (!positions.Add(position))
                    throw new InvalidOperationException("Encounter monster positions must be unique.");
            }
        }
    }
}
