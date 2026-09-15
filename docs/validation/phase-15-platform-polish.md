# Phase 15 Platform Polish Validation

**Branch:** `new`  
**Validated implementation head:** `50200ff49fc7d9672fd232d237140a0c04b59fc8`  
**Fresh Source Validation run:** `35007078904`  
**Date:** 2026-09-15

## Scope

Phase 15 adds platform capability contracts, shared Windows/touch input translation, mobile lifecycle requests and lifecycle autosave routing, responsive safe-area/menu policies, and canonical Windows/Android/iOS build-target contracts. The approved ClickDungeon reference art remains the primary player-facing source of truth. Runtime branding remains `ClickDungeon`; legacy `ClickDungeon2` text visible in some source reference sheets is non-authoritative.

The lifecycle implementation uses the approved Approach A dependency direction: `Platform -> Application -> Save`. `LifecyclePersistenceCoordinator` therefore remains in `Assets/ClickDungeon/Platform/Lifecycle/` and calls the existing Application persistence boundary instead of introducing an Application-to-Platform assembly cycle.

## Fresh regression evidence

Source Validation run `35007078904` checked out exact SHA `50200ff49fc7d9672fd232d237140a0c04b59fc8` and completed successfully.

- `python3 -m pytest -q scripts/tests`: **130 passed**.
- Foundation contracts: **PASS**.
- Tile registry contracts: **PASS**.
- Dungeon contracts: **PASS**.
- Combat contracts: **PASS**.
- Hero contracts: **PASS**.
- Enemy contracts: **PASS**.
- Item/reward contracts: **PASS**.
- Gameplay session contracts: **PASS**.
- Presentation/UI contracts: **PASS**.
- Narrative contracts: **PASS**.
- Production art validation: **PASS (source, 200 assets)**.
- Animation/reward contracts: **PASS**.
- Save progression contracts: **PASS**.
- Recovery parity: **PASS**.
- Platform contracts: **PASS** — capabilities, input, lifecycle, responsive UI; no gameplay authority or nondeterministic RNG entropy.
- Platform build contracts: **PASS** — Windows x64; Android ARM64 + IL2CPP + APK/AAB; iOS ARM64 + IL2CPP + Xcode export; external signing required.
- `git diff --check`: **PASS** as part of the successful workflow.

## Adversarial guard evidence

The Phase 15 adversarial tests operate only on temporary copies selected through `CLICKDUNGEON_VALIDATION_ROOT`; the tracked source tree is never mutated by the test. Temporary directories are removed automatically after each test.

1. A temporary `ApplyDamage` mutation in the Windows input adapter must be rejected by `validate_platform_contracts.py` with the input-layer damage-authority guard.
2. A temporary `UnityEngine.Random` mutation in the mobile lifecycle adapter must be rejected by `validate_platform_contracts.py` with the nondeterministic-entropy guard.
3. A deliberately fake, test-only signing-password literal in a temporary build-contract copy must be rejected by `validate_platform_build_contracts.py` as an embedded signing/auth secret.

Before isolated-root support was implemented, Source Validation run `35006955333` provided the expected RED state: **3 adversarial tests failed while all 127 pre-existing tests passed**. After isolated-root support, all three adversarial rejection tests pass in run `35007078904`, bringing the suite to **130/130**.

No real signing credential, password, token, keystore secret, certificate, provisioning profile, or store credential was introduced or committed.

## Confirmed platform contracts

Windows exposes mouse/keyboard capability and desktop Quit; Android exposes touch, system Back, and safe-area requirements; iOS exposes touch and safe-area requirements without Android-style system Back. Desktop and touch adapters translate into the shared Application `PlayerCommand` vocabulary and do not resolve combat, rewards, HP mutation, or RNG.

Pause/background map to a stable lifecycle autosave checkpoint through the existing `GameSessionPersistenceOrchestrator`. Resume is gameplay-neutral. Android system Back maps to pause/cancel navigation rather than direct application termination.

Responsive layout keeps the reference gameplay invariants: **5 x 5 board**, readable HUD, and action row. Canonical profiles cover desktop 16:9, desktop 16:10, tablet 4:3, phone 19.5:9, and phone 20:9. Constrained/mobile profiles may collapse secondary panels and respect safe areas without changing simulation board dimensions. Main-menu access preserves Play, Continue, Hero Select, Inventory, Talents, Shop, and Settings; direct Quit is exposed only when `SupportsDesktopQuit` is true.

Canonical release targets are Windows x64, Android ARM64/IL2CPP with APK and AAB artifacts, and iOS ARM64/IL2CPP with Xcode export. Release signing material is explicitly external configuration.

## Explicitly BLOCKED / not represented as PASS

The current source-validation environment does **not** establish any of the following, so none is claimed as PASS:

- **BLOCKED:** Unity editor compilation.
- **BLOCKED:** Unity EditMode tests.
- **BLOCKED:** Unity PlayMode tests.
- **BLOCKED:** Windows production player build or runtime smoke.
- **BLOCKED:** Android APK/AAB Unity build, installation, or device smoke.
- **BLOCKED:** iOS Unity/Xcode export, Xcode build, installation, or device smoke.
- **BLOCKED:** Windows code signing, Android keystore signing, iOS certificate/provisioning signing, or store notarization/submission.
- **BLOCKED:** physical-device safe-area/input verification on representative Android/iOS hardware.

These items require the corresponding Unity/build/signing/device environment and external credentials. They must be verified independently before any release claim that depends on them.

## Phase 15 source-level conclusion

At validated implementation SHA `50200ff49fc7d9672fd232d237140a0c04b59fc8`, Phase 15 is **source-contract clean** and its automated adversarial guards are proven. This conclusion is intentionally narrower than a Unity/runtime/release certification and does not upgrade any BLOCKED item above to PASS.
