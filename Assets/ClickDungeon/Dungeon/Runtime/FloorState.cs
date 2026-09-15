using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Dungeon.Runtime
{
    public sealed class FloorState
    {
        public const int Width = 5;
        public const int Height = 5;

        private readonly FloorCell[,] cells = new FloorCell[Width, Height];
        private readonly List<FloorLink> links = new List<FloorLink>();

        public int FloorIndex { get; }
        public FloorCoordinate Start { get; set; }
        public FloorCoordinate Exit { get; set; }
        public IReadOnlyList<FloorLink> Links => links;

        public FloorState(int floorIndex, ContentId baseTerrain)
        {
            if (floorIndex < 1) throw new ArgumentOutOfRangeException(nameof(floorIndex));
            FloorIndex = floorIndex;
            for (var x = 0; x < Width; x++)
            for (var y = 0; y < Height; y++)
                cells[x, y] = new FloorCell(baseTerrain);
        }

        public bool IsInBounds(FloorCoordinate c) => c.X >= 0 && c.X < Width && c.Y >= 0 && c.Y < Height;

        public FloorCell CellAt(FloorCoordinate c)
        {
            if (!IsInBounds(c)) throw new ArgumentOutOfRangeException(nameof(c));
            return cells[c.X, c.Y];
        }

        public void AddLink(FloorLink link) => links.Add(link);
    }
}
