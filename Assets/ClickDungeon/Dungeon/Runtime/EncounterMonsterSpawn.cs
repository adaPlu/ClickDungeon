using System;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Dungeon.Runtime
{
    public sealed class EncounterMonsterSpawn
    {
        public string EntityId { get; }
        public ContentId DefinitionId { get; }
        public FloorCoordinate Position { get; }

        public EncounterMonsterSpawn(string entityId, ContentId definitionId, FloorCoordinate position)
        {
            if (string.IsNullOrWhiteSpace(entityId)) throw new ArgumentException("Entity ID is required.", nameof(entityId));
            EntityId = entityId;
            DefinitionId = definitionId;
            Position = position;
        }
    }
}
