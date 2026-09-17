from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class PresentationUiContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_main_menu_contains_every_reference_interaction_route(self):
        text = self.read("Assets/ClickDungeon/UI/MainMenu/MainMenuLayoutContract.cs")
        for token in (
            "HeroProfile", "GoldStore", "GemStore", "ContinueRun", "Play",
            "HeroSelect", "Inventory", "Talents", "Shop", "Settings", "Quit",
            "Challenges", "Inbox", "Menu", "DailyRewardClaim",
        ):
            self.assertIn(token, text)
        self.assertIn('ProductBrand.PlayerFacingName', text)
        self.assertNotIn('ClickDungeon2', text)

    def test_gameplay_hud_contract_matches_five_by_five_reference_hierarchy(self):
        text = self.read("Assets/ClickDungeon/UI/Gameplay/GameplayHudContract.cs")
        self.assertIn("BoardColumns = 5", text)
        self.assertIn("BoardRows = 5", text)
        for token in (
            "HeroPortrait", "HeroLevel", "Health", "ClassResource", "Gold", "Gems",
            "FloorIdentity", "EnemyHealthBar", "SelectedCellHighlight",
            "Move", "Slash", "Shield", "Dash", "Potion",
            "Inventory", "Talents", "Shop",
        ):
            self.assertIn(token, text)

    def test_responsive_contract_includes_safe_areas_and_touch_targets(self):
        menu = self.read("Assets/ClickDungeon/UI/MainMenu/MainMenuLayoutContract.cs")
        gameplay = self.read("Assets/ClickDungeon/UI/Gameplay/GameplayHudContract.cs")
        joined = menu + gameplay
        for token in ("SafeAreaInsets", "ResponsiveLayoutMode", "MinTouchTargetPixels", "DesktopWide", "MobileCompact"):
            self.assertIn(token, joined)

    def test_hero_selection_contract_exposes_identity_not_mechanics_copy(self):
        text = self.read("Assets/ClickDungeon/UI/HeroSelect/HeroSelectionContract.cs")
        for token in ("HeroIdentityDefinition", "MechanicsClassId", "PresentationClassLabel", "ArtSetId", "IsUnlocked"):
            self.assertIn(token, text)
        self.assertIn("CanonicalHeroes.All", text)

    def test_presentation_intents_cover_ui_animation_audio_and_vfx(self):
        text = self.read("Assets/ClickDungeon/Presentation/PresentationIntent.cs")
        for token in ("Ui", "Animation", "Audio", "Vfx", "Navigation", "IntentId", "TargetId"):
            self.assertIn(token, text)

    def test_gameplay_event_projector_consumes_turn_results(self):
        text = self.read("Assets/ClickDungeon/Presentation/GameplayEventProjector.cs")
        self.assertIn("GameplayTurnResult", text)
        self.assertIn("PresentationIntent", text)
        self.assertIn("Project", text)

    def test_chest_presentation_phase_enum_is_comma_separated(self):
        text = self.read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationPhase.cs")
        for token in (
            "Closed,", "InteractionBegins,", "Opening,", "LightRewardEffect,",
            "ItemReveal,", "RewardPresentation,", "ItemCollection,",
        ):
            self.assertIn(token, text)

    def test_presentation_has_no_gameplay_reward_or_health_authority(self):
        root = ROOT / "Assets/ClickDungeon/Presentation"
        text = "\n".join(p.read_text() for p in root.rglob("*.cs")) if root.exists() else ""
        for forbidden in (
            "ApplyDamage(", "ApplyHealing(", "rewardGrantService.Grant", "RewardGrantService.Grant",
            "CurrencyState", "InventoryState", "CurrentHealth =", "RewardLedger",
        ):
            self.assertNotIn(forbidden, text)


if __name__ == "__main__":
    unittest.main()
