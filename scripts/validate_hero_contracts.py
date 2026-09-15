#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "Assets/ClickDungeon/Content"


def fail(message):
    print(f"hero-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def read(rel):
    path = CONTENT / rel
    if not path.is_file():
        fail(f"missing {rel}")
    return path.read_text()


def main():
    class_def = read("Definitions/HeroClassDefinition.cs")
    identity_def = read("Definitions/HeroIdentityDefinition.cs")
    classes = read("Canonical/CanonicalHeroClasses.cs")
    heroes = read("Canonical/CanonicalHeroes.cs")

    class_ids = (
        "class.knight", "class.thief", "class.wizard", "class.ranger",
        "class.cleric", "class.berserker", "class.engineer", "class.paladin",
    )
    hero_ids = (
        "hero.ironheart", "hero.shadowcut", "hero.emberwisp", "hero.windsong",
        "hero.lightbringer", "hero.rageclaw", "hero.gearspark", "hero.dawnward",
        "hero.sir_clickington",
    )
    for content_id in class_ids:
        if content_id not in classes: fail(f"missing class {content_id}")
    for content_id in hero_ids:
        if content_id not in heroes: fail(f"missing hero {content_id}")
    if classes.count('Add("class.') != 8: fail("class registry must contain exactly eight canonical classes")
    if heroes.count('Add("hero.') != 9: fail("hero registry must contain eight roster heroes plus Sir Clickington")

    for token in ("RoleTags", "CombatTags", "EquipmentAffinityTags"):
        if token not in class_def: fail(f"hero class definition missing {token}")
    for token in ("MasterPose", "PortraitHud", "RosterIcon", "GameplayChibi", "Idle", "Attack", "Hit", "Victory", "Defeat"):
        if token not in identity_def: fail(f"hero art state missing {token}")

    clickington_marker = 'Add("hero.sir_clickington", "Sir Clickington", "class.knight", "Mascot", PresentationArchetype.Mascot'
    if clickington_marker not in heroes: fail("Sir Clickington must be mascot presentation on Knight mechanics")
    if heroes.count('"class.knight"') < 2: fail("Ironheart and Sir Clickington must both bind to Knight mechanics")
    if 'Add("hero.shadowcut", "Shadowcut", "class.thief", "Rogue"' not in heroes:
        fail("Shadowcut must preserve detailed-sheet thief machine ID and roster Rogue label")

    joined = "\n".join((class_def, identity_def, classes, heroes))
    for forbidden in ("UnityEngine", "MonoBehaviour", "ScriptableObject", "GameObject"):
        if forbidden in joined: fail(f"runtime Unity object leaked into content definitions: {forbidden}")

    print("hero contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
