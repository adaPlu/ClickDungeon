using ClickDungeon.Core.Brand;
using ClickDungeon.Dungeon.Runtime;
using UnityEngine;

namespace ClickDungeon.Runtime
{
    public sealed class ClickDungeonBootstrap : MonoBehaviour
    {
        public ReleaseRuntimeState State { get; private set; }
        public int BoardCellCount { get; private set; }
        public GameObject MainMenuRoot { get; private set; }
        public GameObject GameplayRoot { get; private set; }

        private void Awake()
        {
            State = ReleaseRuntimeState.Boot;
            Debug.Log($"{ReleaseSmokeMarkers.Boot} product={ProductBrand.PlayerFacingName}");
            BuildMainMenu();
            State = ReleaseRuntimeState.MainMenu;
            Debug.Log(ReleaseSmokeMarkers.MainMenu);
        }

        public void StartGame()
        {
            if (State == ReleaseRuntimeState.DungeonReady)
                return;
            if (State != ReleaseRuntimeState.MainMenu)
                return;

            Debug.Log(ReleaseSmokeMarkers.StartGame);
            if (MainMenuRoot != null)
                MainMenuRoot.SetActive(false);

            BuildGameplay();
            State = ReleaseRuntimeState.DungeonReady;
            Debug.Log(ReleaseSmokeMarkers.DungeonReady);
        }

        private void BuildMainMenu()
        {
            MainMenuRoot = new GameObject("MainMenu");
            MainMenuRoot.transform.SetParent(transform, false);
            var brand = new GameObject(ProductBrand.PlayerFacingName);
            brand.transform.SetParent(MainMenuRoot.transform, false);
        }

        private void BuildGameplay()
        {
            GameplayRoot = new GameObject("Gameplay");
            GameplayRoot.transform.SetParent(transform, false);

            var board = new GameObject("Board5x5");
            board.transform.SetParent(GameplayRoot.transform, false);
            BoardCellCount = 0;
            for (var y = 0; y < FloorState.Height; y++)
            {
                for (var x = 0; x < FloorState.Width; x++)
                {
                    var cell = new GameObject($"Cell_{x}_{y}");
                    cell.transform.SetParent(board.transform, false);
                    BoardCellCount++;
                }
            }

            var hud = new GameObject("HUD");
            hud.transform.SetParent(GameplayRoot.transform, false);
        }
    }
}
