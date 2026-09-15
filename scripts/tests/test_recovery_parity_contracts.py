from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class RecoveryParityContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_parity_validator_guards_required_failure_classes(self):
        text = self.read("scripts/validate_recovery_parity.py")
        for token in (
            "ClickDungeon2",
            "presentation gameplay authority",
            "mutable definition state",
            "unknown-ID fallback",
            "nondeterministic entropy",
        ):
            self.assertIn(token, text)

    def test_source_validation_workflow_runs_tests_and_all_validators(self):
        text = self.read(".github/workflows/source-validation.yml")
        for token in (
            "python3 -m pytest -q scripts/tests",
            "validate_recovery_parity.py",
            "validate_animation_reward_contracts.py",
            "validate_production_art.py",
            "git diff --check",
        ):
            self.assertIn(token, text)

    def test_parity_report_records_unity_as_blocked_not_green(self):
        text = self.read("docs/validation/recovery-parity-report.md")
        for token in ("Unity", "BLOCKED", "not claimed", "source-contract"):
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
