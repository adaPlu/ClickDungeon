using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Dungeon.Runtime
{
    public sealed class EncounterRoomLayout
    {
        public const int Width = 3;
        public const int Height = 3;

        private readonly FloorCell[,] cells = new FloorCell[Width, Height];
        private readonly EncounterMonsterSpawn[] monsters;

        public string RoomId { get; }
        public int FloorIndex { get; }
        public FloorCoordinate Doorway { get; }
        public EncounterDoorKind DoorKind { get; }
        public ulong RoomSeed { get; }
        public IReadOnlyList<EncounterMonsterSpawn> Monsters => monsters;
        public EncounterRewardMode RewardMode { get; }
        public EncounterRewardTier RewardTier { get; }

        public EncounterRoomLayout(
            string roomId,
            int floorIndex,
            FloorCoordinate doorway,
            EncounterDoorKind doorKind,
            ulong roomSeed,
            ContentId baseTerrain,
            IReadOnlyList<EncounterMonsterSpawn> monsterSpawns,
            EncounterRewardMode rewardMode,
            EncounterRewardTier rewardTier)
        {
            if (string.IsNullOrWhiteSpace(roomId)) throw new ArgumentException("Room ID is required.", nameof(roomId));
            if (floorIndex < 1) throw new ArgumentOutOfRangeException(nameof(floorIndex));
            if (monsterSpawns == null) throw new ArgumentNullException(nameof(monsterSpawns));

            RoomId = roomId;
            FloorIndex = floorIndex;
            Doorway = doorway;
            DoorKind = doorKind;
            RoomSeed = roomSeed;
            RewardMode = rewardMode;
            RewardTier = rewardTier;

            monsters = new EncounterMonsterSpawn[monsterSpawns.Count];
            for (var i = 0; i < monsterSpawns.Count; i++) monsters[i] = monsterSpawns[i];

            for (var x = 0; x < Width; x++)
            for (var y = 0; y < Height; y++)
                cells[x, y] = new FloorCell(baseTerrain);
        }

        public bool IsInBounds(FloorCoordinate coordinate) =>
            coordinate.X >= 0 && coordinate.X < Width && coordinate.Y >= 0 && coordinate.Y < Height;

        public FloorCell CellAt(FloorCoordinate coordinate)
        {
            if (!IsInBounds(coordinate)) throw new ArgumentOutOfRangeException(nameof(coordinate));
            return cells[coordinate.X, coordinate.Y];
        }
    }
}
