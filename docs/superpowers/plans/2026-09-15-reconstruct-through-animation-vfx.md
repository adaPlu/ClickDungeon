# ClickDungeon Reconstruction Through Animation/VFX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reconstruct the lost greenfield `Clicked` Unity source tree from the approved design and supplied reference art, re-establish the previously verified architecture through Animation / Chest Rewards / VFX, and create a durable Git checkpoint before any new feature phase begins.

**Architecture:** Rebuild the same dependency spine: immutable definition data → mutable runtime state → deterministic simulation → application orchestration → presentation intents → Unity-facing adapters. Because Unity is not installed in this execution environment, every task must provide Unity-independent source-contract validation now and clearly mark Unity compile/EditMode/PlayMode/build validation as blocked rather than claiming it passed.

**Tech Stack:** Unity 6 source layout, C#, Python 3 source-contract validators, Git, Git worktrees, GitHub Actions source validation. No release credentials or signing secrets.

**Spec:** `docs/specs/clickdungeon-master-design.md`

## Global Constraints

- Repository: `Clicked`; player-facing title: `ClickDungeon`.
- Player-facing/runtime content must not display `ClickDungeon2`; legacy-logo reference sheets may exist only as non-runtime art references.
- Target platforms remain Windows, Android, and iOS.
- Launch board is deterministic 5×5.
- Tile presentation order is `Base Terrain → Structure → Content → Actor → State Overlay`.
- Initial canonical tile set is exactly the 24 concepts in the approved design.
- Sir Clickington is a unique named hero identity with **presentation archetype `Mascot`**, but his **mechanical class remains `class.knight`**, shared with Ironheart. No duplicated Clickington combat implementation.
- Initial hero roster remains Ironheart, Shadowcut, Emberwisp, Windsong, Lightbringer, Rageclaw, Gearspark, Dawnward, plus Sir Clickington as a distinct named identity.
- Initial enemy roster remains Goblin Brute King, Crowned Slime, Skeleton Warrior, Bat Swarm Leader, Mimic Chest, Fire Imp, Armored Boar, Spooky Spellbook, Cave Spider, Theater Curtain Demon; Lord Blobert is a distinct campaign-boss identity built on reusable slime/boss mechanics.
- Chest rewards are committed by simulation/application exactly once before presentation. Animation/VFX/audio can never grant gameplay rewards.
- Determinism must not use `UnityEngine.Random`, `System.Random` without a controlled seed abstraction, time, GUID generation, or hash-order iteration as gameplay entropy.
- Shared definition assets must not hold mutable runtime state.
- New code follows TDD: failing source contract first, minimal implementation second, full regression gate third.
- Each task ends with a commit and a task review before proceeding.
- No production/runtime art is fabricated from composite reference sheets. Runtime art readiness remains blocked until isolated production sprites and Unity `.meta` files exist.
- Unity compile/EditMode/PlayMode/build claims are forbidden in this environment because no Unity editor is installed.

---

## Task 0: Recover Repository Baseline and Recovery Ledger

**Objective:** Create durable local Git history from the surviving approved docs before any production reconstruction begins.

**Files:**
- Create: `.gitignore`
- Create: `.superpowers/sdd/reconstruct-through-animation-vfx/progress.md` (git-ignored ledger)
- Modify: `docs/specs/clickdungeon-master-design.md`
- Existing: `README.md`
- Existing: `docs/superpowers/plans/2026-09-14-foundation-canonical-content.md`

**Interfaces:**
- Consumes: surviving approved design and current supplied art.
- Produces: clean Git baseline on `main`, ignored `.worktrees/`, and an isolated branch `recovery/reconstruct-204-boundary`.

- [ ] Add `.worktrees/` and `.superpowers/` to `.gitignore` along with standard Unity generated directories.
- [ ] Add the approved Sir Clickington clarification to the design: presentation archetype `Mascot`; mechanical `classId = class.knight`; unique identity/story/art.
- [ ] Initialize Git, commit surviving docs as `chore: recover ClickDungeon design baseline`.
- [ ] Create `.worktrees/reconstruct-204-boundary` on branch `recovery/reconstruct-204-boundary`.
- [ ] Create the SDD ledger in the worktree and record Task 0 complete.
- [ ] Verify `git status --short` is clean before Task 1.

## Task 1: Rebuild Source Skeleton, Assemblies, Branding, and Canonical IDs

**Objective:** Recreate the dependency spine and foundational content contracts without claiming Unity compilation.

**Files:**
- Create: `Assets/ClickDungeon/Core/ClickDungeon.Core.asmdef`
- Create: `Assets/ClickDungeon/Content/ClickDungeon.Content.asmdef`
- Create: `Assets/ClickDungeon/Application/ClickDungeon.Application.asmdef`
- Create: `Assets/ClickDungeon/Presentation/ClickDungeon.Presentation.asmdef`
- Create: `Assets/ClickDungeon/Narrative/ClickDungeon.Narrative.asmdef`
- Create: `Assets/ClickDungeon/Core/Brand/ProductBrand.cs`
- Create: `Assets/ClickDungeon/Core/Content/ContentId.cs`
- Create: `scripts/tests/test_foundation_contracts.py`
- Create: `scripts/validate_foundation_contracts.py`

**Interfaces:**
- Produces: `ProductBrand.PlayerFacingName`, validated `ContentId`, and assembly dependency contracts consumed by all later tasks.

- [ ] Write failing Python source contracts proving required asmdefs/types do not yet exist.
- [ ] Run the focused test and confirm RED for the intended missing files.
- [ ] Create minimal asmdefs and C# foundation types.
- [ ] Implement validator for branding, namespace boundaries, and duplicate/invalid content IDs.
- [ ] Run focused tests GREEN, then all source tests.
- [ ] Commit `feat: reconstruct ClickDungeon foundation contracts`.

## Task 2: Rebuild Canonical Tile and Layered Dungeon Model

**Objective:** Restore the 24-tile registry and layered floor model so terrain can never replace content/actors.

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/TileDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalTiles.cs`
- Create: `Assets/ClickDungeon/Dungeon/TileLayers.cs`
- Create: `Assets/ClickDungeon/Dungeon/FloorCell.cs`
- Create: `scripts/tests/test_tile_registry_contracts.py`
- Create: `scripts/validate_tile_registry.py`

**Interfaces:**
- Produces: exactly 24 stable tile definitions and `FloorCell` with independent terrain/structure/content/actor/overlay fields.

- [ ] Write failing contracts for all 24 IDs and independent layer fields.
- [ ] Verify RED.
- [ ] Implement definitions and canonical registry.
- [ ] Validate exactly-one identity, legal layer, unique sprite contract path, and no terrain/content aliasing.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct canonical dungeon tile model`.

## Task 3: Rebuild Deterministic Dungeon State, Links, Generation Contracts, and Special Tiles

**Objective:** Restore deterministic 5×5 floor state, links, validation, and generic special-tile event vocabulary.

**Files:**
- Create: `Assets/ClickDungeon/Dungeon/Runtime/FloorState.cs`
- Create: `Assets/ClickDungeon/Dungeon/Runtime/FloorLink.cs`
- Create: `Assets/ClickDungeon/Dungeon/Generation/DeterministicRng.cs`
- Create: `Assets/ClickDungeon/Dungeon/Generation/DungeonGenerator.cs`
- Create: `Assets/ClickDungeon/Dungeon/Generation/FloorValidator.cs`
- Create: `Assets/ClickDungeon/Dungeon/Interaction/SpecialTileResolver.cs`
- Create: `scripts/tests/test_dungeon_contracts.py`
- Create: `scripts/validate_dungeon_contracts.py`

**Interfaces:**
- Produces: seeded floor generation contract; generic link kinds for teleport/pressure mechanisms; terrain damage/heal/teleport events without direct HP authority.

- [ ] Write failing contracts for 5×5 deterministic seed inputs, start/exit validity, key-before-lock validation, link validation, one-shot pressure plates, non-recursive teleport, and healing-request events.
- [ ] Verify RED.
- [ ] Implement minimal deterministic POCO models and resolvers.
- [ ] Reject malformed/unreachable floors deterministically.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct deterministic dungeon systems`.

## Task 4: Rebuild Combat, Abilities, Status, and Turn Authority

**Objective:** Restore deterministic combat with simulation as sole gameplay authority.

**Files:**
- Create: `Assets/ClickDungeon/Combat/CombatantState.cs`
- Create: `Assets/ClickDungeon/Combat/CombatResolver.cs`
- Create: `Assets/ClickDungeon/Combat/AbilityDefinition.cs`
- Create: `Assets/ClickDungeon/Combat/StatusEffectState.cs`
- Create: `Assets/ClickDungeon/Combat/CombatEvents.cs`
- Create: `scripts/tests/test_combat_contracts.py`
- Create: `scripts/validate_combat_contracts.py`

**Interfaces:**
- Produces: deterministic damage/events; Presentation receives events but cannot mutate HP.

- [ ] Write failing contracts for damage formula, min damage, deterministic crit input, defeat event, status boundary, and forbidden Presentation HP mutation.
- [ ] Verify RED.
- [ ] Implement minimal combat/state/event types.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct deterministic combat authority`.

## Task 5: Rebuild Hero Classes and Identity Registry

**Objective:** Restore data-driven hero mechanics/identity split using the detailed reference sheets.

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/HeroClassDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Definitions/HeroIdentityDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalHeroClasses.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalHeroes.cs`
- Create: `scripts/tests/test_hero_contracts.py`
- Create: `scripts/validate_hero_contracts.py`

**Interfaces:**
- Produces: eight mechanical classes plus Sir Clickington identity bound to Knight.

- [ ] Write failing contracts for roster/class IDs, art-state IDs, equipment affinity tags, and Clickington/Ironheart mechanic sharing.
- [ ] Verify RED.
- [ ] Implement class/identity definitions.
- [ ] Enforce `hero.sir_clickington.classId == class.knight`, `presentationArchetype == Mascot`, and unique identity/story/art IDs.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct hero class and identity registry`.

## Task 6: Rebuild Monster/Boss Registry and Safe Mapping

**Objective:** Restore the canonical enemy roster, boss behavior hooks, and fail-safe unknown-ID handling.

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/EnemyDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Definitions/BossDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalEnemies.cs`
- Create: `Assets/ClickDungeon/Combat/EnemyBehavior.cs`
- Create: `scripts/tests/test_enemy_contracts.py`
- Create: `scripts/validate_enemy_contracts.py`

**Interfaces:**
- Produces: ten canonical enemy identities; boss mechanic descriptors; strict registry lookup.

- [ ] Write failing contracts for all ten identities, animation states Spawn/Idle/Attack/Hit/Defeat, non-stat-only boss hooks, strict unknown-ID failure, and Lord Blobert distinct identity mapping.
- [ ] Verify RED.
- [ ] Implement definitions/registry/behavior descriptors.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct monster and boss registry`.

## Task 7: Rebuild Items, Loot, Inventory, Equipment, Currency, and Exactly-Once Reward Ledger

**Objective:** Restore the item taxonomy from the supplied equipment imagery and deterministic reward authority.

**Files:**
- Create: `Assets/ClickDungeon/Content/Definitions/ItemDefinition.cs`
- Create: `Assets/ClickDungeon/Content/Canonical/CanonicalItems.cs`
- Create: `Assets/ClickDungeon/Progression/InventoryState.cs`
- Create: `Assets/ClickDungeon/Progression/EquipmentState.cs`
- Create: `Assets/ClickDungeon/Progression/LootResolver.cs`
- Create: `Assets/ClickDungeon/Progression/RewardLedger.cs`
- Create: `Assets/ClickDungeon/Progression/RewardGrantService.cs`
- Create: `scripts/tests/test_item_reward_contracts.py`
- Create: `scripts/validate_item_reward_contracts.py`

**Interfaces:**
- Produces: data-driven item categories/rarities, inventory/equipment, deterministic loot result, idempotent reward transaction ledger.

- [ ] Write failing contracts for categories, rarity IDs, deterministic loot seed input, duplicate grant rejection, and inventory/equipment boundaries.
- [ ] Verify RED.
- [ ] Implement minimal types/services.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct item loot and reward authority`.

## Task 8: Rebuild Gameplay Session and Turn Integration

**Objective:** Restore Application orchestration across movement, interaction, special tiles, combat, rewards, and presentation events.

**Files:**
- Create: `Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs`
- Create: `Assets/ClickDungeon/Application/Gameplay/GameplayTurnResult.cs`
- Create: `Assets/ClickDungeon/Application/Gameplay/PlayerCommand.cs`
- Create: `scripts/tests/test_gameplay_session_contracts.py`
- Create: `scripts/validate_gameplay_session_contracts.py`

**Interfaces:**
- Consumes: Dungeon, Combat, Progression.
- Produces: immutable turn results/events for Presentation.

- [ ] Write failing contracts for player-command validation, move→tile-event→combat/enemy→reward ordering, and Application-only application of requested damage/healing.
- [ ] Verify RED.
- [ ] Implement orchestration without content-ID switch chains.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct gameplay session orchestration`.

## Task 9: Rebuild Presentation Intents, Gameplay UI Contracts, Main Menu, and Hero Selection

**Objective:** Restore player-facing presentation contracts without granting gameplay authority.

**Files:**
- Create: `Assets/ClickDungeon/Presentation/PresentationIntent.cs`
- Create: `Assets/ClickDungeon/Presentation/GameplayEventProjector.cs`
- Create: `Assets/ClickDungeon/UI/MainMenu/MainMenuLayoutContract.cs`
- Create: `Assets/ClickDungeon/UI/HeroSelect/HeroSelectionContract.cs`
- Create: `scripts/tests/test_presentation_ui_contracts.py`
- Create: `scripts/validate_presentation_ui_contracts.py`

**Interfaces:**
- Produces: pure animation/audio/VFX/UI intents and responsive layout contracts derived from the menu/gameplay art.

- [ ] Write failing contracts for menu controls, 5×5 board/HUD/action row, `ClickDungeon` branding, responsive safe-area contract, and forbidden Presentation reward/HP authority.
- [ ] Verify RED.
- [ ] Implement minimal presentation and screen contracts.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct gameplay and menu presentation contracts`.

## Task 10: Rebuild Sir Clickington Narrative and Lord Blobert Campaign Data

**Objective:** Restore deterministic narrative data without coupling story identity to combat implementation.

**Files:**
- Create: `Assets/ClickDungeon/Narrative/DialogueDefinition.cs`
- Create: `Assets/ClickDungeon/Narrative/CampaignDefinition.cs`
- Create: `Assets/ClickDungeon/Narrative/CampaignState.cs`
- Create: `Assets/ClickDungeon/Narrative/NarrativeResolver.cs`
- Create: `Assets/ClickDungeon/Narrative/CanonicalSirClickingtonCampaign.cs`
- Create: `scripts/tests/test_narrative_contracts.py`
- Create: `scripts/validate_narrative_contracts.py`

**Interfaces:**
- Produces: deterministic campaign events and dialogue results; no save/combat/reward authority.

- [ ] Write failing contracts for Clickington expression states, distinct Lord Blobert campaign ID, and generic trigger resolution.
- [ ] Verify RED.
- [ ] Implement immutable definitions/runtime campaign state and resolver.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct Sir Clickington campaign contracts`.

## Task 11: Rebuild Production Art Manifest and Strict Readiness Gate

**Objective:** Restore the canonical runtime-art inventory without pretending composite reference sheets are runtime sprites.

**Files:**
- Create: `Assets/ClickDungeon/Art/Runtime/{Tiles,Heroes,Monsters,Items,UI,VFX,Chest}/.gitkeep`
- Create: `docs/art/guid-preservation-policy.md`
- Create: `docs/art/production-art-readiness.md`
- Create: `art/production-art-manifest.json`
- Create: `scripts/build_production_art_manifest.py`
- Create: `scripts/validate_production_art.py`
- Create: `scripts/tests/test_production_art_contracts.py`

**Interfaces:**
- Produces: deterministic asset inventory and strict release gate.

- [ ] Write failing contracts for canonical manifest schema, duplicate asset/path rejection, runtime folder policy, and strict missing-file failure.
- [ ] Verify RED.
- [ ] Build manifest from canonical content definitions plus explicit UI/chest contracts.
- [ ] Require matching Unity `.meta` files in strict mode; default source mode validates manifest consistency only.
- [ ] Verify focused + full suite GREEN; strict mode is expected to fail until isolated runtime art exists.
- [ ] Commit `feat: reconstruct production art contract`.

## Task 12: Rebuild Animation / Chest Rewards / VFX Boundary

**Objective:** Re-establish the exact lost phase: deterministic exactly-once chest reward commitment plus presentation-only animation/audio/VFX sequencing.

**Files:**
- Create/Modify: `Assets/ClickDungeon/Application/Gameplay/GameplaySession.cs`
- Create: `Assets/ClickDungeon/Presentation/CanonicalPresentationCues.cs`
- Create: `Assets/ClickDungeon/Presentation/Chest/ChestPresentationPhase.cs`
- Create: `Assets/ClickDungeon/Presentation/Chest/ChestPresentationSequence.cs`
- Create: `scripts/tests/test_animation_reward_contracts.py`
- Create: `scripts/validate_animation_reward_contracts.py`
- Create: `docs/validation/animation-reward-vfx-unity-blocked.md`

**Interfaces:**
- Consumes: deterministic reward ledger and gameplay events.
- Produces: immutable reward event plus presentation sequence `Closed → InteractionBegins → Opening → LightRewardEffect → ItemReveal → RewardPresentation → ItemCollection → Complete`.

- [ ] Write failing contracts for deterministic chest transaction IDs, exactly-once grant, immutable reward event, the eight technical phases, four reference visual beats (`Anticipation`, `Burst/Reveal`, `Too Much To Handle`, `Triumph`), input-lock window, canonical cue constants, and forbidden Presentation grant authority.
- [ ] Verify RED.
- [ ] Implement chest commit before presentation and emit immutable reward event.
- [ ] Implement presentation sequence and canonical audio/VFX cue catalog.
- [ ] Run adversarial mutation 1: inject forbidden Presentation reward-grant token; validator must reject; restore exact bytes.
- [ ] Run adversarial mutation 2: inject nondeterministic chest-ID entropy; validator must reject; restore exact bytes.
- [ ] Verify focused + full suite GREEN.
- [ ] Commit `feat: reconstruct animation chest reward and vfx boundary`.

## Task 13: Recovery Parity Gate and Durable Checkpoint

**Objective:** Prove the reconstructed branch covers every previously completed subsystem before new development begins.

**Files:**
- Create: `docs/validation/recovery-parity-report.md`
- Modify: `.github/workflows/source-validation.yml`
- Modify: `.superpowers/sdd/reconstruct-through-animation-vfx/progress.md`

**Interfaces:**
- Produces: a durable, reviewable checkpoint suitable for push/PR.

- [ ] Run every validator and every source-contract test in a single command.
- [ ] Run `git diff --check` and forbidden-pattern scans for `ClickDungeon2` runtime drift, Presentation gameplay authority, mutable definition state, unknown-ID fallback, and nondeterministic entropy.
- [ ] Record actual test count and pass/fail evidence; do not claim the historical 204 count unless freshly reproduced.
- [ ] Record Unity validations as BLOCKED, not passed.
- [ ] Commit `test: prove reconstructed ClickDungeon parity boundary`.
- [ ] Stop before push/PR because external publication is a separate side effect unless already explicitly authorized at that point.

---

## Completion Rule

This recovery plan is complete only when Tasks 0–13 are committed, the complete source-contract suite is freshly green on the exact recovery head, the branch is clean, the parity report maps every pre-loss subsystem to fresh evidence, and the Animation / Chest Rewards / VFX boundary is restored. Only then may the next plan begin Save/Progression and platform work.
