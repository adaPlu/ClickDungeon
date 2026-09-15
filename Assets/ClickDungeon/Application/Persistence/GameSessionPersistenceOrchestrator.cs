using System;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Core.Content;
using ClickDungeon.Save;

namespace ClickDungeon.Application.Persistence
{
    public sealed class GameSessionPersistenceOrchestrator
    {
        private readonly GameplaySession session;
        private readonly IAutosaveCheckpointSink autosave;

        public GameSessionPersistenceOrchestrator(GameplaySession session, IAutosaveCheckpointSink autosave)
        {
            this.session = session ?? throw new ArgumentNullException(nameof(session));
            this.autosave = autosave ?? throw new ArgumentNullException(nameof(autosave));
        }

        public GameplayTurnResult ResolveTurn(PlayerCommand command)
        {
            var result = session.ResolveTurn(command);
            autosave.Request(AutosaveReason.ResolvedTurn);
            return result;
        }

        public bool TryCommitChestReward(
            ChestInteractionState chest,
            string itemInstanceId,
            ContentId itemDefinitionId,
            int quantity,
            ContentId? currencyId,
            long currencyAmount,
            out ChestRewardEvent rewardEvent)
        {
            var committed = session.TryCommitChestReward(
                chest,
                itemInstanceId,
                itemDefinitionId,
                quantity,
                currencyId,
                currencyAmount,
                out rewardEvent);
            if (committed) autosave.Request(AutosaveReason.RewardCommitted);
            return committed;
        }

        public void CompleteFloorTransition(Action transition)
        {
            if (transition == null) throw new ArgumentNullException(nameof(transition));
            transition();
            autosave.Request(AutosaveReason.FloorTransition);
        }

        public void OnLifecyclePauseOrBackground()
        {
            autosave.Request(AutosaveReason.LifecyclePauseOrBackground);
        }
    }
}
