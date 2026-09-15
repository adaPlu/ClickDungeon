# ClickDungeon Phase 15 — Windows / Android / iOS Polish Design

**Status:** Approved in chat on 2026-09-15  
**Repository:** `adaPlu/ClickDungeon`  
**Working branch:** `new`  
**Ignored branch:** `OLD`  
**Player-facing title:** `ClickDungeon`  
**Targets:** Windows, Android, iOS  
**Engine target:** Unity 6 / C#  
**Visual source of truth:** the approved ClickDungeon reference imagery and the master design specification.

## 1. Purpose

Phase 15 converts the reconstructed deterministic game core into a platform-ready application boundary without changing gameplay authority. The implementation must add shared platform capabilities, platform-specific input translation, mobile lifecycle behavior, safe-area and aspect-ratio layout contracts, and build-target configuration contracts while keeping simulation, rewards, save data, and progression deterministic and platform-neutral.

This phase does **not** claim Unity compilation, device execution, APK/AAB generation, Xcode export, or signing success in the current environment. Those runtime/build claims remain blocked until a Unity-capable execution environment is available.

## 2. Architectural Choice

Use a **source-contract-first shared platform layer**.

The dependency path is:

`Platform adapter / input source → shared platform/input interfaces → Application command/lifecycle requests → existing deterministic gameplay/save systems`

Platform code may describe capabilities, normalize device input, report safe-area geometry, and request lifecycle-safe persistence. It may never mutate combat state, grant rewards, alter deterministic RNG inputs, or bypass the established Application and Save boundaries.

## 3. Platform Foundation

Create a platform namespace and assembly boundary dedicated to platform concerns. Required shared concepts:

- Runtime platform identity: Windows, Android, iOS, Unknown.
- Capability data: touch available, mouse/keyboard available, system-back available, safe-area required, desktop quit supported.
- Screen geometry contract expressed in primitive data only; no canonical save data may contain Unity scene objects.
- Platform service interface that reports identity/capabilities and lifecycle state but exposes no gameplay mutation methods.

Unknown platform values must fail safely at development validation boundaries rather than silently inheriting another platform's capabilities.

## 4. Unified Input Architecture

All gameplay input must converge on the existing application command layer. Windows keyboard/mouse and mobile touch are input adapters only; there is one gameplay-command vocabulary.

Required behavior:

- Windows supports directional movement, action selection/activation, interaction, inventory, pause/cancel, and pointer-based board targeting where applicable.
- Android/iOS support direct touch board selection, action buttons, inventory, pause/cancel, and platform back semantics.
- Input adapters may translate gestures/keys/buttons into `PlayerCommand`; they may not resolve movement, damage, rewards, or turn order themselves.
- Platform-specific input must remain deterministic: equivalent accepted commands entering the application layer produce equivalent gameplay results independent of originating device.

## 5. Mobile Lifecycle and Persistence

Android and iOS lifecycle handling must request persistence through the existing save orchestration boundary.

Required lifecycle checkpoints:

- Application pause/background requests a stable autosave checkpoint.
- Application resume does not mutate gameplay state by itself.
- Android system-back first routes through the application's cancel/pause/navigation contract; it must not directly terminate an active run without the application deciding the result.
- iOS has no assumed hardware/system back button.
- Lifecycle code cannot create, edit, or reinterpret reward transactions; committed chest transaction IDs remain owned by existing progression/save systems.

## 6. Responsive UI and Safe Areas

The supplied reference art is the visual source of truth for hierarchy, emphasis, visual language, and major control placement. Desktop preserves the broad composition shown in the title and gameplay references. Mobile preserves hierarchy and function while collapsing or reflowing secondary panels rather than shrinking desktop UI indiscriminately.

Required responsive contracts:

- Safe-area insets are represented as primitive top/right/bottom/left values.
- Gameplay keeps the 5×5 board readable and primary; HUD and action row must not overlap the board's interactive area.
- Title screen keeps the logo, active hero identity, Play/Continue, and required navigation reachable at narrow mobile aspect ratios.
- Secondary desktop content such as wide decorative side panels may collapse, move behind routes, or hide on constrained layouts without removing required functionality.
- Windows keeps a true Quit action; Android/iOS hide or reinterpret Quit as an appropriate navigation/back behavior.
- Common validation profiles cover at least 16:9 desktop, 16:10 desktop, 4:3/tablet-like, 19.5:9 phone, and 20:9 phone layouts.
- Safe-area handling must account for notches/cutouts/home indicators without changing simulation coordinates or board dimensions.

## 7. Build-Target Contracts

This phase defines build configuration requirements in source and validation even when Unity execution is unavailable.

Windows contract:

- x64 target.
- Mouse/keyboard support.
- Fullscreen/windowed support contract.
- Scalable UI across common desktop aspect ratios.

Android contract:

- Touch input and Android back handling.
- Safe-area and variable-aspect handling.
- ARM64 target requirement.
- IL2CPP requirement.
- APK and AAB are required release artifact types.
- Pause/background lifecycle save requests.

Android release signing credentials must remain external secure configuration; no keystore secret or password is committed or fabricated.

iOS contract:

- Touch input and safe-area handling for iPhone/iPad-like layouts.
- IL2CPP requirement.
- Xcode export is the required release build handoff.
- Pause/background lifecycle save requests.
- Signing/provisioning remains external secure configuration only.

## 8. Validation and Testing Strategy

Implementation uses RED → GREEN → full regression verification for each independently reviewable task.

Source-contract tests must prove:

- platform assemblies/interfaces exist and have no gameplay-authority methods;
- capability profiles map correctly to Windows/Android/iOS;
- input adapters only produce application commands;
- Android back and mobile lifecycle produce application/navigation/save requests rather than direct gameplay mutation;
- responsive layout rules include required aspect/safe-area profiles;
- Windows-only Quit behavior is explicit;
- build-target requirements encode Windows x64, Android ARM64/IL2CPP/APK+AAB, and iOS IL2CPP/Xcode export;
- forbidden patterns reject platform code that grants rewards, writes HP directly, owns deterministic RNG entropy, or serializes Unity scene objects into canonical saves;
- player-facing/runtime branding remains `ClickDungeon`, not `ClickDungeon2`.

The existing full source-contract and validator suite remains mandatory after every task. `git diff --check` must remain clean.

## 9. Phase Breakdown

### 15.1 Platform Foundation

Add platform assembly/contracts, runtime identity, capability profiles, and strict validation of platform boundaries.

### 15.2 Unified Input

Add Windows and touch input translation contracts that converge on the existing `PlayerCommand` layer.

### 15.3 Lifecycle / Back / Persistence Requests

Add Android/iOS lifecycle event models, Android back routing, and Application-facing autosave/navigation request contracts.

### 15.4 Responsive Presentation

Add safe-area and aspect-ratio layout contracts derived from the approved title/gameplay imagery, including platform-specific Quit behavior.

### 15.5 Build Configuration Contracts

Add explicit target configuration data/validators for Windows x64, Android ARM64 IL2CPP APK+AAB, and iOS IL2CPP Xcode export while keeping signing external.

### 15.6 Phase Gate

Run focused tests, complete source regression, all validators, adversarial authority/determinism checks, branding scans, and diff hygiene; create a validation report that distinguishes source-contract PASS from Unity/device/build BLOCKED items.

## 10. Definition of Done

Phase 15 is complete when all six sub-phases are committed on `new`, the fresh full source-contract suite and validators pass on the exact head, platform code cannot acquire gameplay/reward/RNG authority, responsive/safe-area contracts cover desktop/tablet/phone profiles, build-target requirements are encoded and validated, and the validation report clearly marks Unity compilation/device/build/signing evidence as BLOCKED rather than passed.

Only after this gate is green does Phase 16 begin: Unity-capable CI/build validation, runtime smoke, artifact inspection, release validation, `/Gaudit`, `/graphRepair` if needed, and final release audit.