#!/usr/bin/env python3
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
ASMDEFS = {
    "Assets/ClickDungeon/Core/ClickDungeon.Core.asmdef": ("ClickDungeon.Core", []),
    "Assets/ClickDungeon/Content/ClickDungeon.Content.asmdef": ("ClickDungeon.Content", ["ClickDungeon.Core"]),
    "Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef": (
        "ClickDungeon.Application", ["ClickDungeon.Core", "ClickDungeon.Content"]),
    "Assets/ClickDungeon/Presentation/ClickDungeon.Presentation.asmdef": (
        "ClickDungeon.Presentation", ["ClickDungeon.Core", "ClickDungeon.Content", "ClickDungeon.Application"]),
    "Assets/ClickDungeon/Narrative/ClickDungeon.Narrative.asmdef": (
        "ClickDungeon.Narrative", ["ClickDungeon.Core", "ClickDungeon.Content"]),
}
ID_PATTERN = re.compile(r"^[a-z0-9]+(?:\.[a-z0-9_]+)+$")


def fail(message: str) -> None:
    print(f"foundation-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> int:
    for rel, (expected_name, expected_refs) in ASMDEFS.items():
        path = ROOT / rel
        if not path.is_file():
            fail(f"missing {rel}")
        data = json.loads(path.read_text())
        if data.get("name") != expected_name:
            fail(f"{rel} has wrong assembly name")
        if data.get("references", []) != expected_refs:
            fail(f"{rel} has wrong references")

    brand = (ROOT / "Assets/ClickDungeon/Core/Brand/ProductBrand.cs").read_text()
    if 'PlayerFacingName = "ClickDungeon"' not in brand or '"ClickDungeon2"' in brand:
        fail("player-facing brand contract violated")

    content_id = (ROOT / "Assets/ClickDungeon/Core/Content/ContentId.cs").read_text()
    if "UnityEngine" in content_id:
        fail("ContentId must remain Unity-independent")
    if not ID_PATTERN.fullmatch("tile.floor_stone"):
        fail("validator ID grammar is internally invalid")

    offenders = []
    runtime_root = ROOT / "Assets/ClickDungeon"
    for path in runtime_root.rglob("*"):
        if path.is_file() and path.suffix in {".cs", ".json", ".asset", ".uxml", ".uss"}:
            if "ClickDungeon2" in path.read_text(errors="ignore"):
                offenders.append(str(path.relative_to(ROOT)))
    if offenders:
        fail("legacy player brand found in: " + ", ".join(offenders))

    print("foundation contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
