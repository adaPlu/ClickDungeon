# ClickDungeon Phase 15 Platform Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add platform-neutral Windows/Android/iOS application contracts for capabilities, unified input, mobile lifecycle persistence requests, responsive safe-area UI behavior, and build-target requirements without moving gameplay authority out of the deterministic core.

**Architecture:** Platform adapters translate device-specific concerns into existing application commands and persistence/navigation requests. Platform code owns capability reporting and input/lifecycle normalization only; simulation, rewards, progression, deterministic RNG, and canonical saves remain authoritative in their current layers. Unity/device/build verification stays explicitly blocked until a Unity-capable environment is available.

**Tech Stack:** Unity 6 source layout, C#, Python 3.12 source-contract tests/validators, pytest, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-15-platform-polish-design.md`

## Global Constraints

- Work only on branch `new`; branch `OLD` is out of scope and must not be modified.
- Player-facing/runtime title remains `ClickDungeon`; runtime `ClickDungeon2` branding is forbidden.
- Visual hierarchy and responsive intent derive from the approved ClickDungeon title/gameplay references.
- Platform code may not mutate HP, grant rewards, own reward transaction IDs, or introduce gameplay RNG entropy.
- Windows x64, Android ARM64 + IL2CPP + APK/AAB, and iOS IL2CPP + Xcode export are the release-target contracts.
- Android/iOS signing credentials remain external secure configuration and are never fabricated or committed.
- New behavior follows RED → GREEN → full regression verification.
- Existing source-contract tests and validators remain mandatory after every task.
- Unity compile/EditMode/PlayMode/device/build/signing claims remain BLOCKED in this environment.

---

### Task 15.1: Platform Foundation and Capability Profiles

**Files:**
- Create: `Assets/ClickDungeon/Platform/ClickDungeon.Platform.asmdef`
- Create: `Assets/ClickDungeon/Platform/RuntimePlatformId.cs`
- Create: `Assets/ClickDungeon/Platform/PlatformCapabilities.cs`
- Create: `Assets/ClickDungeon/Platform/IPlatformService.cs`
- Create: `Assets/ClickDungeon/Platform/CanonicalPlatformProfiles.cs`
- Create: `scripts/tests/test_platform_contracts.py`
- Create: `scripts/validate_platform_contracts.py`

**Interfaces:**
- Produces: `RuntimePlatformId`, immutable `PlatformCapabilities`, `IPlatformService`, and canonical Windows/Android/iOS capability profiles.
- Consumes: no gameplay-layer implementation details.

- [ ] **Step 1: Write the failing platform source-contract tests**

Create `scripts/tests/test_platform_contracts.py` with checks that require the five C# files above, require enum values `Unknown`, `Windows`, `Android`, `IOS`, and require capability fields `HasTouch`, `HasMouseKeyboard`, `HasSystemBack`, `RequiresSafeArea`, and `SupportsDesktopQuit`.

The test must also reject authority tokens inside `Assets/ClickDungeon/Platform/`, including calls or members matching `GrantReward`, `ApplyDamage`, `CurrentHp =`, `UnityEngine.Random`, or `System.Random`.

- [ ] **Step 2: Run focused RED**

Run:

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
```

Expected: FAIL because the platform source files and validator do not yet exist.

- [ ] **Step 3: Implement the minimal platform types**

`RuntimePlatformId.cs`:

```csharp
namespace ClickDungeon.Platform
{
    public enum RuntimePlatformId
    {
        Unknown = 0,
        Windows = 1,
        Android = 2,
        IOS = 3
    }
}
```

`PlatformCapabilities.cs`:

```csharp
namespace ClickDungeon.Platform
{
    public sealed class PlatformCapabilities
    {
        public bool HasTouch { get; }
        public bool HasMouseKeyboard { get; }
        public bool HasSystemBack { get; }
        public bool RequiresSafeArea { get; }
        public bool SupportsDesktopQuit { get; }

        public PlatformCapabilities(
            bool hasTouch,
            bool hasMouseKeyboard,
            bool hasSystemBack,
            bool requiresSafeArea,
            bool supportsDesktopQuit)
        {
            HasTouch = hasTouch;
            HasMouseKeyboard = hasMouseKeyboard;
            HasSystemBack = hasSystemBack;
            RequiresSafeArea = requiresSafeArea;
            SupportsDesktopQuit = supportsDesktopQuit;
        }
    }
}
```

`IPlatformService.cs` exposes read-only identity and capabilities:

```csharp
namespace ClickDungeon.Platform
{
    public interface IPlatformService
    {
        RuntimePlatformId PlatformId { get; }
        PlatformCapabilities Capabilities { get; }
    }
}
```

`CanonicalPlatformProfiles.cs` must map Windows to mouse/keyboard + desktop quit, Android to touch + system back + safe area, and iOS to touch + safe area without system back.

- [ ] **Step 4: Implement `scripts/validate_platform_contracts.py`**

Validate required files/enums/fields/profile values and scan `Assets/ClickDungeon/Platform` for forbidden gameplay-authority and nondeterministic entropy tokens. Exit nonzero with a precise message on any violation.

- [ ] **Step 5: Run GREEN and regression**

Run:

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
python3 scripts/validate_platform_contracts.py
python3 -m pytest -q scripts/tests
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/Platform scripts/tests/test_platform_contracts.py scripts/validate_platform_contracts.py
git commit -m "feat: add platform capability contracts"
```

---

### Task 15.2: Unified Windows and Touch Input Translation

**Files:**
- Create: `Assets/ClickDungeon/Platform/Input/PlatformInputAction.cs`
- Create: `Assets/ClickDungeon/Platform/Input/PlatformInputEvent.cs`
- Create: `Assets/ClickDungeon/Platform/Input/IPlayerInputAdapter.cs`
- Create: `Assets/ClickDungeon/Platform/Input/WindowsInputAdapter.cs`
- Create: `Assets/ClickDungeon/Platform/Input/TouchInputAdapter.cs`
- Modify: `scripts/tests/test_platform_contracts.py`
- Modify: `scripts/validate_platform_contracts.py`

**Interfaces:**
- Consumes: existing `ClickDungeon.Application.Gameplay.PlayerCommand`.
- Produces: platform events translated to `PlayerCommand` only; adapters never resolve gameplay.

- [ ] **Step 1: Extend tests RED**

Require a single input-adapter method returning `PlayerCommand` (or a nullable/try-result wrapper around that exact application type). Require Windows mappings for directional movement, action activation, interaction, inventory, and pause/cancel. Require touch mappings for board selection, action activation, interaction, inventory, and pause/cancel.

Reject gameplay-resolution tokens such as `CombatResolver`, `RewardGrantService`, `ApplyDamage`, or direct HP mutation inside the input-adapter files.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
```

Expected: FAIL on missing input adapter contracts.

- [ ] **Step 3: Implement minimal event vocabulary and adapters**

Define `PlatformInputAction` values sufficient to represent `MoveUp`, `MoveDown`, `MoveLeft`, `MoveRight`, `SelectCell`, `ActivateAction`, `Interact`, `OpenInventory`, and `PauseOrCancel`.

`PlatformInputEvent` stores the action plus primitive integer cell coordinates/action-slot values as needed.

`IPlayerInputAdapter` exposes a pure translation method from `PlatformInputEvent` to the existing `PlayerCommand` representation. Windows and touch implementations differ only in accepted source-event semantics; neither owns gameplay state.

- [ ] **Step 4: Extend validator**

Validate both adapters reference `PlayerCommand`, prohibit gameplay resolver/service authority, and prohibit per-platform command vocabularies that bypass the shared application command layer.

- [ ] **Step 5: Run GREEN and regression**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
python3 scripts/validate_platform_contracts.py
python3 -m pytest -q scripts/tests
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/Platform/Input scripts/tests/test_platform_contracts.py scripts/validate_platform_contracts.py
git commit -m "feat: unify desktop and touch input translation"
```

---

### Task 15.3: Mobile Lifecycle, Android Back, and Persistence Requests

**Files:**
- Create: `Assets/ClickDungeon/Platform/Lifecycle/AppLifecycleEvent.cs`
- Create: `Assets/ClickDungeon/Platform/Lifecycle/PlatformLifecycleRequest.cs`
- Create: `Assets/ClickDungeon/Platform/Lifecycle/MobileLifecycleAdapter.cs`
- Create: `Assets/ClickDungeon/Application/Persistence/LifecyclePersistenceCoordinator.cs`
- Modify: `scripts/tests/test_platform_contracts.py`
- Modify: `scripts/validate_platform_contracts.py`

**Interfaces:**
- Consumes: existing save/autosave orchestration boundary from Phase 14.
- Produces: application-facing checkpoint/navigation requests for pause/background/back; no direct persistence serialization and no gameplay mutation.

- [ ] **Step 1: Extend tests RED**

Require lifecycle events `Paused`, `Backgrounded`, `Resumed`, and `SystemBack`. Require pause/background to request a stable autosave checkpoint through the Application persistence layer. Require resume to perform no gameplay mutation. Require Android system-back to route through navigation/pause/cancel handling rather than direct application termination.

Reject reward-transaction mutation, direct save DTO editing, direct HP mutation, and `Application.Quit` from the mobile lifecycle adapter.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
```

Expected: FAIL on missing lifecycle contracts.

- [ ] **Step 3: Implement lifecycle request data and coordinator**

`MobileLifecycleAdapter` maps platform lifecycle events to immutable `PlatformLifecycleRequest` values. `LifecyclePersistenceCoordinator` consumes those requests and calls the existing Phase-14 persistence/autosave boundary for pause/background checkpoints. `Resumed` is a no-op with respect to gameplay state. `SystemBack` produces an application navigation/pause/cancel request.

- [ ] **Step 4: Extend validator**

Require the adapter to depend on application-facing request types rather than save DTO internals. Reject platform-layer references to reward-ledger mutation, combat resolver mutation, or direct process termination for Android back.

- [ ] **Step 5: Run GREEN and regression**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
python3 scripts/validate_platform_contracts.py
python3 -m pytest -q scripts/tests
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/Platform/Lifecycle Assets/ClickDungeon/Application/Persistence/LifecyclePersistenceCoordinator.cs scripts/tests/test_platform_contracts.py scripts/validate_platform_contracts.py
git commit -m "feat: add mobile lifecycle persistence requests"
```

---

### Task 15.4: Responsive Safe-Area and Platform Navigation Contracts

**Files:**
- Create: `Assets/ClickDungeon/UI/Layout/SafeAreaInsets.cs`
- Create: `Assets/ClickDungeon/UI/Layout/ViewportProfile.cs`
- Create: `Assets/ClickDungeon/UI/Layout/ResponsiveLayoutContract.cs`
- Create: `Assets/ClickDungeon/UI/Layout/CanonicalViewportProfiles.cs`
- Create: `Assets/ClickDungeon/UI/MainMenu/PlatformMenuPolicy.cs`
- Modify: `scripts/tests/test_platform_contracts.py`
- Modify: `scripts/validate_platform_contracts.py`

**Interfaces:**
- Consumes: platform capabilities and existing main-menu/gameplay presentation contracts.
- Produces: pure layout/navigation policy data; no simulation-coordinate changes.

- [ ] **Step 1: Extend tests RED**

Require primitive safe-area insets, viewport profiles for `Desktop16x9`, `Desktop16x10`, `Tablet4x3`, `Phone19_5x9`, and `Phone20x9`, and layout rules preserving a readable 5×5 board with non-overlapping HUD/action row.

Require main-menu policy to show true Quit on Windows and hide/reinterpret Quit on Android/iOS while preserving Play/Continue/Hero Select/Inventory/Talents/Shop/Settings access.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
```

Expected: FAIL on missing responsive layout contracts.

- [ ] **Step 3: Implement responsive data contracts**

`SafeAreaInsets` stores `Top`, `Right`, `Bottom`, `Left` as primitive numeric values. `ViewportProfile` identifies the canonical aspect profile and constrained/mobile state. `ResponsiveLayoutContract` represents board/HUD/action-row constraints and secondary-panel collapse policy without Unity scene references.

`PlatformMenuPolicy` must make desktop Quit explicit and mobile Quit unavailable as a direct process-exit command.

- [ ] **Step 4: Extend validator**

Validate all five viewport profiles, safe-area fields, board/HUD/action constraints, and platform Quit policy. Reject any contract that changes board dimensions away from 5×5 based on viewport.

- [ ] **Step 5: Run GREEN and regression**

```bash
python3 -m pytest -q scripts/tests/test_platform_contracts.py
python3 scripts/validate_platform_contracts.py
python3 -m pytest -q scripts/tests
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add Assets/ClickDungeon/UI/Layout Assets/ClickDungeon/UI/MainMenu/PlatformMenuPolicy.cs scripts/tests/test_platform_contracts.py scripts/validate_platform_contracts.py
git commit -m "feat: add responsive platform layout contracts"
```

---

### Task 15.5: Build-Target Configuration Contracts

**Files:**
- Create: `Assets/ClickDungeon/Platform/Build/BuildTargetContract.cs`
- Create: `Assets/ClickDungeon/Platform/Build/CanonicalBuildTargets.cs`
- Create: `scripts/tests/test_platform_build_contracts.py`
- Create: `scripts/validate_platform_build_contracts.py`
- Modify: `.github/workflows/source-validation.yml`

**Interfaces:**
- Produces: source-level release target requirements consumed by CI validation and later Unity-capable Phase-16 workflows.

- [ ] **Step 1: Write failing build-contract tests**

Require exact semantic requirements:

- Windows: x64.
- Android: ARM64, IL2CPP, APK, AAB.
- iOS: IL2CPP, Xcode export.
- Signing material must be marked external/required-at-release rather than embedded.

The test must reject committed keystore/provisioning secret literals or password/token fields with values.

- [ ] **Step 2: Run focused RED**

```bash
python3 -m pytest -q scripts/tests/test_platform_build_contracts.py
```

Expected: FAIL on missing build-target contracts.

- [ ] **Step 3: Implement immutable build-target contracts**

`BuildTargetContract` stores platform ID, architecture, scripting backend, required artifact kinds, and a boolean/policy indicating secure external signing. `CanonicalBuildTargets` exposes one canonical contract per supported platform.

- [ ] **Step 4: Implement build validator**

`validate_platform_build_contracts.py` validates the exact Windows/Android/iOS requirements and scans relevant configuration/source paths for forbidden embedded signing secrets.

- [ ] **Step 5: Add both Phase-15 validators to CI**

Modify `.github/workflows/source-validation.yml` so the validation step runs:

```bash
python3 scripts/validate_platform_contracts.py
python3 scripts/validate_platform_build_contracts.py
```

The existing `python3 -m pip install --upgrade pip pytest` dependency step must remain.

- [ ] **Step 6: Run GREEN and full source gate**

```bash
python3 -m pytest -q scripts/tests/test_platform_build_contracts.py
python3 scripts/validate_platform_build_contracts.py
python3 -m pytest -q scripts/tests
python3 scripts/validate_foundation_contracts.py
python3 scripts/validate_tile_registry.py
python3 scripts/validate_dungeon_contracts.py
python3 scripts/validate_combat_contracts.py
python3 scripts/validate_hero_contracts.py
python3 scripts/validate_enemy_contracts.py
python3 scripts/validate_item_reward_contracts.py
python3 scripts/validate_gameplay_session_contracts.py
python3 scripts/validate_presentation_ui_contracts.py
python3 scripts/validate_narrative_contracts.py
python3 scripts/validate_production_art.py
python3 scripts/validate_animation_reward_contracts.py
python3 scripts/validate_save_progression_contracts.py
python3 scripts/validate_recovery_parity.py
python3 scripts/validate_platform_contracts.py
python3 scripts/validate_platform_build_contracts.py
git diff --check
```

Expected: all source tests/validators PASS; Unity/device/build execution remains unclaimed.

- [ ] **Step 7: Commit**

```bash
git add Assets/ClickDungeon/Platform/Build scripts/tests/test_platform_build_contracts.py scripts/validate_platform_build_contracts.py .github/workflows/source-validation.yml
git commit -m "feat: add platform build target contracts"
```

---

### Task 15.6: Adversarial Phase Gate and Validation Report

**Files:**
- Create: `docs/validation/phase-15-platform-polish.md`
- Modify: `scripts/validate_platform_contracts.py` only if an adversarial test exposes a missing guard.
- Modify: `scripts/validate_platform_build_contracts.py` only if an adversarial test exposes a missing guard.

**Interfaces:**
- Produces: final Phase-15 evidence and exact BLOCKED list for Phase 16.

- [ ] **Step 1: Run authority adversarial mutation**

Temporarily inject a forbidden platform-layer reward or HP authority token into a platform source file, run `python3 scripts/validate_platform_contracts.py`, confirm nonzero rejection for the intended reason, then restore the exact original bytes.

- [ ] **Step 2: Run entropy adversarial mutation**

Temporarily inject `UnityEngine.Random` or uncontrolled `System.Random` into a platform input/lifecycle file, run the platform validator, confirm rejection, then restore exact original bytes.

- [ ] **Step 3: Run signing-secret adversarial mutation**

Temporarily inject a fake non-empty signing password/keystore-secret literal into the build contract/config test surface, run `python3 scripts/validate_platform_build_contracts.py`, confirm rejection, then restore exact original bytes. Do not use a real credential.

- [ ] **Step 4: Run complete fresh regression**

Run the exact full command block from Task 15.5 Step 6 and record the actual pytest count and validator results from this run.

- [ ] **Step 5: Write validation report**

Create `docs/validation/phase-15-platform-polish.md` containing:

- exact branch and commit SHA;
- actual fresh pytest result count;
- each validator result;
- adversarial checks and expected rejection evidence;
- confirmed platform requirements;
- explicit `BLOCKED` entries for Unity compile, EditMode, PlayMode, Windows runtime/build, Android APK/AAB/device test, iOS Xcode export/device test, and signing;
- statement that no blocked item is represented as PASS.

- [ ] **Step 6: Commit final Phase-15 evidence**

```bash
git add docs/validation/phase-15-platform-polish.md scripts/validate_platform_contracts.py scripts/validate_platform_build_contracts.py
git commit -m "test: prove phase 15 platform polish boundary"
```

- [ ] **Step 7: Verify repository state and CI**

Require `git diff --check`, a clean worktree, and a successful GitHub Source Validation run on the exact final Phase-15 head before declaring Phase 15 complete.

---

## Completion Rule

Phase 15 is complete only when Tasks 15.1–15.6 are committed on `new`, the exact final head has a fresh green complete source-contract suite and validators, the Source Validation workflow is green, adversarial authority/entropy/signing guards have been demonstrated, and all Unity/device/build/signing items remain accurately marked BLOCKED. The next work item is Phase 16: Unity-capable CI/build/runtime validation, artifact inspection, release audit, `/Gaudit`, and `/graphRepair` if evidence requires it.