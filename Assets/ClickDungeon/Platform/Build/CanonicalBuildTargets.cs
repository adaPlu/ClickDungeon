namespace ClickDungeon.Platform.Build
{
    public static class CanonicalBuildTargets
    {
        private const bool RequiresExternalSigning = true;

        public static readonly BuildTargetContract Windows = new BuildTargetContract(
            RuntimePlatformId.Windows,
            "x64",
            "Mono",
            new[] { "WindowsPlayer" },
            RequiresExternalSigning);

        public static readonly BuildTargetContract Android = new BuildTargetContract(
            RuntimePlatformId.Android,
            "ARM64",
            "IL2CPP",
            new[] { "APK", "AAB" },
            RequiresExternalSigning);

        public static readonly BuildTargetContract IOS = new BuildTargetContract(
            RuntimePlatformId.IOS,
            "ARM64",
            "IL2CPP",
            new[] { "XcodeExport" },
            RequiresExternalSigning);
    }
}
