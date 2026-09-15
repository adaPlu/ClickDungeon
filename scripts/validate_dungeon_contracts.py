#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
FILES = {
    "state": ROOT / "Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs",
    "link": ROOT / "Assets/ClickDungeon/Dungeon/Runtime/FloorLink.cs",
    "rng": ROOT / "Assets/ClickDungeon/Dungeon/Generation/DeterministicRng.cs",
    "generator": ROOT / "Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs",
    "validator": ROOT / "Assets/ClickDungeon/Dungeon/Generation/FloorValidator.cs",
    "special": ROOT / "Assets/ClickDungeon/Dungeon/Interaction/SpecialTileResolver.cs",
}


def fail(message):
    print(f"dungeon-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def main():
    for name, path in FILES.items():
        if not path.is_file(): fail(f"missing {name}: {path.relative_to(ROOT)}")
    state = FILES["state"].read_text()
    if "Width = 5" not in state or "Height = 5" not in state: fail("launch board must remain 5x5")
    rng = FILES["rng"].read_text()
    for token in ("UnityEngine.Random", "System.Random", "DateTime", "Guid.NewGuid", "Environment.TickCount"):
        if token in rng: fail(f"external entropy forbidden: {token}")
    validator = FILES["validator"].read_text()
    for token in ("ValidateReachableExit", "ValidateKeyBeforeMandatoryLock", "ValidateLinks"):
        if token not in validator: fail(f"floor validator missing {token}")
    special = FILES["special"].read_text()
    for token in ("DamageRequested", "HealingRequested", "TeleportRequested", "PressurePlateActivated"):
        if token not in special: fail(f"special tile resolver missing {token}")
    for token in ("CurrentHealth", "HitPoints", ".Heal(", ".Damage("):
        if token in special: fail(f"Dungeon gained forbidden HP authority: {token}")
    print("dungeon contracts: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
