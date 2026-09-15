using System;
using System.Collections.Generic;
using ClickDungeon.Combat;
using ClickDungeon.Dungeon.Interaction;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

namespace ClickDungeon.Application.Gameplay
{
    public interface ITraversalPolicy
    {
        bool CanEnter(FloorState floor, FloorCoordinate coordinate);
    }

    public interface IPlayerCombatPhase
    {
        IReadOnlyList<CombatEvent> Resolve(PlayerCommand command);
    }

    public interface IEnemyTurnPhase
    {
        IReadOnlyList<CombatEvent> Resolve();
    }

    public interface IRewardSource
    {
        IReadOnlyList<RewardGrant> Resolve();
    }

    public sealed class GameplaySession
    {
        private readonly FloorState floor;
        private readonly CombatantState player;
        private readonly ITraversalPolicy traversalPolicy;
        private readonly SpecialTileResolver specialTileResolver;
        private readonly IPlayerCombatPhase playerCombat;
        private readonly IEnemyTurnPhase enemyTurns;
        private readonly IRewardSource rewardSource;
        private readonly RewardGrantService rewardGrantService;

        public FloorCoordinate PlayerPosition { get; private set; }

        public GameplaySession(
            FloorState floor,
            CombatantState player,
            FloorCoordinate initialPlayerPosition,
            ITraversalPolicy traversalPolicy,
            SpecialTileResolver specialTileResolver,
            IPlayerCombatPhase playerCombat,
            IEnemyTurnPhase enemyTurns,
            IRewardSource rewardSource,
            RewardGrantService rewardGrantService)
        {
            this.floor = floor ?? throw new ArgumentNullException(nameof(floor));
            this.player = player ?? throw new ArgumentNullException(nameof(player));
            this.traversalPolicy = traversalPolicy ?? throw new ArgumentNullException(nameof(traversalPolicy));
            this.specialTileResolver = specialTileResolver ?? throw new ArgumentNullException(nameof(specialTileResolver));
            this.playerCombat = playerCombat ?? throw new ArgumentNullException(nameof(playerCombat));
            this.enemyTurns = enemyTurns ?? throw new ArgumentNullException(nameof(enemyTurns));
            this.rewardSource = rewardSource ?? throw new ArgumentNullException(nameof(rewardSource));
            this.rewardGrantService = rewardGrantService ?? throw new ArgumentNullException(nameof(rewardGrantService));
            if (!floor.IsInBounds(initialPlayerPosition)) throw new ArgumentOutOfRangeException(nameof(initialPlayerPosition));
            PlayerPosition = initialPlayerPosition;
        }

        public GameplayTurnResult ResolveTurn(PlayerCommand command)
        {
            if (command == null) throw new ArgumentNullException(nameof(command));
            command.Validate();
            if (player.IsDefeated) throw new InvalidOperationException("A defeated player cannot take a turn.");

            var events = new List<GameplayTurnEvent>();
            ResolveCommand(command, events);
            ResolveSpecialTileEvents(events);
            ResolvePlayerCombat(command, events);
            ResolveEnemyTurns(events);
            ResolveRewards(events);

            return new GameplayTurnResult(PlayerPosition, player.CurrentHealth, player.IsDefeated, events);
        }

        private void ResolveCommand(PlayerCommand command, ICollection<GameplayTurnEvent> events)
        {
            if (command.Kind == PlayerCommandKind.Move)
            {
                var target = command.TargetCoordinate.Value;
                var distance = Math.Abs(target.X - PlayerPosition.X) + Math.Abs(target.Y - PlayerPosition.Y);
                if (distance != 1) throw new InvalidOperationException("Move must be orthogonally adjacent.");
                if (!floor.IsInBounds(target) || !traversalPolicy.CanEnter(floor, target))
                    throw new InvalidOperationException("Move target is not traversable.");
                PlayerPosition = target;
            }

            events.Add(new GameplayTurnEvent(GameplayTurnPhase.Command, command.Kind.ToString()));
        }

        private void ResolveSpecialTileEvents(ICollection<GameplayTurnEvent> events)
        {
            var tileEvents = specialTileResolver.ResolveEntry(floor, PlayerPosition);
            for (var i = 0; i < tileEvents.Count; i++)
            {
                var tileEvent = tileEvents[i];
                switch (tileEvent.Kind)
                {
                    case SpecialTileEventKind.DamageRequested:
                        var damaged = player.ApplyDamage(tileEvent.Amount);
                        events.Add(new GameplayTurnEvent(GameplayTurnPhase.Tile, tileEvent.Kind.ToString(), amount: damaged));
                        break;
                    case SpecialTileEventKind.HealingRequested:
                        var healed = player.ApplyHealing(tileEvent.Amount);
                        events.Add(new GameplayTurnEvent(GameplayTurnPhase.Tile, tileEvent.Kind.ToString(), amount: healed));
                        break;
                    case SpecialTileEventKind.TeleportRequested:
                        if (!floor.IsInBounds(tileEvent.Target)) throw new InvalidOperationException("Teleport target is out of bounds.");
                        PlayerPosition = tileEvent.Target;
                        events.Add(new GameplayTurnEvent(GameplayTurnPhase.Tile, tileEvent.Kind.ToString()));
                        break;
                    case SpecialTileEventKind.PressurePlateActivated:
                        events.Add(new GameplayTurnEvent(GameplayTurnPhase.Tile, tileEvent.Kind.ToString()));
                        break;
                    default:
                        throw new ArgumentOutOfRangeException();
                }
            }
        }

        private void ResolvePlayerCombat(PlayerCommand command, ICollection<GameplayTurnEvent> events)
        {
            var combatEvents = playerCombat.Resolve(command);
            AppendCombatEvents(GameplayTurnPhase.PlayerCombat, combatEvents, events);
        }

        private void ResolveEnemyTurns(ICollection<GameplayTurnEvent> events)
        {
            var combatEvents = enemyTurns.Resolve();
            AppendCombatEvents(GameplayTurnPhase.Enemy, combatEvents, events);
        }

        private void ResolveRewards(ICollection<GameplayTurnEvent> events)
        {
            var rewards = rewardSource.Resolve();
            for (var i = 0; i < rewards.Count; i++)
            {
                if (!rewardGrantService.Grant(rewards[i])) continue;
                events.Add(new GameplayTurnEvent(GameplayTurnPhase.Reward, "RewardGranted", rewards[i].TransactionId));
            }
        }

        private static void AppendCombatEvents(
            GameplayTurnPhase phase,
            IReadOnlyList<CombatEvent> combatEvents,
            ICollection<GameplayTurnEvent> target)
        {
            for (var i = 0; i < combatEvents.Count; i++)
            {
                var item = combatEvents[i];
                target.Add(new GameplayTurnEvent(
                    phase,
                    item.Kind.ToString(),
                    item.SourceEntityId,
                    item.TargetEntityId,
                    item.Amount));
            }
        }
    }
}
