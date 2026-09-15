using System;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Combat
{
    public sealed class AbilityDefinition
    {
        public ContentId Id { get; }
        public int FlatDamage { get; }
        public int AttackScalingPermille { get; }
        public int CriticalChancePercent { get; }
        public StatusEffectKind? AppliedStatus { get; }
        public int StatusTurns { get; }

        public AbilityDefinition(
            ContentId id,
            int flatDamage,
            int attackScalingPermille,
            int criticalChancePercent,
            StatusEffectKind? appliedStatus = null,
            int statusTurns = 0)
        {
            if (flatDamage < 0) throw new ArgumentOutOfRangeException(nameof(flatDamage));
            if (attackScalingPermille < 0) throw new ArgumentOutOfRangeException(nameof(attackScalingPermille));
            if (criticalChancePercent < 0 || criticalChancePercent > 100) throw new ArgumentOutOfRangeException(nameof(criticalChancePercent));
            if (appliedStatus.HasValue && statusTurns <= 0) throw new ArgumentOutOfRangeException(nameof(statusTurns));
            Id = id;
            FlatDamage = flatDamage;
            AttackScalingPermille = attackScalingPermille;
            CriticalChancePercent = criticalChancePercent;
            AppliedStatus = appliedStatus;
            StatusTurns = statusTurns;
        }
    }
}
