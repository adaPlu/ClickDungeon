using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public enum EnemyThreatTag
    {
        Brute,
        Swarm,
        Undead,
        Magic,
        Beast,
        Boss,
        Trickster
    }

    public enum EnemyAnimationState
    {
        Spawn,
        Idle,
        Attack,
        Hit,
        Defeat
    }

    public sealed class EnemyDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public IReadOnlyList<EnemyThreatTag> ThreatTags { get; }
        public int BaseHealth { get; }
        public int Attack { get; }
        public int Defense { get; }
        public int Initiative { get; }
        public int XpReward { get; }
        public int MinDepth { get; }
        public int RarityWeight { get; }
        public ContentId LootTableId { get; }
        public ContentId BehaviorId { get; }
        public string SpriteContractPath { get; }
        public IReadOnlyList<EnemyAnimationState> RequiredAnimationStates { get; }

        public EnemyDefinition(
            ContentId id,
            string displayName,
            IReadOnlyList<EnemyThreatTag> threatTags,
            int baseHealth,
            int attack,
            int defense,
            int initiative,
            int xpReward,
            int minDepth,
            int rarityWeight,
            ContentId lootTableId,
            ContentId behaviorId,
            string spriteContractPath,
            IReadOnlyList<EnemyAnimationState> requiredAnimationStates)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            if (threatTags == null || threatTags.Count == 0) throw new ArgumentException("At least one threat tag is required.", nameof(threatTags));
            if (baseHealth <= 0) throw new ArgumentOutOfRangeException(nameof(baseHealth));
            if (attack < 0) throw new ArgumentOutOfRangeException(nameof(attack));
            if (defense < 0) throw new ArgumentOutOfRangeException(nameof(defense));
            if (initiative < 0) throw new ArgumentOutOfRangeException(nameof(initiative));
            if (xpReward < 0) throw new ArgumentOutOfRangeException(nameof(xpReward));
            if (minDepth < 1) throw new ArgumentOutOfRangeException(nameof(minDepth));
            if (rarityWeight <= 0) throw new ArgumentOutOfRangeException(nameof(rarityWeight));
            if (string.IsNullOrWhiteSpace(spriteContractPath)) throw new ArgumentException("Sprite contract path is required.", nameof(spriteContractPath));
            if (requiredAnimationStates == null || requiredAnimationStates.Count == 0) throw new ArgumentException("Animation states are required.", nameof(requiredAnimationStates));

            Id = id;
            DisplayName = displayName;
            ThreatTags = Copy(threatTags);
            BaseHealth = baseHealth;
            Attack = attack;
            Defense = defense;
            Initiative = initiative;
            XpReward = xpReward;
            MinDepth = minDepth;
            RarityWeight = rarityWeight;
            LootTableId = lootTableId;
            BehaviorId = behaviorId;
            SpriteContractPath = spriteContractPath;
            RequiredAnimationStates = Copy(requiredAnimationStates);
        }

        private static IReadOnlyList<T> Copy<T>(IReadOnlyList<T> source)
        {
            var copy = new T[source.Count];
            for (var i = 0; i < source.Count; i++) copy[i] = source[i];
            return copy;
        }
    }
}
