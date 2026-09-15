namespace ClickDungeon.Platform
{
    public static class CanonicalPlatformProfiles
    {
        public static readonly PlatformCapabilities Windows =
            new PlatformCapabilities(false, true, false, false, true);

        public static readonly PlatformCapabilities Android =
            new PlatformCapabilities(true, false, true, true, false);

        public static readonly PlatformCapabilities IOS =
            new PlatformCapabilities(true, false, false, true, false);

        public static PlatformCapabilities For(RuntimePlatformId platformId)
        {
            switch (platformId)
            {
                case RuntimePlatformId.Windows:
                    return Windows;
                case RuntimePlatformId.Android:
                    return Android;
                case RuntimePlatformId.IOS:
                    return IOS;
                default:
                    return new PlatformCapabilities(false, false, false, false, false);
            }
        }
    }
}
