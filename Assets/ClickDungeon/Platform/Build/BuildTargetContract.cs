using System;

namespace ClickDungeon.Platform.Build
{
    public sealed class BuildTargetContract
    {
        public RuntimePlatformId PlatformId { get; }
        public string Architecture { get; }
        public string ScriptingBackend { get; }
        public string[] ArtifactKinds { get; }
        public bool RequiresExternalSigning { get; }

        public BuildTargetContract(
            RuntimePlatformId platformId,
            string architecture,
            string scriptingBackend,
            string[] artifactKinds,
            bool requiresExternalSigning)
        {
            PlatformId = platformId;
            Architecture = architecture ?? throw new ArgumentNullException(nameof(architecture));
            ScriptingBackend = scriptingBackend ?? throw new ArgumentNullException(nameof(scriptingBackend));
            ArtifactKinds = artifactKinds == null
                ? throw new ArgumentNullException(nameof(artifactKinds))
                : (string[])artifactKinds.Clone();
            RequiresExternalSigning = requiresExternalSigning;
        }
    }
}
