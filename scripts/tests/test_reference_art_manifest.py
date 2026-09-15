from pathlib import Path
import json
import unittest

ROOT = Path(__file__).resolve().parents[2]


class ReferenceArtManifestTests(unittest.TestCase):
    def test_authoritative_reference_inventory_names_are_semantically_correct(self):
        manifest = json.loads((ROOT / "docs/reference/latest/manifest.json").read_text())
        names = [entry["canonical_file"] for entry in manifest]
        self.assertIn("10-core-gameplay.png", names)
        self.assertIn("21-hero-roster.png", names)
        self.assertNotIn("10-main-hero-roster.png", names)

    def test_reference_inventory_files_exist_and_hashes_are_unique(self):
        manifest = json.loads((ROOT / "docs/reference/latest/manifest.json").read_text())
        self.assertGreaterEqual(len(manifest), 21)
        hashes = []
        for entry in manifest:
            self.assertTrue((ROOT / "docs/reference/latest" / entry["canonical_file"]).is_file())
            hashes.append(entry["sha256"])
        self.assertEqual(len(hashes), len(set(hashes)))


if __name__ == "__main__":
    unittest.main()
