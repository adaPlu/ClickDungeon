using System.IO;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;

namespace ClickDungeon.Editor
{
    public static class ReleaseSceneBuilder
    {
        private const string SceneDirectory = "Assets/ClickDungeon/Scenes";
        private const string ScenePath = SceneDirectory + "/Bootstrap.unity";

        public static void Build()
        {
            if (!Directory.Exists(SceneDirectory))
                Directory.CreateDirectory(SceneDirectory);

            var scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);
            var root = new GameObject("ClickDungeonBootstrap");
            root.AddComponent<ClickDungeon.Runtime.ClickDungeonBootstrap>();
            root.AddComponent<ClickDungeon.Runtime.ReleaseSmokeDriver>();

            EditorSceneManager.SaveScene(scene, ScenePath);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(ScenePath, true) };
            AssetDatabase.SaveAssets();
            AssetDatabase.Refresh();
        }
    }
}
