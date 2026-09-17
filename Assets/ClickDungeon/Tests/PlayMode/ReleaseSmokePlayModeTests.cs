using System.Collections;
using ClickDungeon.Runtime;
using NUnit.Framework;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.TestTools;

namespace ClickDungeon.Tests.PlayMode
{
    public sealed class ReleaseSmokePlayModeTests
    {
        [UnityTest]
        public IEnumerator BootstrapTransitionsToDungeonReady()
        {
            SceneManager.LoadScene("Bootstrap");
            yield return null;

            var bootstrap = Object.FindFirstObjectByType<ClickDungeonBootstrap>();
            Assert.IsNotNull(bootstrap);

            var deadline = Time.realtimeSinceStartup + 10f;
            while (bootstrap.State != ReleaseRuntimeState.MainMenu)
            {
                Assert.Less(Time.realtimeSinceStartup, deadline, "MainMenu timeout");
                yield return null;
            }

            bootstrap.StartGame();
            while (bootstrap.State != ReleaseRuntimeState.DungeonReady)
            {
                Assert.Less(Time.realtimeSinceStartup, deadline, "DungeonReady timeout");
                yield return null;
            }

            Assert.AreEqual(25, bootstrap.BoardCellCount);
        }
    }
}
