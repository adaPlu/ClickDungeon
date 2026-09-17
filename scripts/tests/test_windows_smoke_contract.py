from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "scripts/run_windows_smoke.ps1"


def test_windows_smoke_runner_contract():
    assert SCRIPT.is_file(), "missing run_windows_smoke.ps1"
    text = SCRIPT.read_text()
    for token in (
        "build/phase16/windows/ClickDungeon.exe",
        "-releaseSmoke",
        "build/phase16/windows-player.log",
        "build/phase16/windows-smoke.json",
        "CD_SMOKE_BOOT",
        "CD_SMOKE_MAIN_MENU",
        "CD_SMOKE_START_GAME",
        "CD_SMOKE_DUNGEON_READY",
        "CD_SMOKE_COMPLETE",
        "45",
        "Exit 1",
    ):
        assert token in text
    assert "ClickDungeon2" not in text


def test_windows_smoke_runner_checks_marker_order():
    text = SCRIPT.read_text()
    assert "IndexOf" in text
    assert "lastIndex" in text
    assert "markerIndex" in text
    assert "markerIndex -le $lastIndex" in text
