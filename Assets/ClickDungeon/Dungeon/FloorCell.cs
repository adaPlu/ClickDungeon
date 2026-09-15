using ClickDungeon.Core.Content;

namespace ClickDungeon.Dungeon
{
    public sealed class FloorCell
    {
        public ContentId BaseTerrain { get; set; }
        public ContentId? Structure { get; set; }
        public ContentId? Content { get; set; }
        public ContentId? Actor { get; set; }
        public ContentId? StateOverlay { get; set; }

        public FloorCell(ContentId baseTerrain)
        {
            BaseTerrain = baseTerrain;
        }
    }
}
