import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
PLATFORM_VALIDATOR = ROOT / "scripts/validate_platform_contracts.py"
BUILD_VALIDATOR = ROOT / "scripts/validate_platform_build_contracts.py"


class PlatformAdversarialContractTests(unittest.TestCase):
    def _validation_env(self, temp_root: Path):
        env = os.environ.copy()
        env["CLICKDUNGEON_VALIDATION_ROOT"] = str(temp_root)
        return env

    def _copy_platform_surface(self, temp_root: Path):
        clickdungeon = temp_root / "Assets/ClickDungeon"
        clickdungeon.mkdir(parents=True)
        shutil.copytree(ROOT / "Assets/ClickDungeon/Platform", clickdungeon / "Platform")
        shutil.copytree(ROOT / "Assets/ClickDungeon/UI", clickdungeon / "UI")

    def test_platform_validator_rejects_gameplay_authority_mutation(self):
        with tempfile.TemporaryDirectory() as directory:
            temp_root = Path(directory)
            self._copy_platform_surface(temp_root)
            target = temp_root / "Assets/ClickDungeon/Platform/Input/WindowsInputAdapter.cs"
            target.write_text(target.read_text() + "\n// ApplyDamage adversarial mutation\n")

            result = subprocess.run(
                [sys.executable, str(PLATFORM_VALIDATOR)],
                env=self._validation_env(temp_root),
                capture_output=True,
                text=True,
            )

            self.assertNotEqual(0, result.returncode)
            self.assertIn("damage resolution", result.stderr)

    def test_platform_validator_rejects_nondeterministic_entropy_mutation(self):
        with tempfile.TemporaryDirectory() as directory:
            temp_root = Path(directory)
            self._copy_platform_surface(temp_root)
            target = temp_root / "Assets/ClickDungeon/Platform/Lifecycle/MobileLifecycleAdapter.cs"
            target.write_text(target.read_text() + "\n// UnityEngine.Random adversarial mutation\n")

            result = subprocess.run(
                [sys.executable, str(PLATFORM_VALIDATOR)],
                env=self._validation_env(temp_root),
                capture_output=True,
                text=True,
            )

            self.assertNotEqual(0, result.returncode)
            self.assertIn("Unity RNG entropy", result.stderr)

    def test_build_validator_rejects_embedded_signing_secret_mutation(self):
        with tempfile.TemporaryDirectory() as directory:
            temp_root = Path(directory)
            build_root = temp_root / "Assets/ClickDungeon/Platform"
            build_root.mkdir(parents=True)
            shutil.copytree(ROOT / "Assets/ClickDungeon/Platform/Build", build_root / "Build")
            target = build_root / "Build/CanonicalBuildTargets.cs"
            fake_literal = "// " + "password" + " = \"phase15-fake-test-only\"\n"
            target.write_text(target.read_text() + "\n" + fake_literal)

            result = subprocess.run(
                [sys.executable, str(BUILD_VALIDATOR)],
                env=self._validation_env(temp_root),
                capture_output=True,
                text=True,
            )

            self.assertNotEqual(0, result.returncode)
            self.assertIn("embedded signing/auth secret literal detected", result.stderr)


if __name__ == "__main__":
    unittest.main()
