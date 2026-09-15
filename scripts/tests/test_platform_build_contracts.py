from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
BUILD = ROOT / "Assets/ClickDungeon/Platform/Build"


class PlatformBuildContractTests(unittest.TestCase):
    def test_build_contract_files_exist(self):
        self.assertTrue((BUILD / "BuildTargetContract.cs").is_file())
        self.assertTrue((BUILD / "CanonicalBuildTargets.cs").is_file())
        self.assertTrue((ROOT / "scripts/validate_platform_build_contracts.py").is_file())

    def test_canonical_targets_require_release_outputs(self):
        text = (BUILD / "CanonicalBuildTargets.cs").read_text()
        for token in [
            "Windows", "x64",
            "Android", "ARM64", "IL2CPP", "APK", "AAB",
            "IOS", "XcodeExport",
            "RequiresExternalSigning",
        ]:
            self.assertIn(token, text)

    def test_build_contract_has_no_embedded_signing_secret_literals(self):
        if not BUILD.exists():
            return
        pattern = re.compile(r"(?i)(?:password|secret|token)\s*=\s*\"[^\"]+\"")
        violations = []
        for path in BUILD.rglob("*.cs"):
            if pattern.search(path.read_text()):
                violations.append(str(path.relative_to(ROOT)))
        self.assertEqual([], violations)


if __name__ == "__main__":
    unittest.main()
