#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "Assets/ClickDungeon/Content"
PROGRESSION = ROOT / "Assets/ClickDungeon/Progression"


def fail(message):
    print(f"item-reward-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path):
    if not path.is_file(): fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    item = require(CONTENT / "Definitions/ItemDefinition.cs")
    canonical = require(CONTENT / "Canonical/CanonicalItems.cs")
    inventory = require(PROGRESSION / "InventoryState.cs")
    equipment = require(PROGRESSION / "EquipmentState.cs")
    loot = require(PROGRESSION / "LootResolver.cs")
    ledger = require(PROGRESSION / "RewardLedger.cs")
    grants = require(PROGRESSION / "RewardGrantService.cs")
    currency = require(PROGRESSION / "CurrencyState.cs")

    for token in ("Weapon", "Armor", "Offhand", "Accessory", "Consumable", "Key", "Currency", "QuestItem", "Relic"):
        if token not in item: fail(f"missing item category {token}")
    for token in ("Common", "Uncommon", "Rare", "Epic", "Legendary"):
        if token not in item: fail(f"missing item rarity {token}")
    for content_id in (
        "item.weapon.dragonslayer", "item.armor.void_plate", "item.weapon.celestial_staff",
        "item.offhand.aegis_of_dawn", "item.armor.crown_of_kings", "item.armor.boots_of_swiftness",
    ):
        if content_id not in canonical: fail(f"missing showcase item {content_id}")

    for forbidden in ("System.Random", "UnityEngine.Random", "DateTime", "Guid.NewGuid"):
        if forbidden in loot: fail(f"external loot entropy forbidden: {forbidden}")
    if "ulong seed" not in loot or "MixSeed" not in loot: fail("loot resolver must receive explicit deterministic seed")
    for token in ("TryBegin", "Commit", "Rollback", "IsCommitted", "HashSet"):
        if token not in ledger: fail(f"reward ledger missing {token}")
    for token in ("RewardLedger", "InventoryState", "CurrencyState", "Grant"):
        if token not in grants: fail(f"reward grant service missing {token}")
    for forbidden in ("Animation", "Animator", "Vfx", "ParticleSystem", "ClickDungeon.Presentation"):
        if forbidden in grants: fail(f"presentation leaked into reward authority: {forbidden}")
    for forbidden in ("UnityEngine", "ScriptableObject", "MonoBehaviour"):
        if forbidden in inventory + equipment + currency: fail(f"Unity runtime type leaked into progression state: {forbidden}")

    print("item/reward contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
