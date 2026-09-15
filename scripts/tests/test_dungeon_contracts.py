from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class DungeonContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_floor_state_is_fixed_5x5_and_tracks_start_exit(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs")
        self.assertRegex(text, r"Width\s*=\s*5")
        self.assertRegex(text, r"Height\s*=\s*5")
        self.assertIn("Start", text)
        self.assertIn("Exit", text)
        self.assertIn("FloorIndex", text)

    def test_deterministic_rng_has_no_external_entropy(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Generation/DeterministicRng.cs")
        for forbidden in ("UnityEngine.Random", "System.Random", "DateTime", "Guid.NewGuid", "Environment.TickCount"):
            self.assertNotIn(forbidden, text)
        self.assertIn("NextUInt", text)
        self.assertIn("NextInt", text)
        self.assertIn("seed", text.lower())

    def test_generator_explicitly_consumes_seed_floor_and_generation_version(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs")
        for token in ("runSeed", "floorIndex", "generationVersion", "DeterministicRng"):
            self.assertIn(token, text)
        self.assertIn("CanonicalTiles", text)

    def test_floor_validator_guards_progression_and_links(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Generation/FloorValidator.cs")
        for token in ("ValidateReachableExit", "ValidateKeyBeforeMandatoryLock", "ValidateLinks", "Teleport", "PressurePlate"):
            self.assertIn(token, text)
        self.assertIn("Queue<FloorCoordinate>", text)

    def test_special_tile_resolver_emits_requests_without_hp_authority(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Interaction/SpecialTileResolver.cs")
        for token in ("DamageRequested", "HealingRequested", "TeleportRequested", "PressurePlateActivated"):
            self.assertIn(token, text)
        for forbidden in ("CurrentHealth", "HitPoints", ".Heal(", ".Damage("):
            self.assertNotIn(forbidden, text)
        self.assertNotIn("ResolveEntry(destination", text, "teleport must not recursively resolve destination in same command")

    def test_floor_links_are_generic_and_one_way_records(self):
        text = self.read("Assets/ClickDungeon/Dungeon/Runtime/FloorLink.cs")
        for token in ("Teleport", "PressurePlate", "Source", "Target"):
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
