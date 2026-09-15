from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class NarrativeContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_clickington_expression_states_match_reference(self):
        text = self.read("Assets/ClickDungeon/Narrative/DialogueDefinition.cs")
        for token in ("Neutral", "Happy", "Confident", "Worried", "Shocked", "Angry", "Victorious", "Defeated"):
            self.assertIn(token, text)

    def test_campaign_definition_separates_hero_boss_and_event_data(self):
        text = self.read("Assets/ClickDungeon/Narrative/CampaignDefinition.cs")
        for token in ("HeroId", "PrimaryBossId", "CampaignEventDefinition", "TriggerId", "DialogueId", "NextEventId"):
            self.assertIn(token, text)

    def test_clickington_campaign_uses_distinct_lord_blobert_boss_id(self):
        text = self.read("Assets/ClickDungeon/Narrative/CanonicalSirClickingtonCampaign.cs")
        for token in ("campaign.sir_clickington", "hero.sir_clickington", "boss.lord_blobert"):
            self.assertIn(token, text)
        self.assertNotIn('"enemy.crowned_slime"', text)

    def test_clickington_campaign_contains_comedic_identity_and_blobert_story_beats(self):
        text = self.read("Assets/ClickDungeon/Narrative/CanonicalSirClickingtonCampaign.cs")
        for token in ("Small Knight", "Big Enthusiasm", "Adventure looks better together", "Lord Blobert", "magnificent"):
            self.assertIn(token, text)

    def test_campaign_state_is_runtime_progress_only(self):
        text = self.read("Assets/ClickDungeon/Narrative/CampaignState.cs")
        for token in ("CampaignId", "CurrentEventId", "CompletedEventIds", "Advance", "HashSet"):
            self.assertIn(token, text)
        for forbidden in ("UnityEngine", "ScriptableObject", "PlayerPrefs"):
            self.assertNotIn(forbidden, text)

    def test_narrative_resolver_is_generic_and_deterministic(self):
        text = self.read("Assets/ClickDungeon/Narrative/NarrativeResolver.cs")
        for token in ("NarrativeTrigger", "Resolve", "TriggerId", "CurrentEventId"):
            self.assertIn(token, text)
        for forbidden in ("sir_clickington", "lord_blobert", "System.Random", "UnityEngine.Random", "DateTime", "Guid.NewGuid"):
            self.assertNotIn(forbidden, text)

    def test_narrative_layer_has_no_combat_save_or_reward_authority(self):
        root = ROOT / "Assets/ClickDungeon/Narrative"
        text = "\n".join(p.read_text() for p in root.rglob("*.cs"))
        for forbidden in (
            "CombatResolver", "ApplyDamage(", "ApplyHealing(", "RewardGrantService", "InventoryState",
            "CurrencyState", "SaveService", "File.Write", "PlayerPrefs",
        ):
            self.assertNotIn(forbidden, text)


if __name__ == "__main__":
    unittest.main()
