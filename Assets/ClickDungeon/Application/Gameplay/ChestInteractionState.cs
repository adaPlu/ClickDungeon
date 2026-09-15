using System;

namespace ClickDungeon.Application.Gameplay
{
    public sealed class ChestInteractionState
    {
        public const int DefaultRequiredInteractions = 3;

        public string ChestDefinitionId { get; }
        public string RequiredKeyTag { get; }
        public int RequiredInteractions { get; }
        public int InteractionsCompleted { get; private set; }
        public bool RewardCommitted { get; private set; }

        public bool IsReadyToCommit => !RewardCommitted && InteractionsCompleted >= RequiredInteractions;

        public ChestInteractionState(
            string chestDefinitionId,
            int requiredInteractions = DefaultRequiredInteractions,
            string requiredKeyTag = null)
        {
            if (string.IsNullOrWhiteSpace(chestDefinitionId)) throw new ArgumentException("Chest definition ID is required.", nameof(chestDefinitionId));
            if (requiredInteractions <= 0) throw new ArgumentOutOfRangeException(nameof(requiredInteractions));
            ChestDefinitionId = chestDefinitionId;
            RequiredInteractions = requiredInteractions;
            RequiredKeyTag = requiredKeyTag;
        }

        public bool RegisterInteraction(string suppliedKeyTag = null)
        {
            if (RewardCommitted) return false;
            if (!string.IsNullOrWhiteSpace(RequiredKeyTag) && !string.Equals(RequiredKeyTag, suppliedKeyTag, StringComparison.Ordinal))
                return false;
            if (InteractionsCompleted < RequiredInteractions) InteractionsCompleted++;
            return true;
        }

        public void MarkRewardCommitted()
        {
            if (!IsReadyToCommit) throw new InvalidOperationException("Chest reward cannot be committed before interaction requirements are satisfied.");
            RewardCommitted = true;
        }
    }
}
