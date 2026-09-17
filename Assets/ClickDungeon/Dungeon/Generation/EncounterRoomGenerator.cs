using System;
using System.Collections.Generic;
using System.Globalization;
using ClickDungeon.Content.Canonical;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Dungeon.Generation
{
    public sealed class EncounterRoomGenerator
    {
        public EncounterRoomLayout TryGenerate(ulong runSeed, FloorState floor, int generationVersion)
        {
            if (floor == null) throw new ArgumentNullException(nameof(floor));
            if (floor.FloorIndex == 1) return null;

            var seed = MixSeed(runSeed, floor.FloorIndex, generationVersion);
            var rng = new DeterministicRng(seed);
            if (rng.NextInt(0, 100) >= 35) return null;

            var kind = rng.NextInt(0, 100) < 75
                ? EncounterDoorKind.Closed
                : EncounterDoorKind.Locked;
            var doorway = ChooseDoorway(floor, rng);
            var roomSeed = MixRoomSeed(seed, doorway);
            var roomRng = new DeterministicRng(roomSeed);
            var monsterCount = roomRng.NextInt(3, 6);
            var tier = kind == EncounterDoorKind.Locked
                ? EncounterRewardTier.Higher
                : EncounterRewardTier.Normal;
            var mode = roomRng.NextInt(0, 2) == 0
                ? EncounterRewardMode.PremiumChest
                : EncounterRewardMode.MultiChest;

            return BuildLayout(floor.FloorIndex, doorway, kind, roomSeed, monsterCount, mode, tier, roomRng);
        }

        private static FloorCoordinate ChooseDoorway(FloorState floor, DeterministicRng rng)
        {
            var candidates = new List<FloorCoordinate>();
            for (var x = 0; x < FloorState.Width; x++)
            for (var y = 0; y < FloorState.Height; y++)
            {
                var coordinate = new FloorCoordinate(x, y);
                if (coordinate == floor.Start || coordinate == floor.Exit) continue;
                if (floor.CellAt(coordinate).Structure.HasValue) continue;
                candidates.Add(coordinate);
            }

            if (candidates.Count == 0)
                throw new InvalidOperationException("No legal encounter-room doorway coordinate is available.");
            return candidates[rng.NextInt(0, candidates.Count)];
        }

        private static EncounterRoomLayout BuildLayout(
            int floorIndex,
            FloorCoordinate doorway,
            EncounterDoorKind kind,
            ulong roomSeed,
            int monsterCount,
            EncounterRewardMode rewardMode,
            EncounterRewardTier rewardTier,
            DeterministicRng roomRng)
        {
            var eligibleEnemies = new List<EnemyDefinition>();
            for (var i = 0; i < CanonicalEnemies.All.Count; i++)
            {
                var enemy = CanonicalEnemies.All[i];
                if (enemy.MinDepth <= floorIndex) eligibleEnemies.Add(enemy);
            }
            if (eligibleEnemies.Count == 0)
                throw new InvalidOperationException("No canonical enemies are eligible for this encounter-room depth.");

            var availablePositions = new List<FloorCoordinate>();
            var entrance = new FloorCoordinate(1, 2);
            for (var x = 0; x < EncounterRoomLayout.Width; x++)
            for (var y = 0; y < EncounterRoomLayout.Height; y++)
            {
                var position = new FloorCoordinate(x, y);
                if (position != entrance) availablePositions.Add(position);
            }

            var monsters = new List<EncounterMonsterSpawn>(monsterCount);
            for (var i = 0; i < monsterCount; i++)
            {
                var positionIndex = roomRng.NextInt(0, availablePositions.Count);
                var position = availablePositions[positionIndex];
                availablePositions.RemoveAt(positionIndex);
                var definition = eligibleEnemies[roomRng.NextInt(0, eligibleEnemies.Count)];
                var entityId = string.Format(
                    CultureInfo.InvariantCulture,
                    "encounter:{0}:{1}:{2}:{3}",
                    floorIndex,
                    doorway.X,
                    doorway.Y,
                    i);
                monsters.Add(new EncounterMonsterSpawn(entityId, definition.Id, position));
            }

            var roomId = string.Format(
                CultureInfo.InvariantCulture,
                "encounter-room:{0}:{1}:{2}:{3:X16}",
                floorIndex,
                doorway.X,
                doorway.Y,
                roomSeed);

            return new EncounterRoomLayout(
                roomId,
                floorIndex,
                doorway,
                kind,
                roomSeed,
                ContentId.Parse("tile.floor_stone"),
                monsters,
                rewardMode,
                rewardTier);
        }

        private static ulong MixSeed(ulong runSeed, int floorIndex, int generationVersion)
        {
            unchecked
            {
                var value = runSeed ^ ((ulong)(uint)floorIndex * 0xD6E8FEB86659FD93UL);
                value ^= (ulong)(uint)generationVersion * 0xA5A3564E27F8862FUL;
                value ^= value >> 32;
                value *= 0x9E3779B185EBCA87UL;
                value ^= value >> 29;
                return value;
            }
        }

        private static ulong MixRoomSeed(ulong seed, FloorCoordinate doorway)
        {
            unchecked
            {
                var value = seed ^ ((ulong)(uint)doorway.X << 32) ^ (uint)doorway.Y;
                value ^= 0xC2B2AE3D27D4EB4FUL;
                value ^= value >> 33;
                value *= 0xFF51AFD7ED558CCDUL;
                value ^= value >> 33;
                return value;
            }
        }
    }
}
