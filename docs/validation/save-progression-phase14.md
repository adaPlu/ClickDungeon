# Phase 14 — Save / Progression Validation

Branch: `new`
Baseline: `3b6cb7fb617445b908420f98dd2e75fefebcc610`

## Implemented boundary

- Run, hero, and account/meta progression are separate pure state models.
- Hero XP leveling uses deterministic checked integer arithmetic.
- Floor completion is duplicate-safe and derives its identity from run seed + floor index.
- Depth difficulty increases threat, trap complexity, elite budget, special-interaction budget, and reward budget; it does not collapse difficulty into an enemy-HP multiplier.
- `ProfileSave`, `RunSave`, and `SettingsSave` are separate versioned DTOs.
- `RunSave` carries generation version, run seed, hero, floor/board, player resources, inventory, statuses, enemies, tile state, committed reward transaction IDs, objectives, and run progression.
- Save storage and encoding are behind interfaces; canonical DTOs contain no Unity scene objects.
- Migration registries are explicit, sequential, gap-rejecting, and future-schema rejecting.
- Application persistence orchestration requests stable autosaves after resolved turns, committed chest rewards, floor transitions, and lifecycle pause/background events.
- Run resume is exposed through validated/migrated `SaveRepository.TryLoadRun`.

## Fresh source evidence

- Focused Phase-14 contracts: `17 passed`.
- Full source suite after Phase-14 implementation: `103 passed`.
- `scripts/validate_save_progression_contracts.py`: PASS.
- Adversarial nondeterminism mutation (`Guid.NewGuid`): correctly rejected.
- Adversarial HP-only scaling mutation (`HealthMultiplier`): correctly rejected.
- `git diff --check`: clean.

## Environment boundary

No C# compiler or Unity editor is installed in this execution environment. Unity compile, EditMode, PlayMode, platform builds, and runtime save serialization are therefore **BLOCKED**, not claimed as passing. Platform-specific filesystem/codec adapters belong to Phase 15.
