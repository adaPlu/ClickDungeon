from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]
LIFE = ROOT / "Assets/ClickDungeon/Platform/Lifecycle"


class PlatformLifecycleContractTests(unittest.TestCase):
    def test_required_files_exist(self):
        for name in [
            "AppLifecycleEvent.cs",
            "PlatformLifecycleRequest.cs",
            "MobileLifecycleAdapter.cs",
            "LifecyclePersistenceCoordinator.cs",
        ]:
            self.assertTrue((LIFE / name).is_file(), name)

    def test_events_and_requests_cover_mobile_lifecycle(self):
        events = (LIFE / "AppLifecycleEvent.cs").read_text()
        for token in ["Paused", "Backgrounded", "Resumed", "SystemBack"]:
            self.assertRegex(events, rf"\b{token}\b")
        requests = (LIFE / "PlatformLifecycleRequest.cs").read_text()
        for token in ["None", "AutosaveCheckpoint", "PauseOrCancel"]:
            self.assertRegex(requests, rf"\b{token}\b")

    def test_adapter_routes_without_process_exit(self):
        text = (LIFE / "MobileLifecycleAdapter.cs").read_text()
        for token in ["Paused", "Backgrounded", "Resumed", "SystemBack", "AutosaveCheckpoint", "PauseOrCancel"]:
            self.assertIn(token, text)
        self.assertNotIn("Application.Quit", text)

    def test_coordinator_uses_existing_autosave_boundary(self):
        text = (LIFE / "LifecyclePersistenceCoordinator.cs").read_text()
        self.assertIn("GameSessionPersistenceOrchestrator", text)
        self.assertIn("OnLifecyclePauseOrBackground", text)
        self.assertIn("AutosaveCheckpoint", text)
        self.assertNotRegex(text, r"\b(?:ProfileSave|RunSave|SaveRepository|ApplyDamage|CurrentHp)\b")


if __name__ == "__main__":
    unittest.main()
