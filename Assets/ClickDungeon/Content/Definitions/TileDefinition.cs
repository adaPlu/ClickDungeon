using System;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public enum TileLayer
    {
        BaseTerrain,
        Structure,
        Content,
        StateOverlay
    }

    public sealed class TileDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public TileLayer Layer { get; }
        public string SpriteContractPath { get; }

        public TileDefinition(ContentId id, string displayName, TileLayer layer, string spriteContractPath)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            if (string.IsNullOrWhiteSpace(spriteContractPath)) throw new ArgumentException("Sprite contract path is required.", nameof(spriteContractPath));
            Id = id;
            DisplayName = displayName;
            Layer = layer;
            SpriteContractPath = spriteContractPath;
        }
    }
}
