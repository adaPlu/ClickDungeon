using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Progression
{
    public sealed class LootCandidate
    {
        public ContentId ItemId { get; }
        public int Weight { get; }
        public int MinQuantity { get; }
        public int MaxQuantity { get; }

        public LootCandidate(ContentId itemId, int weight, int minQuantity, int maxQuantity)
        {
            if (weight <= 0) throw new ArgumentOutOfRangeException(nameof(weight));
            if (minQuantity <= 0 || maxQuantity < minQuantity) throw new ArgumentOutOfRangeException(nameof(minQuantity));
            ItemId = itemId;
            Weight = weight;
            MinQuantity = minQuantity;
            MaxQuantity = maxQuantity;
        }
    }

    public sealed class LootResult
    {
        public ContentId ItemId { get; }
        public int Quantity { get; }

        public LootResult(ContentId itemId, int quantity)
        {
            ItemId = itemId;
            Quantity = quantity;
        }
    }

    public sealed class LootResolver
    {
        public LootResult Resolve(IReadOnlyList<LootCandidate> candidates, ulong seed)
        {
            if (candidates == null || candidates.Count == 0) throw new ArgumentException("Loot candidates are required.", nameof(candidates));

            var totalWeight = 0L;
            for (var i = 0; i < candidates.Count; i++) totalWeight = checked(totalWeight + candidates[i].Weight);

            var mixed = MixSeed(seed);
            var choice = (long)(mixed % (ulong)totalWeight);
            LootCandidate selected = null;
            for (var i = 0; i < candidates.Count; i++)
            {
                if (choice < candidates[i].Weight)
                {
                    selected = candidates[i];
                    break;
                }
                choice -= candidates[i].Weight;
            }
            if (selected == null) selected = candidates[candidates.Count - 1];

            var quantityRange = selected.MaxQuantity - selected.MinQuantity + 1;
            var quantitySeed = MixSeed(mixed ^ 0x9E3779B97F4A7C15UL);
            var quantity = selected.MinQuantity + (int)(quantitySeed % (ulong)quantityRange);
            return new LootResult(selected.ItemId, quantity);
        }

        private static ulong MixSeed(ulong seed)
        {
            var z = seed + 0x9E3779B97F4A7C15UL;
            z = (z ^ (z >> 30)) * 0xBF58476D1CE4E5B9UL;
            z = (z ^ (z >> 27)) * 0x94D049BB133111EBUL;
            return z ^ (z >> 31);
        }
    }
}
