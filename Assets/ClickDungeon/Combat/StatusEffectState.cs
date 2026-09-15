using System;

namespace ClickDungeon.Combat
{
    public enum StatusEffectKind
    {
        Poison,
        Burn,
        Bleed,
        Stun,
        Slow,
        Vulnerable,
        Shielded,
        Regeneration,
        Marked
    }

    public sealed class StatusEffectState
    {
        public StatusEffectKind Kind { get; }
        public int RemainingTurns { get; private set; }
        public int Stacks { get; private set; }

        public StatusEffectState(StatusEffectKind kind, int remainingTurns, int stacks = 1)
        {
            if (remainingTurns <= 0) throw new ArgumentOutOfRangeException(nameof(remainingTurns));
            if (stacks <= 0) throw new ArgumentOutOfRangeException(nameof(stacks));
            Kind = kind;
            RemainingTurns = remainingTurns;
            Stacks = stacks;
        }

        public void Merge(int turns, int stacks)
        {
            if (turns <= 0 || stacks <= 0) throw new ArgumentOutOfRangeException();
            RemainingTurns = Math.Max(RemainingTurns, turns);
            Stacks = checked(Stacks + stacks);
        }

        public void AdvanceTurn()
        {
            if (RemainingTurns > 0) RemainingTurns--;
        }
    }
}
