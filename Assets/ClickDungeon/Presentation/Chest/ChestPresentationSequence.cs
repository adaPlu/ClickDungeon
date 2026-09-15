using System;
using System.Collections.Generic;
using ClickDungeon.Application.Gameplay;

namespace ClickDungeon.Presentation.Chest
{
    public enum ChestVisualBeat
    {
        None,
        Anticipation,
        BurstReveal,
        TooMuchToHandle,
        Triumph
    }

    public readonly struct ChestPresentationStep
    {
        public ChestPresentationPhase Phase { get; }
        public ChestVisualBeat VisualBeat { get; }
        public string ReferenceBeatName { get; }
        public string CueId { get; }
        public bool InputLocked { get; }

        public ChestPresentationStep(
            ChestPresentationPhase phase,
            ChestVisualBeat visualBeat,
            string referenceBeatName,
            string cueId,
            bool inputLocked)
        {
            Phase = phase;
            VisualBeat = visualBeat;
            ReferenceBeatName = referenceBeatName;
            CueId = cueId;
            InputLocked = inputLocked;
        }
    }

    public sealed class ChestPresentationSequence
    {
        private readonly ChestRewardEvent committedReward;
        private readonly ChestPresentationStep[] steps;

        public ChestRewardEvent CommittedReward => committedReward;
        public IReadOnlyList<ChestPresentationStep> Steps => steps;

        public ChestPresentationSequence(ChestRewardEvent committedReward)
        {
            if (string.IsNullOrWhiteSpace(committedReward.TransactionId)) throw new ArgumentException("A committed reward event is required.", nameof(committedReward));
            this.committedReward = committedReward;
            steps = new[]
            {
                new ChestPresentationStep(ChestPresentationPhase.Closed, ChestVisualBeat.None, string.Empty, string.Empty, false),
                new ChestPresentationStep(ChestPresentationPhase.InteractionBegins, ChestVisualBeat.Anticipation, "Anticipation", CanonicalPresentationCues.ChestInteraction, true),
                new ChestPresentationStep(ChestPresentationPhase.Opening, ChestVisualBeat.Anticipation, "Anticipation", CanonicalPresentationCues.ChestOpening, true),
                new ChestPresentationStep(ChestPresentationPhase.LightRewardEffect, ChestVisualBeat.BurstReveal, "Burst/Reveal", CanonicalPresentationCues.RewardBurst, true),
                new ChestPresentationStep(ChestPresentationPhase.ItemReveal, ChestVisualBeat.BurstReveal, "Burst/Reveal", CanonicalPresentationCues.ItemReveal, true),
                new ChestPresentationStep(ChestPresentationPhase.RewardPresentation, ChestVisualBeat.TooMuchToHandle, "Too Much To Handle", CanonicalPresentationCues.RewardPresentation, true),
                new ChestPresentationStep(ChestPresentationPhase.ItemCollection, ChestVisualBeat.Triumph, "Triumph", CanonicalPresentationCues.ItemCollection, true),
                new ChestPresentationStep(ChestPresentationPhase.Complete, ChestVisualBeat.Triumph, "Triumph", CanonicalPresentationCues.ChestComplete, false),
            };
        }
    }
}
