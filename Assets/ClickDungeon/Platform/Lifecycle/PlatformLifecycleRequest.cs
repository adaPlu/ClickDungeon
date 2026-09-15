namespace ClickDungeon.Platform.Lifecycle
{
    public enum PlatformLifecycleRequestKind
    {
        None = 0,
        AutosaveCheckpoint = 1,
        PauseOrCancel = 2
    }

    public sealed class PlatformLifecycleRequest
    {
        public AppLifecycleEvent SourceEvent { get; }
        public PlatformLifecycleRequestKind Kind { get; }

        public PlatformLifecycleRequest(AppLifecycleEvent sourceEvent, PlatformLifecycleRequestKind kind)
        {
            SourceEvent = sourceEvent;
            Kind = kind;
        }
    }
}
