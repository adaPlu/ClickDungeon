using System;

namespace ClickDungeon.Dungeon.Generation
{
    // Small xorshift64* generator: deterministic and platform-independent for integer operations.
    public sealed class DeterministicRng
    {
        private ulong state;

        public DeterministicRng(ulong seed)
        {
            state = seed == 0 ? 0x9E3779B97F4A7C15UL : seed;
        }

        public uint NextUInt()
        {
            var x = state;
            x ^= x >> 12;
            x ^= x << 25;
            x ^= x >> 27;
            state = x;
            return (uint)((x * 2685821657736338717UL) >> 32);
        }

        public int NextInt(int minInclusive, int maxExclusive)
        {
            if (maxExclusive <= minInclusive) throw new ArgumentOutOfRangeException(nameof(maxExclusive));
            var range = (uint)(maxExclusive - minInclusive);
            return minInclusive + (int)(NextUInt() % range);
        }
    }
}
