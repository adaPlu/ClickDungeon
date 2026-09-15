using System;
using System.Collections.Generic;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Dungeon.Generation
{
    public static class FloorValidator
    {
        public static void Validate(FloorState floor)
        {
            if (!floor.IsInBounds(floor.Start) || !floor.IsInBounds(floor.Exit))
                throw new InvalidOperationException("Start and exit must be in bounds.");
            ValidateReachableExit(floor);
            ValidateKeyBeforeMandatoryLock(floor);
            ValidateLinks(floor);
        }

        public static void ValidateReachableExit(FloorState floor)
        {
            var queue = new Queue<FloorCoordinate>();
            var seen = new HashSet<FloorCoordinate>();
            queue.Enqueue(floor.Start);
            seen.Add(floor.Start);
            while (queue.Count > 0)
            {
                var current = queue.Dequeue();
                if (current == floor.Exit) return;
                foreach (var next in Neighbors(current))
                {
                    if (!floor.IsInBounds(next) || seen.Contains(next) || IsBlocking(floor.CellAt(next))) continue;
                    seen.Add(next);
                    queue.Enqueue(next);
                }
            }
            throw new InvalidOperationException("Exit is unreachable.");
        }

        public static void ValidateKeyBeforeMandatoryLock(FloorState floor)
        {
            // A mandatory locked stair/door is valid only if at least one key is reachable while locks remain blocked.
            var hasMandatoryLock = false;
            for (var x = 0; x < FloorState.Width; x++)
            for (var y = 0; y < FloorState.Height; y++)
            {
                var cell = floor.CellAt(new FloorCoordinate(x, y));
                var structure = cell.Structure?.Value;
                if (structure == "tile.stair_down_locked" || structure == "tile.door_locked") hasMandatoryLock = true;
            }
            if (!hasMandatoryLock) return;

            var queue = new Queue<FloorCoordinate>();
            var seen = new HashSet<FloorCoordinate>();
            queue.Enqueue(floor.Start);
            seen.Add(floor.Start);
            while (queue.Count > 0)
            {
                var current = queue.Dequeue();
                if (floor.CellAt(current).Content?.Value == "tile.key") return;
                foreach (var next in Neighbors(current))
                {
                    if (!floor.IsInBounds(next) || seen.Contains(next) || IsBlocking(floor.CellAt(next))) continue;
                    seen.Add(next);
                    queue.Enqueue(next);
                }
            }
            throw new InvalidOperationException("Mandatory lock has no reachable key before it.");
        }

        public static void ValidateLinks(FloorState floor)
        {
            var ids = new HashSet<string>(StringComparer.Ordinal);
            foreach (var link in floor.Links)
            {
                if (!floor.IsInBounds(link.Source) || !floor.IsInBounds(link.Target))
                    throw new InvalidOperationException("Floor link endpoint is out of bounds.");
                if (!ids.Add(link.Id.Value)) throw new InvalidOperationException("Duplicate floor link ID.");
                if (link.Kind != FloorLinkKind.Teleport && link.Kind != FloorLinkKind.PressurePlate)
                    throw new InvalidOperationException("Unsupported floor link kind.");
            }
        }

        private static IEnumerable<FloorCoordinate> Neighbors(FloorCoordinate c)
        {
            yield return new FloorCoordinate(c.X + 1, c.Y);
            yield return new FloorCoordinate(c.X - 1, c.Y);
            yield return new FloorCoordinate(c.X, c.Y + 1);
            yield return new FloorCoordinate(c.X, c.Y - 1);
        }

        private static bool IsBlocking(FloorCell cell)
        {
            var id = cell.Structure?.Value;
            return id == "tile.wall" || id == "tile.wall_corner" || id == "tile.door_locked" ||
                   id == "tile.stair_down_locked" || id == "tile.stair_up_locked";
        }
    }
}
