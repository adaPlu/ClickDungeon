using System;
using System.IO;
using ClickDungeon.Core.Brand;
using ClickDungeon.Platform.Build;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;

namespace ClickDungeon.Editor
{
    public static class ReleaseBuild
    {
        private const string ScenePath = "Assets/ClickDungeon/Scenes/Bootstrap.unity";

        public static void BuildWindows()
        {
            _ = CanonicalBuildTargets.Windows;
            ConfigureShared();
            Build(BuildTarget.StandaloneWindows64,
                "build/phase16/windows/ClickDungeon.exe");
        }

        public static void BuildAndroidApk()
        {
            _ = CanonicalBuildTargets.Android;
            ConfigureAndroid();
            EditorUserBuildSettings.buildAppBundle = false;
            Build(BuildTarget.Android,
                "build/phase16/android-apk/ClickDungeon.apk");
        }

        public static void BuildAndroidAab()
        {
            _ = CanonicalBuildTargets.Android;
            ConfigureAndroid();
            EditorUserBuildSettings.buildAppBundle = true;
            Build(BuildTarget.Android,
                "build/phase16/android-aab/ClickDungeon.aab");
        }

        public static void ExportIos()
        {
            _ = CanonicalBuildTargets.IOS;
            ConfigureShared();
            PlayerSettings.SetScriptingBackend(
                NamedBuildTarget.iOS,
                ScriptingImplementation.IL2CPP);
            Build(BuildTarget.iOS, "build/phase16/ios");
        }

        private static void ConfigureAndroid()
        {
            ConfigureShared();
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
            PlayerSettings.SetScriptingBackend(
                NamedBuildTarget.Android,
                ScriptingImplementation.IL2CPP);
        }

        private static void ConfigureShared()
        {
            PlayerSettings.productName = ProductBrand.PlayerFacingName;
        }

        private static void Build(BuildTarget target, string outputPath)
        {
            var fullPath = Path.GetFullPath(outputPath);
            var directory = target == BuildTarget.iOS
                ? fullPath
                : Path.GetDirectoryName(fullPath);
            if (!string.IsNullOrEmpty(directory))
                Directory.CreateDirectory(directory);

            var options = new BuildPlayerOptions
            {
                scenes = new[] { ScenePath },
                locationPathName = fullPath,
                target = target,
                options = BuildOptions.None,
            };

            var report = BuildPipeline.BuildPlayer(options);
            if (report.summary.result != BuildResult.Succeeded)
                throw new InvalidOperationException(
                    $"Release build failed for {target}: {report.summary.result}");
        }
    }
}
