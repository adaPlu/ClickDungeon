using System;
using System.Collections.Generic;

namespace ClickDungeon.Combat
{
    public interface ICombatRandom
    {
        int NextPercent();
    }

    public sealed class CombatResolver
    {
        public IReadOnlyList<CombatEvent> ResolveAbility(
            CombatantState attacker,
            CombatantState defender,
            AbilityDefinition ability,
            ICombatRandom random)
        {
            if (attacker == null) throw new ArgumentNullException(nameof(attacker));
            if (defender == null) throw new ArgumentNullException(nameof(defender));
            if (ability == null) throw new ArgumentNullException(nameof(ability));
            if (random == null) throw new ArgumentNullException(nameof(random));
            if (attacker.IsDefeated || defender.IsDefeated) throw new InvalidOperationException("Defeated combatants cannot resolve abilities.");

            var events = new List<CombatEvent>
            {
                new CombatEvent(CombatEventKind.AbilityUsed, attacker.EntityId, defender.EntityId)
            };

            var scaledAttack = (attacker.Stats.Attack * ability.AttackScalingPermille) / 1000;
            var raw = checked(ability.FlatDamage + scaledAttack);
            var damage = Math.Max(1, raw - defender.Stats.Defense);
            if (ability.CriticalChancePercent > 0 && random.NextPercent() < ability.CriticalChancePercent)
            {
                long critical = ((long)damage * 150L) / 100L;
                damage = (int)Math.Min(int.MaxValue, Math.Max(1L, critical));
            }

            var wasDefeated = defender.IsDefeated;
            var applied = defender.ApplyDamage(damage);
            events.Add(new CombatEvent(CombatEventKind.DamageApplied, attacker.EntityId, defender.EntityId, applied));

            if (ability.AppliedStatus.HasValue && !defender.IsDefeated)
            {
                defender.ApplyStatus(ability.AppliedStatus.Value, ability.StatusTurns);
                events.Add(new CombatEvent(CombatEventKind.StatusApplied, attacker.EntityId, defender.EntityId, status: ability.AppliedStatus));
            }

            if (!wasDefeated && defender.IsDefeated)
                events.Add(new CombatEvent(CombatEventKind.Defeated, attacker.EntityId, defender.EntityId));

            return events;
        }
    }
}
