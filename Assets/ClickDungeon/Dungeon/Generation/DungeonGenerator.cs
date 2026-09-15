using System;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Dungeon.Generation
{
    public sealed class DungeonGenerator
    {
        public FloorState Generate(ulong runSeed, int floorIndex, int generationVersion)
        {
            var seed = MixSeed(runSeed, floorIndex, generationVersion);
            var rng = new DeterministicRng(seed);
            // Touch the canonical registry so generation fails during integration if stone content disappears.
            if (CanonicalTiles.All.Count != 24) throw new InvalidOperationException("Canonical tile registry is incomplete.");
            var stone = ContentId.Parse("tile.floor_stone");
            var floor = new FloorState(floorIndex, stone)
            {
                Start = new FloorCoordinate(0, rng.NextInt(0, FloorState.Height)),
                Exit = new FloorCoordinate(FloorState.Width - 1, rng.NextInt(0, FloorState.Height))
            };

            // The initial backbone is an unobstructed Manhattan route. Later placement may add optional content
            // only if FloorValidator still proves the floor valid.
            return floor;
        }

        private static ulong MixSeed(ulong runSeed, int floorIndex, int generationVersion)
        {
            unchecked
            {
                var value = runSeed ^ ((ulong)(uint)floorIndex * 0x9E3779B185EBCA87UL);
                value ^= (ulong)(uint)generationVersion * 0xC2B2AE3D27D4EB4FUL;
                value ^= value >> 33;
                value *= 0xFF51AFD7ED558CCDUL;
                value ^= value >> 33;
                return value;
            }
        }
    }
}
