from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
PLATFORM_ROOT = ROOT / "Assets/ClickDungeon/Platform"

REQUIRED_FILES = [
    PLATFORM_ROOT / "ClickDungeon.Platform.asmdef",
    PLATFORM_ROOT / "RuntimePlatformId.cs",
    PLATFORM_ROOT / "PlatformCapabilities.cs",
    PLATFORM_ROOT / "IPlatformService.cs",
    PLATFORM_ROOT / "CanonicalPlatformProfiles.cs",
]

FORBIDDEN_AUTHORITY_PATTERNS = [
    r"\bGrantReward\b",
    r"\bApplyDamage\b",
    r"\bCurrentHp\s*=",
    r"\bUnityEngine\.Random\b",
    r"\bSystem\.Random\b",
]


class PlatformContractTests(unittest.TestCase):
    def test_platform_foundation_files_exist(self):
        for path in REQUIRED_FILES:
            self.assertTrue(path.is_file(), f"missing required platform contract: {path.relative_to(ROOT)}")
        self.assertTrue(
            (ROOT / "scripts/validate_platform_contracts.py").is_file(),
            "missing platform contract validator",
        )

    def test_runtime_platform_enum_is_canonical(self):
        text = (PLATFORM_ROOT / "RuntimePlatformId.cs").read_text()
        for token in ["Unknown", "Windows", "Android", "IOS"]:
            self.assertRegex(text, rf"\b{token}\b")

    def test_platform_capabilities_are_read_only_contract_fields(self):
        text = (PLATFORM_ROOT / "PlatformCapabilities.cs").read_text()
        for field in [
            "HasTouch",
            "HasMouseKeyboard",
            "HasSystemBack",
            "RequiresSafeArea",
            "SupportsDesktopQuit",
        ]:
            self.assertIn(f"public bool {field} {{ get; }}", text)

    def test_platform_service_exposes_identity_and_capabilities_only(self):
        text = (PLATFORM_ROOT / "IPlatformService.cs").read_text()
        self.assertIn("RuntimePlatformId PlatformId { get; }", text)
        self.assertIn("PlatformCapabilities Capabilities { get; }", text)
        self.assertNotRegex(text, r"\b(?:GrantReward|ApplyDamage|Save|Combat|Random)\b")

    def test_canonical_profiles_encode_expected_capabilities(self):
        text = (PLATFORM_ROOT / "CanonicalPlatformProfiles.cs").read_text()
        for platform in ["Windows", "Android", "IOS"]:
            self.assertRegex(text, rf"\b{platform}\b")

        normalized = re.sub(r"\s+", "", text)
        self.assertIn(
            "Windows=newPlatformCapabilities(false,true,false,false,true)",
            normalized,
        )
        self.assertIn(
            "Android=newPlatformCapabilities(true,false,true,true,false)",
            normalized,
        )
        self.assertIn(
            "IOS=newPlatformCapabilities(true,false,false,true,false)",
            normalized,
        )

    def test_platform_layer_has_no_gameplay_authority_or_entropy(self):
        if not PLATFORM_ROOT.exists():
            self.fail("platform layer does not exist")

        violations = []
        for path in PLATFORM_ROOT.rglob("*.cs"):
            text = path.read_text()
            for pattern in FORBIDDEN_AUTHORITY_PATTERNS:
                if re.search(pattern, text):
                    violations.append(f"{path.relative_to(ROOT)}: {pattern}")
        self.assertEqual([], violations, "forbidden platform authority detected: " + "; ".join(violations))


if __name__ == "__main__":
    unittest.main()
