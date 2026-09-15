#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "Assets/ClickDungeon/Content"
COMBAT = ROOT / "Assets/ClickDungeon/Combat"


def fail(message):
    print(f"enemy-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path):
    if not path.is_file(): fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    enemy_def = require(CONTENT / "Definitions/EnemyDefinition.cs")
    boss_def = require(CONTENT / "Definitions/BossDefinition.cs")
    enemies = require(CONTENT / "Canonical/CanonicalEnemies.cs")
    bosses = require(CONTENT / "Canonical/CanonicalBosses.cs")
    behavior = require(COMBAT / "EnemyBehavior.cs")

    enemy_ids = (
        "enemy.goblin_brute_king", "enemy.crowned_slime", "enemy.skeleton_warrior",
        "enemy.bat_swarm_leader", "enemy.mimic_chest", "enemy.fire_imp",
        "enemy.armored_boar", "enemy.spooky_spellbook", "enemy.cave_spider",
        "enemy.theater_curtain_demon",
    )
    for content_id in enemy_ids:
        if content_id not in enemies: fail(f"missing enemy {content_id}")
    if enemies.count('Add("enemy.') != 10: fail("enemy registry must contain exactly ten roster enemies")

    for token in ("Spawn", "Idle", "Attack", "Hit", "Defeat"):
        if token not in enemy_def: fail(f"missing enemy animation state {token}")
    for token in ("MechanicIds", "TelegraphIds", "PhaseIds", "SummonIds", "ArenaEffectIds", "DialogueTriggerIds"):
        if token not in boss_def: fail(f"boss definition missing {token}")
    for token in ("GroundSlam", "DarkDive", "CurtainCall", "GoldenSplash", "boss.lord_blobert"):
        if token not in bosses: fail(f"boss mechanic/identity missing {token}")

    for text, kind in ((enemies, "enemy"), (bosses, "boss")):
        if "GetRequired" not in text or "KeyNotFoundException" not in text:
            fail(f"{kind} lookup is not strict")
        if "FirstOrDefault" in text or "return All[0]" in text:
            fail(f"{kind} lookup may substitute unrelated content")

    if "EnemyBehaviorDefinition" not in behavior or "AbilityIds" not in behavior or "BehaviorTags" not in behavior:
        fail("generic enemy behavior contract incomplete")
    for identity in ("goblin_brute_king", "crowned_slime", "sir_clickington"):
        if identity in behavior: fail(f"identity-specific branch leaked into generic behavior: {identity}")

    print("enemy contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
