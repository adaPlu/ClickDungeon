# ClickDungeon Recovery Parity Report

## Scope

This checkpoint reconstructs the greenfield `Clicked` source boundary through **Animation / Chest Rewards / VFX**. The player-facing product remains **ClickDungeon**. The supplied reference imagery is the visual source of truth; composite sheets are never treated as isolated runtime production sprites.

The implementation state verified immediately before this parity-only checkpoint was:

- implementation head: `95d2d3802b8a3c967ef1a4a5dea65e334f10be67`
- source-contract tests: **86 passed, 0 failed**
- source production-art manifest: **200 assets validated**
- `git diff --check`: clean

Task 13 itself adds only validation/reporting/CI files. A fresh full parity run is required again after the Task 13 commit before the branch may be described as complete.

## Subsystem parity map

| Reconstructed boundary | Fresh evidence |
| --- | --- |
| Foundation / assembly graph / branding / content IDs | `test_foundation_contracts.py`, `validate_foundation_contracts.py` |
| Exact 24-tile registry and independent tile layers | `test_tile_registry_contracts.py`, `validate_tile_registry.py` |
| Deterministic 5×5 floor generation, links, special tiles | `test_dungeon_contracts.py`, `validate_dungeon_contracts.py` |
| Generic deterministic combat, abilities, nine status kinds | `test_combat_contracts.py`, `validate_combat_contracts.py` |
| Hero classes and identities, including Sir Clickington → shared Knight mechanics | `test_hero_contracts.py`, `validate_hero_contracts.py` |
| Canonical monsters/bosses and strict unknown-ID handling | `test_enemy_contracts.py`, `validate_enemy_contracts.py` |
| Items, inventory/equipment, deterministic loot, exactly-once reward ledger | `test_item_reward_contracts.py`, `validate_item_reward_contracts.py` |
| Gameplay session ordering and Application-owned HP/reward authority | `test_gameplay_session_contracts.py`, `validate_gameplay_session_contracts.py` |
| Main menu, gameplay UI, hero selection, presentation-only intents | `test_presentation_ui_contracts.py`, `validate_presentation_ui_contracts.py` |
| Sir Clickington narrative / Lord Blobert campaign contracts | `test_narrative_contracts.py`, `validate_narrative_contracts.py` |
| Production-art inventory/readiness source gate | `test_production_art_contracts.py`, `validate_production_art.py` |
| Chest interaction, deterministic reward commit, 8-phase sequence, 4 visual beats, VFX/audio cue boundary | `test_animation_reward_contracts.py`, `validate_animation_reward_contracts.py` |
| Cross-cutting recovery guards | `test_recovery_parity_contracts.py`, `validate_recovery_parity.py` |

## Recovery-wide forbidden-pattern guards

`validate_recovery_parity.py` rejects:

- player-facing/runtime `ClickDungeon2` drift,
- Presentation gameplay/reward/HP authority,
- mutable definition state,
- unknown-ID fallback patterns in canonical content,
- nondeterministic gameplay entropy such as GUID/time/uncontrolled random sources.

Task 12 additionally proved two adversarial mutations are rejected: injected Presentation reward authority and injected `Guid.NewGuid` chest-ID entropy.

## Reference-art continuity

The re-uploaded title screen, core gameplay screen, Sir Clickington detail sheet, and hero roster were SHA-256 compared against the pinned copies and were byte-identical. No reference-art drift was introduced during reconstruction.

## Unity boundary

**Unity: BLOCKED.** No Unity editor is installed in this execution environment. Unity compilation, EditMode, PlayMode, scene/runtime visual verification, Windows/Android/iOS builds, and strict imported-production-art validation are **not claimed** as green.

The **strict production art** gate also remains blocked until isolated runtime sprites and matching Unity `.meta` files exist. Source-contract validation does not substitute for that Unity/import boundary.
