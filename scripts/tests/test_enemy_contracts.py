from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class EnemyContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_ten_reference_roster_enemy_ids_exist(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalEnemies.cs")
        expected = (
            "enemy.goblin_brute_king", "enemy.crowned_slime", "enemy.skeleton_warrior",
            "enemy.bat_swarm_leader", "enemy.mimic_chest", "enemy.fire_imp",
            "enemy.armored_boar", "enemy.spooky_spellbook", "enemy.cave_spider",
            "enemy.theater_curtain_demon",
        )
        for content_id in expected:
            self.assertIn(content_id, text)
        self.assertEqual(text.count('Add("enemy.'), 10)

    def test_enemy_definition_carries_runtime_balance_and_registry_contracts(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/EnemyDefinition.cs")
        for token in (
            "BaseHealth", "Attack", "Defense", "Initiative", "XpReward", "MinDepth",
            "RarityWeight", "LootTableId", "BehaviorId", "SpriteContractPath", "ThreatTags",
        ):
            self.assertIn(token, text)

    def test_enemy_animation_states_match_reference_sheet(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/EnemyDefinition.cs")
        for token in ("Spawn", "Idle", "Attack", "Hit", "Defeat"):
            self.assertRegex(text, rf"\b{token}\b")

    def test_reference_threat_categories_and_trickster_are_represented(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/EnemyDefinition.cs")
        for token in ("Brute", "Swarm", "Undead", "Magic", "Beast", "Boss", "Trickster"):
            self.assertRegex(text, rf"\b{token}\b")

    def test_boss_definitions_have_mechanics_beyond_stats(self):
        text = self.read("Assets/ClickDungeon/Content/Definitions/BossDefinition.cs")
        for token in (
            "MechanicIds", "TelegraphIds", "PhaseIds", "SummonIds",
            "ArenaEffectIds", "DialogueTriggerIds", "RewardTableId",
        ):
            self.assertIn(token, text)
        bosses = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalBosses.cs")
        self.assertIn("GroundSlam", bosses)
        self.assertIn("DarkDive", bosses)
        self.assertIn("CurtainCall", bosses)
        self.assertIn("GoldenSplash", bosses)

    def test_lord_blobert_is_distinct_campaign_boss_using_reusable_slime_archetype(self):
        text = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalBosses.cs")
        self.assertIn('"boss.lord_blobert"', text)
        self.assertIn('"enemy.crowned_slime"', text)
        blobert = re.search(r'Add\("boss\.lord_blobert".*?items\);', text, re.S)
        self.assertIsNotNone(blobert)
        block = blobert.group(0)
        self.assertIn("GoldenSplash", block)
        self.assertIn("SummonMinions", block)
        self.assertIn("PuffsUp", block)

    def test_registry_lookup_is_strict_and_has_no_fallback_enemy(self):
        enemies = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalEnemies.cs")
        bosses = self.read("Assets/ClickDungeon/Content/Canonical/CanonicalBosses.cs")
        for text in (enemies, bosses):
            self.assertIn("GetRequired", text)
            self.assertIn("KeyNotFoundException", text)
            self.assertNotIn("return All[0]", text)
            self.assertNotIn("FirstOrDefault", text)
            self.assertNotIn("fallback", text.lower())

    def test_enemy_behavior_is_generic_not_id_switched(self):
        text = self.read("Assets/ClickDungeon/Combat/EnemyBehavior.cs")
        for token in ("EnemyBehaviorDefinition", "AbilityIds", "BehaviorTags"):
            self.assertIn(token, text)
        self.assertNotRegex(text, r"switch\s*\([^)]*(enemy|boss)")
        for forbidden in ("goblin_brute_king", "crowned_slime", "sir_clickington"):
            self.assertNotIn(forbidden, text)


if __name__ == "__main__":
    unittest.main()
