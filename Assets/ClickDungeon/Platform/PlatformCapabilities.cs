namespace ClickDungeon.Platform
{
    public sealed class PlatformCapabilities
    {
        public bool HasTouch { get; }
        public bool HasMouseKeyboard { get; }
        public bool HasSystemBack { get; }
        public bool RequiresSafeArea { get; }
        public bool SupportsDesktopQuit { get; }

        public PlatformCapabilities(
            bool hasTouch,
            bool hasMouseKeyboard,
            bool hasSystemBack,
            bool requiresSafeArea,
            bool supportsDesktopQuit)
        {
            HasTouch = hasTouch;
            HasMouseKeyboard = hasMouseKeyboard;
            HasSystemBack = hasSystemBack;
            RequiresSafeArea = requiresSafeArea;
            SupportsDesktopQuit = supportsDesktopQuit;
        }
    }
}
