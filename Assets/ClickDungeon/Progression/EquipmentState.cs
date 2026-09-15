using System.Collections.Generic;
using ClickDungeon.Content.Definitions;

namespace ClickDungeon.Progression
{
    public sealed class EquipmentState
    {
        private readonly Dictionary<EquipmentSlot, string> equipped = new Dictionary<EquipmentSlot, string>();

        public void Equip(EquipmentSlot slot, string itemInstanceId)
        {
            if (slot == EquipmentSlot.None) throw new System.ArgumentException("None is not an equipment slot.", nameof(slot));
            if (string.IsNullOrWhiteSpace(itemInstanceId)) throw new System.ArgumentException("Item instance ID is required.", nameof(itemInstanceId));
            equipped[slot] = itemInstanceId;
        }

        public string Unequip(EquipmentSlot slot)
        {
            if (!equipped.TryGetValue(slot, out var instanceId)) return null;
            equipped.Remove(slot);
            return instanceId;
        }

        public string GetEquipped(EquipmentSlot slot)
        {
            equipped.TryGetValue(slot, out var instanceId);
            return instanceId;
        }
    }
}
