namespace ClickDungeon.Combat
{
    public enum CombatEventKind
    {
        AbilityUsed,
        DamageApplied,
        StatusApplied,
        Defeated
    }

    public readonly struct CombatEvent
    {
        public CombatEventKind Kind { get; }
        public string SourceEntityId { get; }
        public string TargetEntityId { get; }
        public int Amount { get; }
        public StatusEffectKind? Status { get; }

        public CombatEvent(CombatEventKind kind, string sourceEntityId, string targetEntityId, int amount = 0, StatusEffectKind? status = null)
        {
            Kind = kind;
            SourceEntityId = sourceEntityId;
            TargetEntityId = targetEntityId;
            Amount = amount;
            Status = status;
        }
    }
}
