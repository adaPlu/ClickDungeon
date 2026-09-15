from pathlib import Path
import json
import unittest

ROOT = Path(__file__).resolve().parents[2]
MANIFEST_PATH = ROOT / "docs/reference/latest/manifest.json"
IMPLEMENTATION_CONTRACT_PATH = ROOT / "docs/reference/latest/implementation-contract.json"
POLICY_PATH = ROOT / "docs/art/reference-art-source-of-truth.md"


class ReferenceArtManifestTests(unittest.TestCase):
    def test_authoritative_reference_inventory_names_are_semantically_correct(self):
        manifest = json.loads(MANIFEST_PATH.read_text())
        names = [entry["canonical_file"] for entry in manifest]
        self.assertIn("10-core-gameplay.png", names)
        self.assertIn("21-hero-roster.png", names)
        self.assertNotIn("10-main-hero-roster.png", names)

    def test_reference_inventory_files_exist_and_hashes_are_unique(self):
        manifest = json.loads(MANIFEST_PATH.read_text())
        self.assertGreaterEqual(len(manifest), 21)
        hashes = []
        for entry in manifest:
            self.assertTrue((ROOT / "docs/reference/latest" / entry["canonical_file"]).is_file())
            hashes.append(entry["sha256"])
        self.assertEqual(len(hashes), len(set(hashes)))

    def test_reference_art_is_declared_primary_implementation_source_of_truth(self):
        self.assertTrue(POLICY_PATH.is_file(), "missing reference-art source-of-truth policy")
        self.assertTrue(IMPLEMENTATION_CONTRACT_PATH.is_file(), "missing implementation contract")

        contract = json.loads(IMPLEMENTATION_CONTRACT_PATH.read_text())
        self.assertEqual(contract["authority"], "primary product source of truth")
        self.assertEqual(
            contract["precedence"],
            ["reference_art", "approved_gameplay_contracts", "existing_implementation"],
        )
        self.assertEqual(contract["branding"]["player_facing_title"], "ClickDungeon")
        self.assertTrue(contract["branding"]["runtime_clickdungeon2_forbidden"])

    def test_core_reference_views_drive_look_and_function_contracts(self):
        contract = json.loads(IMPLEMENTATION_CONTRACT_PATH.read_text())
        required_views = {
            "title_main_menu": "09-title-main-menu.png",
            "core_gameplay": "10-core-gameplay.png",
            "gameplay_and_chest_flow": "08-gameplay-example.png",
            "game_rules": "04-game-rules-plan.png",
            "dungeon_tiles_a": "11-dungeon-tiles-a.png",
            "dungeon_tiles_b": "12-dungeon-tiles-b.png",
            "monsters_and_bosses": "02-monsters-bosses.png",
            "items_and_equipment": "01-item-demo.png",
            "hero_roster": "21-hero-roster.png",
            "sir_clickington": "16-sir-clickington.png",
        }
        self.assertEqual(contract["core_views"], required_views)

        manifest_names = {
            entry["canonical_file"] for entry in json.loads(MANIFEST_PATH.read_text())
        }
        for canonical_file in required_views.values():
            self.assertIn(canonical_file, manifest_names)
            self.assertTrue((ROOT / "docs/reference/latest" / canonical_file).is_file())

        functional_requirements = set(contract["functional_requirements"])
        self.assertTrue(
            {
                "title_navigation",
                "continue_and_daily_reward",
                "five_by_five_board",
                "hud_and_action_row",
                "tile_trap_key_door_chest_semantics",
                "hero_identity_and_animation_states",
                "monster_identity_and_readability",
                "inventory_rarity_and_equipment_silhouettes",
                "chest_found_click_key_reward_sequence",
            }.issubset(functional_requirements)
        )


if __name__ == "__main__":
    unittest.main()
