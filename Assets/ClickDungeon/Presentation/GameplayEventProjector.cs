using System;
using System.Collections.Generic;
using ClickDungeon.Application.Gameplay;

namespace ClickDungeon.Presentation
{
    public sealed class GameplayEventProjector
    {
        public IReadOnlyList<PresentationIntent> Project(GameplayTurnResult result)
        {
            if (result == null) throw new ArgumentNullException(nameof(result));
            var intents = new List<PresentationIntent>();

            for (var i = 0; i < result.Events.Count; i++)
            {
                var item = result.Events[i];
                intents.Add(new PresentationIntent(
                    PresentationIntentChannel.Ui,
                    $"ui.turn.{item.Phase.ToString().ToLowerInvariant()}.{item.Kind.ToLowerInvariant()}",
                    item.TargetId,
                    item.Amount));

                if (item.Phase == GameplayTurnPhase.Tile)
                {
                    intents.Add(new PresentationIntent(
                        PresentationIntentChannel.Vfx,
                        $"vfx.tile.{item.Kind.ToLowerInvariant()}",
                        item.TargetId,
                        item.Amount));
                }
                else if (item.Phase == GameplayTurnPhase.PlayerCombat || item.Phase == GameplayTurnPhase.Enemy)
                {
                    intents.Add(new PresentationIntent(
                        PresentationIntentChannel.Animation,
                        $"animation.combat.{item.Kind.ToLowerInvariant()}",
                        item.SourceId,
                        item.Amount));
                    intents.Add(new PresentationIntent(
                        PresentationIntentChannel.Vfx,
                        $"vfx.combat.{item.Kind.ToLowerInvariant()}",
                        item.TargetId,
                        item.Amount));
                }
                else if (item.Phase == GameplayTurnPhase.Reward)
                {
                    intents.Add(new PresentationIntent(
                        PresentationIntentChannel.Vfx,
                        "vfx.reward.committed",
                        item.SourceId));
                    intents.Add(new PresentationIntent(
                        PresentationIntentChannel.Audio,
                        "audio.reward.committed",
                        item.SourceId));
                }
            }

            return intents;
        }
    }
}
