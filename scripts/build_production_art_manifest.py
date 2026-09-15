#!/usr/bin/env python3
from pathlib import Path
import argparse
import json
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "art/production-art-manifest.json"

HERO_STATES = (
    "master_pose", "portrait_hud", "roster_icon", "gameplay_chibi",
    "idle", "attack", "hit", "victory", "defeat",
)
ENEMY_STATES = ("spawn", "idle", "attack", "hit", "defeat")
CHEST_PHASES = (
    "closed", "interaction_begins", "opening", "light_reward_effect",
    "item_reveal", "reward_presentation", "item_collection", "complete",
)
EXPLICIT_UI = (
    ("art.ui.title.background", "main_menu_background.png"),
    ("art.ui.title.logo", "clickdungeon_logo.png"),
    ("art.ui.main.hero_panel", "main_hero_panel.png"),
    ("art.ui.main.continue_panel", "continue_panel.png"),
    ("art.ui.main.daily_reward_panel", "daily_reward_panel.png"),
    ("art.ui.main.bottom_nav", "main_bottom_nav.png"),
    ("art.ui.gameplay.hud_frame", "gameplay_hud_frame.png"),
    ("art.ui.gameplay.action_button", "gameplay_action_button.png"),
    ("art.ui.gameplay.selected_cell", "selected_cell_highlight.png"),
    ("art.ui.hero_select.card", "hero_select_card.png"),
)
EXPLICIT_VFX = (
    ("art.vfx.slash", "slash.png"),
    ("art.vfx.hit", "hit.png"),
    ("art.vfx.victory", "victory.png"),
    ("art.vfx.shield_aura", "shield_aura.png"),
    ("art.vfx.reward_burst", "reward_burst.png"),
    ("art.vfx.teleport", "teleport.png"),
    ("art.vfx.heal", "heal.png"),
)


def asset(asset_id, category, path, source):
    return {
        "asset_id": asset_id,
        "category": category,
        "relative_path": path,
        "source_contract": source,
        "required": True,
    }


def build_assets():
    assets = []

    tile_text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs").read_text()
    tile_pattern = re.compile(
        r'Add\("(?P<id>tile\.[^"]+)",\s*"[^"]+",\s*TileLayer\.[^,]+,\s*"(?P<path>[^"]+)"',
        re.S,
    )
    for match in tile_pattern.finditer(tile_text):
        assets.append(asset(
            f"art.{match.group('id')}",
            "Tiles",
            f"Assets/ClickDungeon/{match.group('path')}",
            "CanonicalTiles.cs",
        ))

    hero_text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs").read_text()
    hero_pattern = re.compile(
        r'Add\("(?P<id>hero\.[^"]+)".*?"(?P<art>art\.hero\.[^"]+)".*?items\);',
        re.S,
    )
    for match in hero_pattern.finditer(hero_text):
        slug = match.group("id").split(".", 1)[1]
        for state in HERO_STATES:
            assets.append(asset(
                f"{match.group('art')}.{state}",
                "Heroes",
                f"Assets/ClickDungeon/Art/Runtime/Heroes/{slug}/{state}.png",
                "CanonicalHeroes.cs",
            ))

    enemy_text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalEnemies.cs").read_text()
    enemy_pattern = re.compile(
        r'Add\("(?P<id>enemy\.[^"]+)".*?"(?P<path>Art/Runtime/(?:Monsters|Bosses)/[^"]+)"\s*,\s*items\);',
        re.S,
    )
    for match in enemy_pattern.finditer(enemy_text):
        base = f"Assets/ClickDungeon/{match.group('path')}"
        category = "Bosses" if "/Bosses/" in base else "Monsters"
        for state in ENEMY_STATES:
            assets.append(asset(
                f"art.{match.group('id')}.{state}",
                category,
                f"{base}/{state}.png",
                "CanonicalEnemies.cs",
            ))

    item_text = (ROOT / "Assets/ClickDungeon/Content/Canonical/CanonicalItems.cs").read_text()
    item_pattern = re.compile(
        r'Add\("(?P<id>item\.[^"]+)".*?"(?P<path>Art/Runtime/Items/[^"]+)"\s*,\s*items\);',
        re.S,
    )
    for match in item_pattern.finditer(item_text):
        assets.append(asset(
            f"art.{match.group('id')}.icon",
            "Items",
            f"Assets/ClickDungeon/{match.group('path')}.png",
            "CanonicalItems.cs",
        ))

    for asset_id, filename in EXPLICIT_UI:
        assets.append(asset(asset_id, "UI", f"Assets/ClickDungeon/Art/Runtime/UI/{filename}", "MainMenuLayoutContract.cs + GameplayHudContract.cs"))

    for asset_id, filename in EXPLICIT_VFX:
        assets.append(asset(asset_id, "VFX", f"Assets/ClickDungeon/Art/Runtime/VFX/{filename}", "presentation cue contract"))

    for phase in CHEST_PHASES:
        assets.append(asset(
            f"art.chest.{phase}",
            "Chest",
            f"Assets/ClickDungeon/Art/Runtime/Chest/{phase}.png",
            "chest presentation contract",
        ))

    assets.sort(key=lambda entry: entry["asset_id"])
    return assets


def build_manifest():
    return {
        "schema_version": 1,
        "product": "ClickDungeon",
        "generated_from": [
            "Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs",
            "Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs",
            "Assets/ClickDungeon/Content/Canonical/CanonicalEnemies.cs",
            "Assets/ClickDungeon/Content/Canonical/CanonicalItems.cs",
            "Assets/ClickDungeon/UI/MainMenu/MainMenuLayoutContract.cs",
            "Assets/ClickDungeon/UI/Gameplay/GameplayHudContract.cs",
        ],
        "assets": build_assets(),
    }


def rendered():
    return json.dumps(build_manifest(), indent=2, sort_keys=False) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    expected = rendered()
    if args.check:
        if not MANIFEST.is_file():
            print("production-art-manifest-error: manifest missing", file=sys.stderr)
            return 1
        actual = MANIFEST.read_text()
        if actual != expected:
            print("production-art-manifest-error: manifest is stale; run builder without --check", file=sys.stderr)
            return 1
        print(f"production art manifest: PASS ({len(build_manifest()['assets'])} assets)")
        return 0
    MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST.write_text(expected)
    print(f"wrote {MANIFEST.relative_to(ROOT)} with {len(build_manifest()['assets'])} assets")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
