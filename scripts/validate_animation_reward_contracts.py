#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]


def fail(message):
    print(f"animation/reward contracts: FAIL: {message}")
    raise SystemExit(1)


def read(rel):
    path = ROOT / rel
    if not path.is_file():
        fail(f"missing {rel}")
    return path.read_text()


def main():
    session = read("Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs")
    presentation_files = list((ROOT / "Assets/ClickDungeon/Presentation").rglob("*.cs"))
    presentation = "\n".join(path.read_text() for path in presentation_files)

    nondeterministic = ("Guid.NewGuid", "System.Random", "UnityEngine.Random", "DateTime", "Environment.TickCount")
    for token in nondeterministic:
        if token in session:
            fail(f"nondeterministic chest ID uses {token}")

    if "BuildChestTransactionId" not in session or "runSeed" not in session or "ChestOrdinal" not in session:
        fail("nondeterministic chest ID contract is incomplete")

    forbidden_authority = ("RewardGrantService", "RewardLedger", ".Grant(", "InventoryState", "CurrencyState")
    for token in forbidden_authority:
        if token in presentation:
            fail(f"presentation reward authority token detected: {token}")

    phases = read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationPhase.cs")
    for token in ("Closed", "InteractionBegins", "Opening", "LightRewardEffect", "ItemReveal", "RewardPresentation", "ItemCollection", "Complete"):
        if token not in phases:
            fail(f"missing chest phase {token}")

    sequence = read("Assets/ClickDungeon/Presentation/Chest/ChestPresentationSequence.cs")
    for token in ("Anticipation", "Burst/Reveal", "Too Much To Handle", "Triumph", "InputLocked"):
        if token not in sequence:
            fail(f"missing presentation beat {token}")

    print("animation/reward contracts: PASS")


if __name__ == "__main__":
    main()
