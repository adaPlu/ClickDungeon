#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "Assets/ClickDungeon/Application"
DUNGEON = ROOT / "Assets/ClickDungeon/Dungeon"


def fail(message):
    print(f"gameplay-session-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path):
    if not path.is_file(): fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    asm = require(APP / "ClickDungeon.Application.asmdef")
    command = require(APP / "Gameplay/PlayerCommand.cs")
    result = require(APP / "Gameplay/GameplayTurnResult.cs")
    session = require(APP / "Gameplay/GameplaySession.cs")
    special = require(DUNGEON / "Interaction/SpecialTileResolver.cs")

    for assembly in ("ClickDungeon.Dungeon", "ClickDungeon.Combat", "ClickDungeon.Progression"):
        if assembly not in asm: fail(f"Application missing assembly dependency {assembly}")
    for token in ("PlayerCommandKind", "Move", "Ability", "Interact", "UseItem", "Wait", "Validate"):
        if token not in command: fail(f"command contract missing {token}")
    for token in ("GameplayTurnEvent", "GameplayTurnPhase", "IReadOnlyList"):
        if token not in result: fail(f"turn result missing {token}")

    order = ("ResolveCommand", "ResolveSpecialTileEvents", "ResolvePlayerCombat", "ResolveEnemyTurns", "ResolveRewards")
    positions = [session.find(token) for token in order]
    if any(position < 0 for position in positions) or positions != sorted(positions):
        fail("turn phase ordering is not canonical")
    for token in ("SpecialTileEventKind.DamageRequested", "SpecialTileEventKind.HealingRequested", "player.ApplyDamage", "player.ApplyHealing"):
        if token not in session: fail(f"Application authority missing {token}")
    if "ApplyDamage(" in special or "ApplyHealing(" in special:
        fail("Dungeon special-tile resolver mutated HP directly")
    if "RewardGrantService" not in session or "rewardGrantService.Grant" not in session:
        fail("reward grant authority is not sequenced by Application")
    for forbidden in ("ClickDungeon.Presentation", 'case "tile.', 'case "enemy.', 'case "hero.'):
        if forbidden in session: fail(f"forbidden presentation/identity branch in GameplaySession: {forbidden}")

    print("gameplay session contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
