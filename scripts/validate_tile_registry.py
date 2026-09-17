#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = {
    "tile.floor_stone": "BaseTerrain",
    "tile.trap_pit": "Content",
    "tile.trap_bomb": "Content",
    "tile.trap_spike": "Content",
    "tile.stair_up": "Structure",
    "tile.stair_up_locked": "Structure",
    "tile.stair_down": "Structure",
    "tile.stair_down_locked": "Structure",
    "tile.wall": "Structure",
    "tile.wall_corner": "Structure",
    "tile.key": "Content",
    "tile.chest_closed": "Content",
    "tile.chest_open": "Content",
    "tile.door_closed": "Structure",
    "tile.door_locked": "Structure",
    "tile.door_open": "Structure",
    "tile.torch": "Content",
    "tile.floor_cracked": "BaseTerrain",
    "tile.floor_moss": "BaseTerrain",
    "tile.water": "BaseTerrain",
    "tile.lava": "BaseTerrain",
    "tile.shadow": "BaseTerrain",
    "tile.pressure_plate": "Content",
    "tile.teleport": "Content",
    "tile.fountain_heal": "Content",
}


def fail(msg):
    print(f"tile-registry-error: {msg}", file=sys.stderr)
    raise SystemExit(1)


def main():
    text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs").read_text()
    found = dict(re.findall(r'Add\("([a-z0-9_.]+)",\s*"[^"]+",\s*TileLayer\.([A-Za-z]+)', text))
    if found != EXPECTED:
        fail(f"canonical IDs/layers drifted: {found}")
    sprite_paths = re.findall(r'"(Art/Runtime/Tiles/[^"]+\.png)"', text)
    if len(sprite_paths) != 25 or len(set(sprite_paths)) != 25:
        fail("runtime sprite contract paths must be 25 unique values")
    floor = (ROOT / "Assets/ClickDungeon/Dungeon/FloorCell.cs").read_text()
    for token in ("BaseTerrain", "Structure", "Content", "Actor", "StateOverlay"):
        if token not in floor:
            fail(f"FloorCell missing {token} layer")
    if "Sprite" in floor:
        fail("runtime FloorCell must not store sprite presentation state")
    print("tile registry contracts: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
