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
            if (CanonicalTiles.All.Count != 25) throw new InvalidOperationException("Canonical tile registry is incomplete.");
            var stone = ContentId.Parse("tile.floor_stone");
            var floor = new FloorState(floorIndex, stone)
            {
                Start = new FloorCoordinate(0, rng.NextInt(0, FloorState.Height)),
                Exit = new FloorCoordinate(FloorState.Width - 1, rng.NextInt(0, FloorState.Height))
            };

            // The initial backbone is an unobstructed Manhattan route. Optional encounter-room placement may
            // add one doorway only after Start/Exit exist so doorway validation can preserve that route.
            var encounterGenerator = new EncounterRoomGenerator();
            var encounterRoom = encounterGenerator.TryGenerate(runSeed, floor, generationVersion);
            if (encounterRoom != null)
            {
                new EncounterRoomValidator().Validate(floor, encounterRoom);
                floor.CellAt(encounterRoom.Doorway).Structure = ContentId.Parse(
                    encounterRoom.DoorKind == EncounterDoorKind.Locked
                        ? "tile.door_locked"
                        : "tile.door_closed");
                floor.SetEncounterRoom(encounterRoom);
            }

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
