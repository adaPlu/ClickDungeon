from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RUNTIME = ROOT / "Assets/ClickDungeon/Runtime"
EDITOR = ROOT / "Assets/ClickDungeon/Editor"
SCENE = ROOT / "Assets/ClickDungeon/Scenes/Bootstrap.unity"


def read(path):
    assert path.is_file(), f"missing {path.relative_to(ROOT)}"
    return path.read_text()


def runtime_text():
    assert RUNTIME.is_dir(), "missing Assets/ClickDungeon/Runtime"
    return "\n".join(path.read_text() for path in RUNTIME.rglob("*.cs"))


def test_release_smoke_markers_and_state_contract_exist():
    markers = read(RUNTIME / "ReleaseSmokeMarkers.cs")
    state = read(RUNTIME / "ReleaseRuntimeState.cs")
    for token in (
        'Boot = "CD_SMOKE_BOOT"',
        'MainMenu = "CD_SMOKE_MAIN_MENU"',
        'StartGame = "CD_SMOKE_START_GAME"',
        'DungeonReady = "CD_SMOKE_DUNGEON_READY"',
    ):
        assert token in markers
    for token in ("Boot", "MainMenu", "DungeonReady"):
        assert token in state


def test_runtime_bootstrap_consumes_release_contracts():
    text = runtime_text()
    for token in (
        "ProductBrand.PlayerFacingName",
        "FloorState.Width",
        "FloorState.Height",
        '"-releaseSmoke"',
    ):
        assert token in text
    assert "ClickDungeon2" not in text


def test_editor_builder_and_unity_generated_scene_exist():
    builder = read(EDITOR / "ReleaseSceneBuilder.cs")
    assert "EditorSceneManager.NewScene" in builder
    assert "EditorSceneManager.SaveScene" in builder
    assert "EditorBuildSettings.scenes" in builder
    assert "ClickDungeonBootstrap" in builder
    assert SCENE.is_file(), "Bootstrap scene must be created by Unity editor script"
