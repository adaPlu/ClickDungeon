from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class AnimationRewardContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()


    def test_repeated_interaction_chests_default_to_three_and_support_special_key_tags(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/ChestInteractionState.cs")
        for token in ("DefaultRequiredInteractions = 3", "RequiredKeyTag", "InteractionsCompleted", "RegisterInteraction", "IsReadyToCommit"):
            self.assertIn(token, text)

    def test_chest_reward_event_is_immutable_application_output(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/ChestRewardEvent.cs")
        self.assertIn("readonly struct ChestRewardEvent", text)
        for token in ("ChestId", "TransactionId", "ItemDefinitionId", "Quantity", "CurrencyId", "CurrencyAmount"):
            self.assertRegex(text, rf"public .* {token} \{{ get; \}}")
        self.assertNotIn(" set; ", text)

    def test_chest_transaction_id_is_deterministic_and_has_no_external_entropy(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        for token in ("BuildChestId", "BuildChestTransactionId", "runSeed", "FloorIndex", "ChestOrdinal"):
            self.assertIn(token, text)
        for forbidden in ("Guid.NewGuid", "System.Random", "UnityEngine.Random", "DateTime", "Environment.TickCount"):
            self.assertNotIn(forbidden, text)

    def test_exactly_once_grant_occurs_before_immutable_reward_event_creation(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        grant = text.index("rewardGrantService.Grant(chestGrant)")
        event_create = text.index("new ChestRewardEvent")
        self.assertLess(grant, event_create)
        self.assertIn("TryCommitChestReward", text)

    def test_eight_technical_chest_phases_are_exact(self):
        text = self.read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationPhase.cs")
        expected = (
            "Closed", "InteractionBegins", "Opening", "LightRewardEffect",
            "ItemReveal", "RewardPresentation", "ItemCollection", "Complete",
        )
        for token in expected:
            self.assertRegex(text, rf"\b{token}\b")
        enum = re.search(r'enum ChestPresentationPhase\s*\{(?P<body>.*?)\}', text, re.S)
        self.assertIsNotNone(enum)
        values = [v.strip().strip(',') for v in enum.group('body').splitlines() if v.strip()]
        self.assertEqual(values, list(expected))

    def test_four_reference_visual_beats_are_preserved(self):
        text = self.read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationSequence.cs")
        for token in ("Anticipation", "BurstReveal", "TooMuchToHandle", "Triumph", '"Burst/Reveal"', '"Too Much To Handle"'):
            self.assertIn(token, text)

    def test_input_is_locked_during_opening_reward_window_and_released_at_complete(self):
        text = self.read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationSequence.cs")
        self.assertIn("InputLocked", text)
        self.assertRegex(text, r"InteractionBegins.*?true", re.S)
        self.assertRegex(text, r"RewardPresentation.*?true", re.S)
        self.assertRegex(text, r"Complete.*?false", re.S)

    def test_canonical_presentation_cues_cover_chest_audio_vfx_and_collection(self):
        text = self.read("Assets/ClickDungeon/Presentation/CanonicalPresentationCues.cs")
        for token in (
            "ChestInteraction", "ChestOpening", "RewardBurst", "ItemReveal",
            "RewardPresentation", "ItemCollection", "ChestComplete",
        ):
            self.assertIn(token, text)

    def test_presentation_sequence_consumes_committed_event_and_never_grants_reward(self):
        text = self.read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationSequence.cs")
        self.assertIn("ChestRewardEvent", text)
        for forbidden in ("RewardGrantService", "RewardLedger", ".Grant(", "InventoryState", "CurrencyState"):
            self.assertNotIn(forbidden, text)

    def test_animation_validator_contains_both_adversarial_guards(self):
        text = self.read("scripts/validate_animation_reward_contracts.py")
        for token in ("presentation reward authority", "nondeterministic chest ID", "Guid.NewGuid", "RewardGrantService"):
            self.assertIn(token, text)

    def test_unity_validation_boundary_is_documented_without_false_green_claim(self):
        text = self.read("docs/validation/animation-reward-vfx-unity-blocked.md")
        for token in ("Unity", "blocked", "strict production art", "not claimed"):
            self.assertIn(token, text)


if __name__ == "__main__":
    unittest.main()
