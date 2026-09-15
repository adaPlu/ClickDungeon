#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
COMBAT = ROOT / "Assets/ClickDungeon/Combat"


def fail(message):
    print(f"combat-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def main():
    required = [
        "ClickDungeon.Combat.asmdef", "CombatantState.cs", "CombatResolver.cs",
        "AbilityDefinition.cs", "StatusEffectState.cs", "CombatEvents.cs"
    ]
    for name in required:
        if not (COMBAT / name).is_file(): fail(f"missing {name}")
    text = "\n".join(p.read_text() for p in COMBAT.rglob("*.cs"))
    for token in ("UnityEngine.Random", "System.Random", "DateTime", "Guid.NewGuid"):
        if token in text: fail(f"external combat entropy forbidden: {token}")
    for token in ("SirClickington", "sir_clickington", "Ironheart", "monster.", "hero.", "ClickDungeon.Presentation"):
        if token in text: fail(f"identity/presentation logic leaked into combat: {token}")
    status = (COMBAT / "StatusEffectState.cs").read_text()
    for token in ("Poison", "Burn", "Bleed", "Stun", "Slow", "Vulnerable", "Shielded", "Regeneration", "Marked"):
        if token not in status: fail(f"missing status kind {token}")
    state = (COMBAT / "CombatantState.cs").read_text()
    if "long" not in state: fail("health arithmetic must be widened before clamp")
    print("combat contracts: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
