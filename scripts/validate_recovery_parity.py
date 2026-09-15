#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def fail(kind, path, token):
    raise SystemExit(f"recovery parity: FAIL: {kind}: {path}: {token}")


def cs_files(root):
    return sorted(root.rglob("*.cs")) if root.exists() else []


def scan_forbidden(files, kind, tokens):
    for path in files:
        text = path.read_text()
        for token in tokens:
            if token in text:
                fail(kind, path.relative_to(ROOT), token)


def main():
    runtime = ROOT / "Assets/ClickDungeon"
    presentation = runtime / "Presentation"
    definitions = runtime / "Content/Definitions"
    canonical = runtime / "Content/Canonical"
    deterministic_roots = [
        runtime / "Application",
        runtime / "Combat",
        runtime / "Dungeon",
        runtime / "Progression",
    ]

    # ClickDungeon2 runtime drift
    scan_forbidden(cs_files(runtime), "ClickDungeon2 runtime drift", ("ClickDungeon2",))

    # presentation gameplay authority
    scan_forbidden(
        cs_files(presentation),
        "presentation gameplay authority",
        ("RewardGrantService", "RewardLedger", ".Grant(", "InventoryState", "CurrencyState", ".ApplyDamage(", ".ApplyHealing("),
    )

    # mutable definition state
    for path in cs_files(definitions):
        text = path.read_text()
        if re.search(r"\{\s*get;\s*set;\s*\}", text):
            fail("mutable definition state", path.relative_to(ROOT), "public setter")
        if re.search(r"\b(List|Dictionary|HashSet)<", text):
            fail("mutable definition state", path.relative_to(ROOT), "mutable collection type")

    # unknown-ID fallback
    scan_forbidden(
        cs_files(canonical),
        "unknown-ID fallback",
        ("FirstOrDefault", "return default;", "return null;", "GetValueOrDefault"),
    )

    # nondeterministic entropy
    deterministic_files = []
    for root in deterministic_roots:
        deterministic_files.extend(cs_files(root))
    scan_forbidden(
        deterministic_files,
        "nondeterministic entropy",
        ("Guid.NewGuid", "System.Random", "UnityEngine.Random", "DateTime.Now", "DateTime.UtcNow", "Environment.TickCount"),
    )

    print("recovery parity: PASS")


if __name__ == "__main__":
    main()
