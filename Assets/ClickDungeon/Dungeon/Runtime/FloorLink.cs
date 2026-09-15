using ClickDungeon.Core.Content;

namespace ClickDungeon.Dungeon.Runtime
{
    public enum FloorLinkKind
    {
        Teleport,
        PressurePlate
    }

    public sealed class FloorLink
    {
        public ContentId Id { get; }
        public FloorLinkKind Kind { get; }
        public FloorCoordinate Source { get; }
        public FloorCoordinate Target { get; }
        public bool IsConsumed { get; private set; }

        public FloorLink(ContentId id, FloorLinkKind kind, FloorCoordinate source, FloorCoordinate target)
        {
            Id = id;
            Kind = kind;
            Source = source;
            Target = target;
        }

        public bool ConsumeOnce()
        {
            if (IsConsumed) return false;
            IsConsumed = true;
            return true;
        }
    }
}
