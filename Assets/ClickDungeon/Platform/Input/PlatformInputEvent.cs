namespace ClickDungeon.Platform.Input
{
    public sealed class PlatformInputEvent
    {
        public PlatformInputAction Action { get; }
        public int X { get; }
        public int Y { get; }
        public bool HasCoordinate { get; }
        public string ContentIdValue { get; }

        public PlatformInputEvent(
            PlatformInputAction action,
            int x = 0,
            int y = 0,
            bool hasCoordinate = false,
            string contentIdValue = null)
        {
            Action = action;
            X = x;
            Y = y;
            HasCoordinate = hasCoordinate;
            ContentIdValue = contentIdValue;
        }
    }
}
