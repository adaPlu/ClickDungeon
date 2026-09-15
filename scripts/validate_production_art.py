#!/usr/bin/env python3
from pathlib import Path
import argparse
import json
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "art/production-art-manifest.json"
ALLOWED_ROOTS = {
    "Tiles", "Heroes", "Monsters", "Bosses", "Items", "UI", "VFX", "Chest"
}


def fail(message):
    print(f"production-art-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--strict", action="store_true", help="require runtime files and Unity .meta files")
    args = parser.parse_args()

    check = subprocess.run(
        [sys.executable, str(ROOT / "scripts/build_production_art_manifest.py"), "--check"],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    if check.returncode != 0:
        fail(check.stderr.strip() or check.stdout.strip() or "generated manifest mismatch")

    data = json.loads(MANIFEST.read_text())
    if data.get("schema_version") != 1: fail("unsupported schema_version")
    if data.get("product") != "ClickDungeon": fail("wrong product")
    assets = data.get("assets")
    if not isinstance(assets, list) or not assets: fail("assets must be a non-empty list")

    seen_ids = set()
    seen_paths = set()
    for entry in assets:
        for key in ("asset_id", "category", "relative_path", "source_contract", "required"):
            if key not in entry: fail(f"asset entry missing {key}")
        asset_id = entry["asset_id"]
        relative_path = entry["relative_path"]
        if asset_id in seen_ids: fail(f"duplicate asset_id: {asset_id}")
        if relative_path in seen_paths: fail(f"duplicate relative_path: {relative_path}")
        seen_ids.add(asset_id)
        seen_paths.add(relative_path)

        prefix = "Assets/ClickDungeon/Art/Runtime/"
        if not relative_path.startswith(prefix): fail(f"runtime path outside allowed root: {relative_path}")
        remainder = relative_path[len(prefix):]
        root_name = remainder.split("/", 1)[0]
        if root_name not in ALLOWED_ROOTS: fail(f"runtime category folder not allowed: {relative_path}")
        if "docs/reference" in relative_path: fail(f"reference sheet cannot be runtime art: {relative_path}")

        if args.strict and entry["required"]:
            runtime_path = ROOT / relative_path
            if not runtime_path.is_file(): fail(f"missing runtime asset: {relative_path}")
            meta_path = Path(str(runtime_path) + ".meta")
            if not meta_path.is_file(): fail(f"missing Unity .meta: {relative_path}.meta")

    mode = "strict" if args.strict else "source"
    print(f"production art validation: PASS ({mode}, {len(assets)} assets)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
