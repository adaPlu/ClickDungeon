from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
LAYOUT = ROOT / "Assets/ClickDungeon/UI/Layout"
MENU = ROOT / "Assets/ClickDungeon/UI/MainMenu"


class PlatformLayoutContractTests(unittest.TestCase):
    def test_required_files_exist(self):
        names = [
            LAYOUT / "SafeAreaInsets.cs",
            LAYOUT / "ViewportProfile.cs",
            LAYOUT / "ResponsiveLayoutContract.cs",
            LAYOUT / "CanonicalViewportProfiles.cs",
            MENU / "PlatformMenuPolicy.cs",
        ]
        for path in names:
            self.assertTrue(path.is_file(), str(path.relative_to(ROOT)))

    def test_reference_layout_contract(self):
        safe = (LAYOUT / "SafeAreaInsets.cs").read_text()
        for token in ["Top", "Right", "Bottom", "Left"]:
            self.assertIn(token, safe)
        profiles = (LAYOUT / "CanonicalViewportProfiles.cs").read_text()
        for token in ["Desktop16x9", "Desktop16x10", "Tablet4x3", "Phone19_5x9", "Phone20x9"]:
            self.assertIn(token, profiles)
        layout = (LAYOUT / "ResponsiveLayoutContract.cs").read_text()
        for token in ["BoardColumns", "BoardRows", "PreserveHud", "PreserveActionRow", "CollapseSecondaryPanels"]:
            self.assertIn(token, layout)
        self.assertIn("BoardColumns = 5", layout)
        self.assertIn("BoardRows = 5", layout)

    def test_menu_contract_preserves_navigation_and_desktop_quit_policy(self):
        text = (MENU / "PlatformMenuPolicy.cs").read_text()
        for token in ["Play", "Continue", "HeroSelect", "Inventory", "Talents", "Shop", "Settings", "SupportsDesktopQuit", "ShowQuit"]:
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
