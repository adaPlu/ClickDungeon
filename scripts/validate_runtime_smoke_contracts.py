#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
RUNTIME = ROOT / "Assets/ClickDungeon/Runtime"
SCENE = ROOT / "Assets/ClickDungeon/Scenes/Bootstrap.unity"


def fail(message):
    raise SystemExit(f"runtime-smoke-error: {message}")


def read(path):
    if not path.is_file():
        fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    markers = read(RUNTIME / "ReleaseSmokeMarkers.cs")
    bootstrap = read(RUNTIME / "ClickDungeonBootstrap.cs")
    driver = read(RUNTIME / "ReleaseSmokeDriver.cs")
    required_markers = (
        "CD_SMOKE_BOOT",
        "CD_SMOKE_MAIN_MENU",
        "CD_SMOKE_START_GAME",
        "CD_SMOKE_DUNGEON_READY",
    )
    for marker in required_markers:
        if marker not in markers:
            fail(f"missing marker {marker}")

    joined = "\n".join(path.read_text() for path in RUNTIME.rglob("*.cs"))
    for token in (
        "ProductBrand.PlayerFacingName",
        "FloorState.Width",
        "FloorState.Height",
        '"-releaseSmoke"',
    ):
        if token not in joined:
            fail(f"missing runtime contract token {token}")
    if "ClickDungeon2" in joined:
        fail("runtime contains forbidden ClickDungeon2 literal")
    if not SCENE.is_file():
        fail("Bootstrap scene is missing")

    scene_text = SCENE.read_text()
    if "ClickDungeonBootstrap" not in scene_text:
        fail("Bootstrap scene root is missing")
    if "ReleaseSmokeMarkers.Complete" not in driver:
        fail("smoke driver completion marker reference is missing")
    if "ReleaseSmokeMarkers.Failure" not in driver:
        fail("smoke driver failure marker reference is missing")

    print("runtime smoke contracts: PASS")
    return 0


if __name__ == "__main__":
    sys.exit(main())
