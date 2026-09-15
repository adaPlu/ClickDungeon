namespace ClickDungeon.Platform.Lifecycle
{
    public sealed class MobileLifecycleAdapter
    {
        public PlatformLifecycleRequest Translate(AppLifecycleEvent lifecycleEvent)
        {
            switch (lifecycleEvent)
            {
                case AppLifecycleEvent.Paused:
                case AppLifecycleEvent.Backgrounded:
                    return new PlatformLifecycleRequest(
                        lifecycleEvent,
                        PlatformLifecycleRequestKind.AutosaveCheckpoint);
                case AppLifecycleEvent.SystemBack:
                    return new PlatformLifecycleRequest(
                        lifecycleEvent,
                        PlatformLifecycleRequestKind.PauseOrCancel);
                case AppLifecycleEvent.Resumed:
                default:
                    return new PlatformLifecycleRequest(
                        lifecycleEvent,
                        PlatformLifecycleRequestKind.None);
            }
        }
    }
}
