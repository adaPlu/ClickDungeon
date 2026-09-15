# ClickDungeon Phase 14 — Save / Progression Implementation Plan

**Goal:** Complete master-design Phase 14 with deterministic progression and versioned, scene-independent save contracts that can preserve and resume a run without granting gameplay authority to presentation or platform code.

**Spec:** `docs/specs/clickdungeon-master-design.md` sections 14, 20, 22, 24–25.

## Constraints

- Work only on branch `new`; branch `OLD` is out of scope and must not be modified.
- Player-facing title remains `ClickDungeon`.
- Keep simulation/progression authority outside Presentation/UI.
- Canonical saves contain DTO data only; never Unity scene objects, `MonoBehaviour`, `GameObject`, or `ScriptableObject` references.
- Separate `ProfileSave`, `RunSave`, and `SettingsSave`, each with an explicit schema version.
- `RunSave` must retain generation version, seed, hero, floor/board, player resources, inventory, statuses, enemies, tile state, committed chest reward transaction IDs, and objectives.
- Progression stays split into run, hero, and account/meta state.
- Difficulty depth changes threat/traps/elites/special interactions/reward budget; no global HP-multiplier-only scaling.
- Autosave checkpoints are requested after resolved turns, reward commitment, floor transitions, and pause/background lifecycle events.
- New behavior follows RED → GREEN → full regression verification.
- Unity compile/runtime/build remains blocked in this environment and must not be claimed.

## Task 14.1 — Progression authority

Create pure progression state for run, hero, and account/meta progression; deterministic integer hero-XP leveling; duplicate-safe floor completion; and multidimensional depth difficulty budgets.

## Task 14.2 — Versioned save DTOs

Create `ClickDungeon.Save`, schema constants, `ProfileSave`, `RunSave`, `SettingsSave`, and primitive nested DTOs sufficient to round-trip the canonical run state without scene references.

## Task 14.3 — Validation and migration boundary

Create explicit save validation plus sequential migration contracts that reject downgrade, unknown future schemas, and migration gaps.

## Task 14.4 — Autosave / resume orchestration

Create save store/codec boundaries and an Application persistence orchestrator that requests stable checkpoints after the four required events while keeping reward/simulation authority in existing systems.

## Task 14.5 — Phase gate

Add source contracts and a Phase-14 validator to CI. Run focused RED/GREEN, full source regression, all validators, adversarial forbidden-authority scans, and diff hygiene. Commit only after fresh evidence.
