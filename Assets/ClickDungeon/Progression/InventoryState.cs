using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Progression
{
    public sealed class InventoryEntry
    {
        public string InstanceId { get; }
        public ContentId DefinitionId { get; }
        public int Quantity { get; private set; }

        public InventoryEntry(string instanceId, ContentId definitionId, int quantity)
        {
            if (string.IsNullOrWhiteSpace(instanceId)) throw new ArgumentException("Instance ID is required.", nameof(instanceId));
            if (quantity <= 0) throw new ArgumentOutOfRangeException(nameof(quantity));
            InstanceId = instanceId;
            DefinitionId = definitionId;
            Quantity = quantity;
        }

        internal void Increase(int quantity)
        {
            if (quantity <= 0) throw new ArgumentOutOfRangeException(nameof(quantity));
            Quantity = checked(Quantity + quantity);
        }

        internal void Decrease(int quantity)
        {
            if (quantity <= 0 || quantity > Quantity) throw new ArgumentOutOfRangeException(nameof(quantity));
            Quantity -= quantity;
        }
    }

    public sealed class InventoryState
    {
        private readonly Dictionary<string, InventoryEntry> entries = new Dictionary<string, InventoryEntry>(StringComparer.Ordinal);
        public IReadOnlyCollection<InventoryEntry> Entries => entries.Values;

        public void Add(string instanceId, ContentId definitionId, int quantity)
        {
            if (entries.TryGetValue(instanceId, out var existing))
            {
                if (existing.DefinitionId != definitionId) throw new InvalidOperationException("Instance ID already belongs to another definition.");
                existing.Increase(quantity);
                return;
            }
            entries.Add(instanceId, new InventoryEntry(instanceId, definitionId, quantity));
        }

        public bool Remove(string instanceId, int quantity)
        {
            if (!entries.TryGetValue(instanceId, out var existing) || quantity <= 0 || quantity > existing.Quantity) return false;
            existing.Decrease(quantity);
            if (existing.Quantity == 0) entries.Remove(instanceId);
            return true;
        }

        public bool TryRemoveDefinition(ContentId definitionId, int quantity)
        {
            if (quantity <= 0) return false;

            var matches = new List<InventoryEntry>();
            var total = 0;
            foreach (var entry in entries.Values)
            {
                if (entry.DefinitionId != definitionId) continue;
                matches.Add(entry);
                total = checked(total + entry.Quantity);
            }

            if (total < quantity) return false;
            matches.Sort((left, right) => StringComparer.Ordinal.Compare(left.InstanceId, right.InstanceId));

            var remaining = quantity;
            for (var i = 0; i < matches.Count && remaining > 0; i++)
            {
                var entry = matches[i];
                var take = Math.Min(entry.Quantity, remaining);
                if (!Remove(entry.InstanceId, take))
                    throw new InvalidOperationException("Inventory changed during deterministic definition removal.");
                remaining -= take;
            }

            return remaining == 0;
        }

        public InventoryEntry GetRequired(string instanceId)
        {
            if (!entries.TryGetValue(instanceId, out var entry)) throw new KeyNotFoundException($"Unknown item instance: {instanceId}");
            return entry;
        }
    }
}
