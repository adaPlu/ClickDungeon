using System;
using System.Collections.Generic;

namespace ClickDungeon.Combat
{
    public readonly struct CombatStats
    {
        public int Attack { get; }
        public int Defense { get; }
        public int Speed { get; }

        public CombatStats(int attack, int defense, int speed)
        {
            Attack = Math.Max(0, attack);
            Defense = Math.Max(0, defense);
            Speed = Math.Max(0, speed);
        }
    }

    public sealed class CombatantState
    {
        private readonly List<StatusEffectState> statuses = new List<StatusEffectState>();

        public string EntityId { get; }
        public CombatStats Stats { get; }
        public int MaxHealth { get; }
        public int CurrentHealth { get; private set; }
        public bool IsDefeated => CurrentHealth <= 0;
        public IReadOnlyList<StatusEffectState> Statuses => statuses;

        public CombatantState(string entityId, int maxHealth, CombatStats stats)
        {
            if (string.IsNullOrWhiteSpace(entityId)) throw new ArgumentException("Entity ID is required.", nameof(entityId));
            if (maxHealth <= 0) throw new ArgumentOutOfRangeException(nameof(maxHealth));
            EntityId = entityId;
            MaxHealth = maxHealth;
            CurrentHealth = maxHealth;
            Stats = stats;
        }

        public int ApplyDamage(int amount)
        {
            if (amount < 0) throw new ArgumentOutOfRangeException(nameof(amount));
            var before = CurrentHealth;
            long next = (long)CurrentHealth - amount;
            CurrentHealth = (int)Math.Max(0L, next);
            return before - CurrentHealth;
        }

        public int ApplyHealing(int amount)
        {
            if (amount < 0) throw new ArgumentOutOfRangeException(nameof(amount));
            var before = CurrentHealth;
            long next = (long)CurrentHealth + amount;
            CurrentHealth = (int)Math.Min((long)MaxHealth, next);
            return CurrentHealth - before;
        }

        public void ApplyStatus(StatusEffectKind kind, int turns, int stacks = 1)
        {
            foreach (var status in statuses)
            {
                if (status.Kind != kind) continue;
                status.Merge(turns, stacks);
                return;
            }
            statuses.Add(new StatusEffectState(kind, turns, stacks));
        }
    }
}
