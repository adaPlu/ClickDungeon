#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
NARRATIVE = ROOT / "Assets/ClickDungeon/Narrative"


def fail(message):
    print(f"narrative-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(name):
    path = NARRATIVE / name
    if not path.is_file(): fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    dialogue = require("DialogueDefinition.cs")
    campaign = require("CampaignDefinition.cs")
    state = require("CampaignState.cs")
    resolver = require("NarrativeResolver.cs")
    canonical = require("CanonicalSirClickingtonCampaign.cs")

    for token in ("Neutral", "Happy", "Confident", "Worried", "Shocked", "Angry", "Victorious", "Defeated"):
        if token not in dialogue: fail(f"missing Clickington expression {token}")
    for token in ("HeroId", "PrimaryBossId", "TriggerId", "DialogueId", "NextEventId"):
        if token not in campaign: fail(f"campaign definition missing {token}")
    for token in ("campaign.sir_clickington", "hero.sir_clickington", "boss.lord_blobert"):
        if token not in canonical: fail(f"Clickington campaign missing {token}")
    if '"enemy.crowned_slime"' in canonical: fail("Lord Blobert campaign collapsed into Crowned Slime identity")
    for token in ("CampaignId", "CurrentEventId", "CompletedEventIds", "Advance", "HashSet"):
        if token not in state: fail(f"campaign state missing {token}")
    for forbidden in ("sir_clickington", "lord_blobert", "System.Random", "UnityEngine.Random", "DateTime", "Guid.NewGuid"):
        if forbidden in resolver: fail(f"identity/entropy leaked into generic resolver: {forbidden}")

    all_text = "\n".join(p.read_text() for p in NARRATIVE.rglob("*.cs"))
    for forbidden in ("CombatResolver", "ApplyDamage(", "ApplyHealing(", "RewardGrantService", "InventoryState", "CurrencyState", "SaveService", "PlayerPrefs"):
        if forbidden in all_text: fail(f"narrative gained forbidden authority: {forbidden}")

    print("narrative contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
