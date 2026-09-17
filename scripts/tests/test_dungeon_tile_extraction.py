from pathlib import Path
import json

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
EXPECTED = {
    "tile_floor_stone.png",
    "tile_trap_pit.png",
    "tile_trap_bomb.png",
    "tile_trap_spike.png",
    "tile_stair_up.png",
    "tile_stair_up_locked.png",
    "tile_stair_down.png",
    "tile_stair_down_locked.png",
    "tile_wall.png",
    "tile_wall_corner.png",
    "tile_door_closed.png",
    "tile_door_locked.png",
    "tile_door_open.png",
    "tile_key.png",
    "tile_chest_closed.png",
    "tile_chest_open.png",
    "tile_torch.png",
    "tile_floor_cracked.png",
    "tile_floor_moss.png",
    "tile_water.png",
    "tile_lava.png",
    "tile_shadow.png",
    "tile_pressure_plate.png",
    "tile_teleport.png",
    "tile_fountain_heal.png",
}


def test_crop_manifest_covers_exact_25_outputs():
    data = json.loads((ROOT / "art/dungeon-tile-crops.json").read_text())
    assert data["schema_version"] == 1
    assert {entry["output"] for entry in data["tiles"]} == EXPECTED
    assert len(data["tiles"]) == 25


def test_runtime_tiles_are_256_square_rgba():
    for name in EXPECTED:
        image = Image.open(ROOT / "Assets/ClickDungeon/Art/Runtime/Tiles" / name)
        assert image.size == (256, 256)
        assert image.mode == "RGBA"


def test_closed_and_open_chest_roles_match_canonical_tile_bytes():
    pairs = (
        ("tile_chest_closed.png", "closed.png"),
        ("tile_chest_open.png", "open.png"),
    )
    for tile_name, chest_name in pairs:
        tile_bytes = (ROOT / "Assets/ClickDungeon/Art/Runtime/Tiles" / tile_name).read_bytes()
        chest_bytes = (ROOT / "Assets/ClickDungeon/Art/Runtime/Chest" / chest_name).read_bytes()
        assert chest_bytes == tile_bytes


def test_extractor_exists_and_supports_check_mode():
    script = (ROOT / "scripts/extract_dungeon_tiles.py").read_text()
    assert "--check" in script
    assert "extract_all" in script

