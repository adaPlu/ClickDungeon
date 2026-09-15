from pathlib import Path
import json
import subprocess
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]


class ProductionArtContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_runtime_art_folder_policy_exists(self):
        for name in ("Tiles", "Heroes", "Monsters", "Bosses", "Items", "UI", "VFX", "Chest"):
            path = ROOT / "Assets/ClickDungeon/Art/Runtime" / name / ".gitkeep"
            self.assertTrue(path.is_file(), f"missing runtime folder marker {name}")

    def test_manifest_has_versioned_schema_and_unique_asset_paths(self):
        path = ROOT / "art/production-art-manifest.json"
        self.assertTrue(path.is_file())
        data = json.loads(path.read_text())
        self.assertEqual(data["schema_version"], 1)
        self.assertEqual(data["product"], "ClickDungeon")
        assets = data["assets"]
        self.assertGreaterEqual(len(assets), 150)
        ids = [asset["asset_id"] for asset in assets]
        paths = [asset["relative_path"] for asset in assets]
        self.assertEqual(len(ids), len(set(ids)))
        self.assertEqual(len(paths), len(set(paths)))
        for asset in assets:
            for key in ("asset_id", "category", "relative_path", "source_contract", "required"):
                self.assertIn(key, asset)
            self.assertTrue(asset["relative_path"].startswith("Assets/ClickDungeon/Art/Runtime/"))

    def test_builder_check_is_deterministic(self):
        result = subprocess.run(
            [sys.executable, str(ROOT / "scripts/build_production_art_manifest.py"), "--check"],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_source_mode_validator_passes_without_pretending_reference_sheets_are_runtime_art(self):
        result = subprocess.run(
            [sys.executable, str(ROOT / "scripts/validate_production_art.py")],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        manifest = json.loads((ROOT / "art/production-art-manifest.json").read_text())
        for asset in manifest["assets"]:
            self.assertNotIn("docs/reference", asset["relative_path"])

    def test_strict_mode_fails_until_isolated_runtime_png_and_meta_exist(self):
        result = subprocess.run(
            [sys.executable, str(ROOT / "scripts/validate_production_art.py"), "--strict"],
            cwd=ROOT,
            capture_output=True,
            text=True,
        )
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("missing runtime asset", result.stderr)

    def test_validator_rejects_duplicates_and_requires_meta_in_strict_mode(self):
        text = self.read("scripts/validate_production_art.py")
        for token in ("duplicate asset_id", "duplicate relative_path", ".meta", "--strict"):
            self.assertIn(token, text)

    def test_guid_preservation_policy_is_documented(self):
        text = self.read("docs/art/guid-preservation-policy.md")
        for token in ("GUID", ".meta", "preserve", "replace"):
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
