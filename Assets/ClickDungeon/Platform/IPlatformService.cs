namespace ClickDungeon.Platform
{
    public interface IPlatformService
    {
        RuntimePlatformId PlatformId { get; }
        PlatformCapabilities Capabilities { get; }
    }
}
