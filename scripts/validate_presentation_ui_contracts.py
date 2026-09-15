#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
PRESENTATION = ROOT / "Assets/ClickDungeon/Presentation"
UI = ROOT / "Assets/ClickDungeon/UI"


def fail(message):
    print(f"presentation-ui-contract-error: {message}", file=sys.stderr)
    raise SystemExit(1)


def require(path):
    if not path.is_file(): fail(f"missing {path.relative_to(ROOT)}")
    return path.read_text()


def main():
    intent = require(PRESENTATION / "PresentationIntent.cs")
    projector = require(PRESENTATION / "GameplayEventProjector.cs")
    menu = require(UI / "MainMenu/MainMenuLayoutContract.cs")
    gameplay = require(UI / "Gameplay/GameplayHudContract.cs")
    heroes = require(UI / "HeroSelect/HeroSelectionContract.cs")

    for token in (
        "HeroProfile", "GoldStore", "GemStore", "ContinueRun", "Play", "HeroSelect", "Inventory",
        "Talents", "Shop", "Settings", "Quit", "Challenges", "Inbox", "Menu", "DailyRewardClaim",
    ):
        if token not in menu: fail(f"main menu route missing {token}")
    if "ProductBrand.PlayerFacingName" not in menu: fail("main menu does not use canonical player-facing brand")
    if "BoardColumns = 5" not in gameplay or "BoardRows = 5" not in gameplay: fail("gameplay board contract is not 5x5")
    for token in ("Move", "Slash", "Shield", "Dash", "Potion", "EnemyHealthBar", "SelectedCellHighlight"):
        if token not in gameplay: fail(f"gameplay HUD/action contract missing {token}")
    for token in ("SafeAreaInsets", "ResponsiveLayoutMode", "MinTouchTargetPixels"):
        if token not in menu + gameplay: fail(f"responsive contract missing {token}")
    if "CanonicalHeroes.All" not in heroes or "HeroIdentityDefinition" not in heroes:
        fail("hero selection is not identity-registry driven")
    for token in ("Ui", "Animation", "Audio", "Vfx", "Navigation"):
        if token not in intent: fail(f"presentation intent channel missing {token}")
    if "GameplayTurnResult" not in projector or "Project" not in projector:
        fail("gameplay event projector does not consume turn results")

    presentation_text = "\n".join(p.read_text() for p in PRESENTATION.rglob("*.cs"))
    for forbidden in (
        "ApplyDamage(", "ApplyHealing(", "rewardGrantService.Grant", "RewardGrantService.Grant",
        "CurrencyState", "InventoryState", "CurrentHealth =", "RewardLedger",
    ):
        if forbidden in presentation_text: fail(f"Presentation gained gameplay authority: {forbidden}")

    print("presentation/UI contracts: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
