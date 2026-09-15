from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class CombatContractsTests(unittest.TestCase):
    def read(self, rel):
        path = ROOT / rel
        self.assertTrue(path.is_file(), f"missing {rel}")
        return path.read_text()

    def test_combat_assembly_and_generic_combatant_state_exist(self):
        asm = self.read("Assets/ClickDungeon/Combat/ClickDungeon.Combat.asmdef")
        self.assertIn('"ClickDungeon.Combat"', asm)
        state = self.read("Assets/ClickDungeon/Combat/CombatantState.cs")
        for token in ("MaxHealth", "CurrentHealth", "ApplyDamage", "ApplyHealing", "IsDefeated"):
            self.assertIn(token, state)
        self.assertIn("long", state, "health arithmetic must avoid int overflow")

    def test_damage_resolution_is_integer_and_rng_is_injected(self):
        text = self.read("Assets/ClickDungeon/Combat/CombatResolver.cs")
        self.assertIn("ICombatRandom", text)
        self.assertIn("ResolveAbility", text)
        self.assertIn("Math.Max(1", text)
        for forbidden in ("UnityEngine.Random", "System.Random", "DateTime", "Guid.NewGuid"):
            self.assertNotIn(forbidden, text)
        self.assertNotRegex(text, r"\b(float|double)\b")

    def test_nine_status_kinds_are_generic(self):
        text = self.read("Assets/ClickDungeon/Combat/StatusEffectState.cs")
        expected = ("Poison", "Burn", "Bleed", "Stun", "Slow", "Vulnerable", "Shielded", "Regeneration", "Marked")
        for token in expected:
            self.assertRegex(text, rf"\b{token}\b")
        self.assertIn("RemainingTurns", text)
        self.assertIn("Stacks", text)

    def test_ability_and_combat_event_contracts_exist(self):
        ability = self.read("Assets/ClickDungeon/Combat/AbilityDefinition.cs")
        for token in ("ContentId", "FlatDamage", "AttackScalingPermille", "CriticalChancePercent"):
            self.assertIn(token, ability)
        events = self.read("Assets/ClickDungeon/Combat/CombatEvents.cs")
        for token in ("AbilityUsed", "DamageApplied", "StatusApplied", "Defeated"):
            self.assertIn(token, events)

    def test_combat_kernel_has_no_identity_specific_or_presentation_logic(self):
        root = ROOT / "Assets/ClickDungeon/Combat"
        text = "\n".join(p.read_text() for p in root.rglob("*.cs")) if root.exists() else ""
        for forbidden in ("SirClickington", "sir_clickington", "Ironheart", "monster.", "hero.", "ClickDungeon.Presentation", "Animation", "AudioSource"):
            self.assertNotIn(forbidden, text)


if __name__ == "__main__":
    unittest.main()
