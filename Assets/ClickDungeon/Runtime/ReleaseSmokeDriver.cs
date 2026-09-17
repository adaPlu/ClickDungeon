using System;
using System.Collections;
using UnityEngine;

namespace ClickDungeon.Runtime
{
    public sealed class ReleaseSmokeDriver : MonoBehaviour
    {
        private const float TimeoutSeconds = 30f;

        private void Start()
        {
            if (!HasSmokeArgument())
                return;
            StartCoroutine(RunSmoke());
        }

        private static bool HasSmokeArgument()
        {
            foreach (var arg in Environment.GetCommandLineArgs())
            {
                if (arg == "-releaseSmoke")
                    return true;
            }
            return false;
        }

        private IEnumerator RunSmoke()
        {
            var bootstrap = GetComponent<ClickDungeonBootstrap>();
            if (bootstrap == null)
            {
                Fail("bootstrap missing");
                yield break;
            }

            var deadline = Time.realtimeSinceStartup + TimeoutSeconds;
            while (bootstrap.State != ReleaseRuntimeState.MainMenu)
            {
                if (Time.realtimeSinceStartup >= deadline)
                {
                    Fail("main menu timeout");
                    yield break;
                }
                yield return null;
            }

            bootstrap.StartGame();
            while (bootstrap.State != ReleaseRuntimeState.DungeonReady)
            {
                if (Time.realtimeSinceStartup >= deadline)
                {
                    Fail("dungeon timeout");
                    yield break;
                }
                yield return null;
            }

            Debug.Log(ReleaseSmokeMarkers.Complete);
            Application.Quit(0);
        }

        private static void Fail(string reason)
        {
            Debug.LogError($"{ReleaseSmokeMarkers.Failure} {reason}");
            Application.Quit(1);
        }
    }
}
