from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class HeroContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_eight_art_defined_mechanical_classes_exist(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroClasses.cs")
        expected = (
            "class.knight", "class.thief", "class.wizard", "class.ranger",
            "class.cleric", "class.berserker", "class.engineer", "class.paladin",
        )
        for content_id in expected:
            self.assertIn(content_id, text)
        self.assertEqual(text.count('Add("class.'), 8)

    def test_canonical_hero_roster_contains_eight_roster_heroes_plus_clickington(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs")
        expected = (
            "hero.ironheart", "hero.shadowcut", "hero.emberwisp", "hero.windsong",
            "hero.lightbringer", "hero.rageclaw", "hero.gearspark", "hero.dawnward",
            "hero.sir_clickington",
        )
        for content_id in expected:
            self.assertIn(content_id, text)
        self.assertEqual(text.count('Add("hero.'), 9)

    def test_art_states_match_reference_library(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/HeroIdentityDefinition.cs")
        for token in (
            "MasterPose", "PortraitHud", "RosterIcon", "GameplayChibi",
            "Idle", "Attack", "Hit", "Victory", "Defeat",
        ):
            self.assertRegex(text, rf"\b{token}\b")

    def test_class_definitions_expose_role_combat_and_equipment_affinity_tags(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/HeroClassDefinition.cs")
        for token in ("RoleTags", "CombatTags", "EquipmentAffinityTags"):
            self.assertIn(token, text)
        canonical = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroClasses.cs")
        for token in (
            "sword", "shield", "heavy_armor",
            "daggers", "light_armor", "stealth_gear",
            "staff", "robes", "magical_focus",
            "longbow", "quiver", "ranger_leathers",
            "holy_tome", "vestments", "relic",
            "great_axe", "fur_armor", "rage_totem",
            "wrench", "goggles", "mechanical_core",
            "warhammer", "holy_relic",
        ):
            self.assertIn(token, canonical)

    def test_shadowcut_detailed_sheet_machine_id_wins_while_roster_label_is_retained(self):
        classes = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroClasses.cs")
        heroes = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs")
        self.assertIn('"class.thief", "Thief"', classes)
        self.assertIn('"hero.shadowcut"', heroes)
        self.assertIn('"class.thief"', heroes)
        self.assertIn('"Rogue"', heroes)

    def test_clickington_shares_knight_mechanics_but_not_identity_assets_or_story(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs")
        clickington = re.search(r'Add\("hero\.sir_clickington".*?items\);', text, re.S)
        self.assertIsNotNone(clickington, "Sir Clickington definition missing")
        block = clickington.group(0)
        self.assertIn('"class.knight"', block)
        self.assertIn('PresentationArchetype.Mascot', block)
        self.assertIn('"Mascot"', block)
        for token in (
            "art.hero.sir_clickington", "flavor.hero.sir_clickington",
            "dialogue.hero.sir_clickington", "campaign.sir_clickington",
        ):
            self.assertIn(token, block)
        self.assertNotIn("art.hero.ironheart", block)
        self.assertNotIn("campaign.ironheart", block)

    def test_ironheart_and_clickington_both_bind_to_knight(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs")
        self.assertGreaterEqual(text.count('"class.knight"'), 2)

    def test_content_layer_hero_models_do_not_depend_on_unity_runtime_objects(self):
        paths = [
            ROOT / "Assets/ClickDungeon/Content/Definitions/HeroClassDefinition.cs",
            ROOT / "Assets/ClickDungeon/Content/Definitions/HeroIdentityDefinition.cs",
            ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalHeroClasses.cs",
            ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs",
        ]
        text = "\n".join(p.read_text() for p in paths if p.exists())
        for forbidden in ("UnityEngine", "MonoBehaviour", "ScriptableObject", "GameObject"):
            self.assertNotIn(forbidden, text)


if __name__ == "__main__":
    unittest.main()
