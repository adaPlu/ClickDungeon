from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]

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


class TileRegistryContractsTests(unittest.TestCase):
    def test_tile_definition_and_dungeon_assembly_exist(self):
        self.assertTrue((ROOT / "Assets/ClickDungeon/Content/Definitions/TileDefinition.cs").is_file())
        self.assertTrue((ROOT / "Assets/ClickDungeon/Dungeon/ClickDungeon.Dungeon.asmdef").is_file())

    def test_exact_25_canonical_tile_ids_and_layers(self):
        path = ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs"
        self.assertTrue(path.is_file(), "CanonicalTiles.cs is missing")
        text = path.read_text()
        found = dict(re.findall(r'Add\("([a-z0-9_.]+)",\s*"[^"]+",\s*TileLayer\.([A-Za-z]+)', text))
        self.assertEqual(found, EXPECTED)
        self.assertEqual(len(found), 25)
        self.assertIn('Art/Runtime/Tiles/tile_door_closed.png', text)

    def test_floor_cell_has_independent_render_state_layers(self):
        path = ROOT / "Assets/ClickDungeon/Dungeon/FloorCell.cs"
        self.assertTrue(path.is_file(), "FloorCell.cs is missing")
        text = path.read_text()
        for token in ("BaseTerrain", "Structure", "Content", "Actor", "StateOverlay"):
            self.assertRegex(text, rf"\b{token}\b")
        self.assertNotIn("Sprite", text, "runtime floor state must not use presentation sprites as truth")

    def test_canonical_tile_sprite_contracts_are_unique(self):
        path = ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs"
        self.assertTrue(path.is_file())
        text = path.read_text()
        sprite_paths = re.findall(r'"(Art/Runtime/Tiles/[^"]+\.png)"', text)
        self.assertEqual(len(sprite_paths), 25)
        self.assertEqual(len(sprite_paths), len(set(sprite_paths)))


if __name__ == "__main__":
    unittest.main()
