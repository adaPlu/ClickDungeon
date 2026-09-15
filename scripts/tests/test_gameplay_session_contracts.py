from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]


class GameplaySessionContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_application_assembly_consumes_dungeon_combat_and_progression(self):
        text = self.read("Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef")
        for assembly in ("ClickDungeon.Dungeon", "ClickDungeon.Combat", "ClickDungeon.Progression"):
            self.assertIn(assembly, text)

    def test_player_command_is_explicit_and_validated(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/PlayerCommand.cs")
        for token in ("PlayerCommandKind", "Move", "Ability", "Interact", "UseItem", "Wait", "TargetCoordinate", "ContentId"):
            self.assertIn(token, text)
        self.assertIn("Validate", text)

    def test_turn_result_is_immutable_event_output_for_presentation(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplayTurnResult.cs")
        for token in ("GameplayTurnEvent", "GameplayTurnPhase", "Command", "Tile", "PlayerCombat", "Enemy", "Reward"):
            self.assertIn(token, text)
        self.assertIn("IReadOnlyList", text)
        self.assertNotIn("UnityEngine", text)

    def test_session_order_is_command_then_tile_then_combat_enemy_then_reward(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        expected = (
            "ResolveCommand",
            "ResolveSpecialTileEvents",
            "ResolvePlayerCombat",
            "ResolveEnemyTurns",
            "ResolveRewards",
        )
        positions = [text.index(token) for token in expected]
        self.assertEqual(positions, sorted(positions), "turn phases must appear in canonical order")

    def test_application_applies_requested_tile_damage_and_healing(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        self.assertIn("SpecialTileEventKind.DamageRequested", text)
        self.assertIn("SpecialTileEventKind.HealingRequested", text)
        self.assertIn("player.ApplyDamage", text)
        self.assertIn("player.ApplyHealing", text)
        dungeon = self.read("Assets/ClickDungeon/Dungeon/Interaction/SpecialTileResolver.cs")
        self.assertNotIn("ApplyDamage(", dungeon)
        self.assertNotIn("ApplyHealing(", dungeon)

    def test_session_has_no_content_id_switch_chain_or_presentation_dependency(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        self.assertNotIn("ClickDungeon.Presentation", text)
        self.assertNotIn('case "tile.', text)
        self.assertNotIn('case "enemy.', text)
        self.assertNotIn('case "hero.', text)

    def test_reward_phase_uses_exactly_once_grant_service(self):
        text = self.read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
        self.assertIn("RewardGrantService", text)
        self.assertIn("rewardGrantService.Grant", text)


if __name__ == "__main__":
    unittest.main()
