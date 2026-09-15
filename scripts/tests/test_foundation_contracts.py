from pathlib import Path
import json
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class FoundationContractsTests(unittest.TestCase):
    def test_required_assembly_definitions_exist_with_expected_dependencies(self):
        expected = {
            "Assets/ClickDungeon/Core/ClickDungeon.Core.asmdef": ("ClickDungeon.Core", []),
            "Assets/ClickDungeon/Content/ClickDungeon.Content.asmdef": ("ClickDungeon.Content", ["ClickDungeon.Core"]),
            "Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef": (
                "ClickDungeon.Application",
                ["ClickDungeon.Core", "ClickDungeon.Content"],
            ),
            "Assets/ClickDungeon/Presentation/ClickDungeon.Presentation.asmdef": (
                "ClickDungeon.Presentation",
                ["ClickDungeon.Core", "ClickDungeon.Content", "ClickDungeon.Application"],
            ),
            "Assets/ClickDungeon/Narrative/ClickDungeon.Narrative.asmdef": (
                "ClickDungeon.Narrative",
                ["ClickDungeon.Core", "ClickDungeon.Content"],
            ),
        }
        for rel, (name, refs) in expected.items():
            path = ROOT / rel
            self.assertTrue(path.is_file(), f"missing {rel}")
            data = json.loads(path.read_text())
            self.assertEqual(data["name"], name)
            actual_refs = data.get("references", [])
            self.assertTrue(set(refs).issubset(actual_refs), f"{rel} missing required references")
            if name in {"ClickDungeon.Core", "ClickDungeon.Content"}:
                self.assertEqual(actual_refs, refs, f"{rel} must remain a low-level dependency boundary")
            self.assertNotIn("ClickDungeon.Presentation", actual_refs, f"{rel} must not depend upward on Presentation")

    def test_player_facing_brand_is_clickdungeon_only(self):
        path = ROOT / "Assets/ClickDungeon/Core/Brand/ProductBrand.cs"
        self.assertTrue(path.is_file(), "ProductBrand.cs is missing")
        text = path.read_text()
        self.assertRegex(text, r'PlayerFacingName\s*=\s*"ClickDungeon"')
        self.assertNotIn('"ClickDungeon2"', text)

    def test_content_id_is_dependency_free_value_object_with_validation(self):
        path = ROOT / "Assets/ClickDungeon/Core/Content/ContentId.cs"
        self.assertTrue(path.is_file(), "ContentId.cs is missing")
        text = path.read_text()
        self.assertIn("readonly struct ContentId", text)
        self.assertIn("IEquatable<ContentId>", text)
        self.assertIn("ArgumentException", text)
        self.assertRegex(text, r"[a-z0-9]+(?:\.[a-z0-9_]+)+")
        self.assertNotIn("UnityEngine", text)

    def test_runtime_sources_do_not_contain_legacy_player_brand(self):
        runtime_root = ROOT / "Assets/ClickDungeon"
        if not runtime_root.exists():
            self.fail("runtime source tree is missing")
        offenders = []
        for path in runtime_root.rglob("*"):
            if path.is_file() and path.suffix in {".cs", ".json", ".asset", ".uxml", ".uss"}:
                if "ClickDungeon2" in path.read_text(errors="ignore"):
                    offenders.append(str(path.relative_to(ROOT)))
        self.assertEqual(offenders, [])


if __name__ == "__main__":
    unittest.main()
