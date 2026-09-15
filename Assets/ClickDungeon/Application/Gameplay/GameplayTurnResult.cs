using System.Collections.Generic;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Application.Gameplay
{
    public enum GameplayTurnPhase
    {
        Command,
        Tile,
        PlayerCombat,
        Enemy,
        Reward
    }

    public readonly struct GameplayTurnEvent
    {
        public GameplayTurnPhase Phase { get; }
        public string Kind { get; }
        public string SourceId { get; }
        public string TargetId { get; }
        public int Amount { get; }

        public GameplayTurnEvent(GameplayTurnPhase phase, string kind, string sourceId = null, string targetId = null, int amount = 0)
        {
            Phase = phase;
            Kind = kind ?? string.Empty;
            SourceId = sourceId;
            TargetId = targetId;
            Amount = amount;
        }
    }

    public sealed class GameplayTurnResult
    {
        public FloorCoordinate PlayerPosition { get; }
        public int PlayerHealth { get; }
        public bool PlayerDefeated { get; }
        public IReadOnlyList<GameplayTurnEvent> Events { get; }

        public GameplayTurnResult(
            FloorCoordinate playerPosition,
            int playerHealth,
            bool playerDefeated,
            IReadOnlyList<GameplayTurnEvent> events)
        {
            PlayerPosition = playerPosition;
            PlayerHealth = playerHealth;
            PlayerDefeated = playerDefeated;
            var copy = new GameplayTurnEvent[events.Count];
            for (var i = 0; i < events.Count; i++) copy[i] = events[i];
            Events = copy;
        }
    }
}
