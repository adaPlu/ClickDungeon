using System;

namespace ClickDungeon.Presentation
{
    public enum PresentationIntentChannel
    {
        Ui,
        Animation,
        Audio,
        Vfx,
        Navigation
    }

    public readonly struct PresentationIntent
    {
        public PresentationIntentChannel Channel { get; }
        public string IntentId { get; }
        public string TargetId { get; }
        public int Amount { get; }

        public PresentationIntent(
            PresentationIntentChannel channel,
            string intentId,
            string targetId = null,
            int amount = 0)
        {
            if (string.IsNullOrWhiteSpace(intentId)) throw new ArgumentException("Intent ID is required.", nameof(intentId));
            Channel = channel;
            IntentId = intentId;
            TargetId = targetId;
            Amount = amount;
        }
    }
}
