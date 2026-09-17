from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[2]
EDIT = ROOT / "Assets/ClickDungeon/Tests/EditMode"
PLAY = ROOT / "Assets/ClickDungeon/Tests/PlayMode"


def read(path):
    assert path.is_file(), f"missing {path.relative_to(ROOT)}"
    return path.read_text()


def asmdef(path):
    data = json.loads(read(path))
    assert "TestAssemblies" in data.get("optionalUnityReferences", [])
    return data


def test_unity_release_test_assemblies_exist():
    edit = asmdef(EDIT / "ClickDungeon.EditModeTests.asmdef")
    play = asmdef(PLAY / "ClickDungeon.PlayModeTests.asmdef")
    assert "ClickDungeon.Runtime" in edit.get("references", [])
    assert "ClickDungeon.Runtime" in play.get("references", [])


def test_editmode_release_contract_assertions_exist():
    text = read(EDIT / "ReleaseContractTests.cs")
    for token in (
        'Assert.AreEqual("ClickDungeon", ProductBrand.PlayerFacingName)',
        "Assert.AreEqual(5, FloorState.Width)",
        "Assert.AreEqual(5, FloorState.Height)",
        "GameplayHudContract.BoardColumns",
        "GameplayHudContract.BoardRows",
    ):
        assert token in text


def test_playmode_release_smoke_assertions_exist():
    text = read(PLAY / "ReleaseSmokePlayModeTests.cs")
    for token in (
        'LoadScene("Bootstrap")',
        "ReleaseRuntimeState.MainMenu",
        "bootstrap.StartGame()",
        "ReleaseRuntimeState.DungeonReady",
        "Assert.AreEqual(25, bootstrap.BoardCellCount)",
        "10f",
    ):
        assert token in text
