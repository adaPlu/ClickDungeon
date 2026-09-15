# Phase 16 Release Validation and Final Audit Design

**Status:** Approved in chat 2026-09-15; written for repository review before implementation planning  
**Repository:** `adaPlu/ClickDungeon`  
**Working branch:** `new`  
**Out of scope:** `OLD`  
**Player-facing product:** `ClickDungeon`  
**Targets:** Windows x64, Android ARM64/IL2CPP APK+AAB, iOS ARM64/IL2CPP Xcode export

## 1. Purpose

Phase 16 converts the source-contract-clean ClickDungeon reconstruction into an evidence-backed Unity/release candidate. Phase 15 deliberately stopped at platform-neutral source contracts because Unity compilation, EditMode/PlayMode execution, production builds, runtime smoke, device validation, and signing were not proven. Phase 16 closes as many of those blocked gates as the available Unity/build environment can genuinely prove, without fabricating editor metadata, signing material, device evidence, or release claims.

This phase is validation-first. It does not redesign gameplay, rebalance content, replace the canonical art direction, or add unrelated features. Any code or configuration change must be justified by a failing release/validation gate.

## 2. Baseline and Branch Topology

`new` is the only implementation branch for this phase and begins from the Phase-15 merge state on `main`. `OLD` must not be modified, merged, rebased, or used as an implementation source.

Phase 16 work remains on `new` until all required evidence is green. Promotion is `new -> main` through a reviewed pull request after the final release audit. No direct feature commits are pushed to `main` during the phase.

## 3. Primary Sources of Truth

The project has three authority layers:

1. **Gameplay/system authority:** `docs/specs/clickdungeon-master-design.md` and committed canonical definitions/tests.
2. **Visual authority:** the pinned reference bundle in `docs/reference/latest/`, with the title/main-menu, core gameplay, tile, hero, monster/boss, inventory/equipment, and reward-sequence sheets treated as the primary player-facing reference for layout, hierarchy, tone, proportions, identity, and presentation.
3. **Release evidence authority:** fresh CI/build/test/runtime logs and inspected build artifacts from the exact Phase-16 head SHA.

A passing source test cannot override a visible mismatch with canonical art where the requirement is presentation. Likewise, artwork cannot override deterministic simulation, save, reward, combat, or progression contracts.

Legacy `ClickDungeon2` text appearing inside historical/reference artwork is non-authoritative for runtime branding. Shipping/player-facing runtime text remains `ClickDungeon`.

## 4. Authentic Unity Project Requirement

Phase 16 must not hand-author or copy guessed Unity project metadata merely to make CI pass. If required Unity files such as `ProjectSettings/ProjectVersion.txt`, package manifests, or lockfiles are absent, they must be established through a real Unity-capable environment using a supported Unity 6 editor and then committed exactly as generated/reconciled.

The exact Unity editor version used becomes repository authority through `ProjectSettings/ProjectVersion.txt`. Package state must be deterministic and committed where Unity conventions require it. Any first-import mutation must be inspected, classified, and either committed as deterministic project state or rejected as unexplained mutation.

## 5. CI Architecture

Phase 16 extends the existing source-validation workflow rather than replacing it. Source validation remains the first gate and must continue to install and run pytest plus all existing validators.

Unity-capable CI is separated into explicit stages so failures have a narrow boundary:

- source contracts and static validators;
- Unity project import/compile;
- Unity EditMode tests;
- Unity PlayMode tests where the project supports them;
- post-import mutation guard;
- Windows production build;
- Windows artifact inspection;
- Windows built-player runtime smoke;
- Android APK build;
- Android AAB build;
- Android artifact inspection;
- iOS Xcode export;
- iOS export artifact inspection;
- final mutation guard.

A later stage must not be marked passed when an earlier required stage fails. Build/signing/device gates must be labeled independently so an unsigned build/export can be validated without implying store-signing readiness.

## 6. Runtime Smoke Contract

The minimum built-player smoke flow is:

**Boot -> Main Menu -> Start Game -> Dungeon Ready**

The smoke must prove that the application reaches the title/main menu, can start the canonical gameplay flow, and reaches a usable 5x5 dungeon state. Where reliable automation already exists, one basic interaction should also be exercised without weakening the core smoke requirement.

Runtime readiness is determined from explicit machine-readable or uniquely parseable markers in player logs, not screenshot inference alone. A build that launches but never reaches Dungeon Ready is a failure.

## 7. Visual Acceptance Contract

Visual validation compares the implemented runtime against the canonical reference bundle. It is not pixel-perfect screenshot matching; it is structured presentation acceptance.

The title/main-menu gate requires the reference hierarchy: ClickDungeon branding, Sir Clickington identity/showcase, Continue/Play prominence, hero/progression/currency information, Daily Reward, and routes for Hero Select, Inventory, Talents, Shop, Settings, plus platform-appropriate Quit behavior.

The gameplay gate requires a readable 5x5 board, clear hero/enemy/tile identities, top HUD hierarchy, floor identity, selected-cell readability, bottom gameplay actions, and visible separation of terrain/structure/content/actor/state layers. Stone-dungeon presentation, warm torch lighting intent, and the canonical tile vocabulary remain the launch visual baseline.

Hero/enemy/reward gates verify that identity art is not silently substituted with unrelated fallback content and that chest/reward presentation follows the approved anticipation -> reveal/burst -> reaction -> triumph intent while reward authority remains simulation/application owned.

Responsive validation checks desktop and mobile hierarchy preservation rather than forcing one fixed composition across all aspect ratios.

## 8. Artifact Inspection

Every generated artifact must be inspected for the properties relevant to its target.

Windows validation includes executable/data presence, expected product naming, architecture expectations, and absence of obviously misplaced development-only artifacts when the build mode is production.

Android validation includes APK and AAB presence, package/build metadata as available, ARM64/IL2CPP target intent, and no embedded signing secrets. Unsigned or CI-signed test artifacts must be labeled accurately.

iOS validation includes a structurally valid Xcode export with expected Unity-generated project/workspace content and target configuration. Export success is not equivalent to successful Xcode archive, device installation, App Store signing, or notarization.

Artifact checks must operate on the exact outputs generated by the workflow run for the exact commit under review.

## 9. Mutation Guards

Unity import and build steps may not leave unexplained tracked-file changes.

The gate records repository state before Unity import, after import/tests, after each build class where appropriate, and at workflow completion. Known deterministic project files may be updated only when the change is understood and intentionally committed. Temporary environmental mutations may be restored only when they are explicitly allowlisted and their pre/post bytes are proven. Any new unexplained tracked mutation fails the phase.

Generated build output directories that are intentionally ignored do not count as source mutation.

## 10. Signing and Secrets

No Android keystore, keystore password, iOS certificate, provisioning profile, App Store credential, Windows code-signing certificate, or provider secret is invented or committed.

CI may prove unsigned builds/exports and release-target configuration. Signing becomes PASS only when an authorized external credential path is available and the corresponding signed artifact has been inspected. Otherwise the specific signing gate remains BLOCKED, not failed and not passed.

## 11. Repair Policy

Phase 16 uses evidence-driven repair only.

When a gate fails:

1. capture the exact failing evidence;
2. identify the narrow root cause;
3. add or strengthen a regression test/validator when practical;
4. implement the smallest repair consistent with architecture;
5. rerun the focused gate;
6. rerun the complete affected validation chain;
7. verify no new mutation or visual regression.

`/graphRepair` is used only when the evidence shows an actual dependency/integration defect or when the final audit identifies a repairable graph issue. It is not run as a speculative rewrite mechanism.

## 12. Final Audit

The final release audit examines:

- exact branch/head SHA and cleanliness;
- source-test and validator results;
- Unity compile/EditMode/PlayMode evidence;
- import/build mutation evidence;
- Windows build/runtime smoke;
- Android APK/AAB evidence;
- iOS Xcode export evidence;
- visual acceptance against canonical references;
- runtime branding;
- package/project metadata consistency;
- secrets/signing posture;
- stale documentation or contradictory release claims;
- unresolved warnings/blockers;
- PR/CI state for `new -> main`.

`/Gaudit` is the final broad audit. Any issue it identifies is classified as release blocker, non-blocking defect, accepted limitation, or external BLOCKED requirement. Release-clean means there is no unresolved blocker in code/configuration under repository control.

## 13. Documentation Reconciliation

Phase 16 updates stale repository documentation that materially misstates the current implementation or next boundary. In particular, README text must no longer claim that gameplay has not started or that Phase 0-1 is still the next execution step once the current implementation state is verified.

Validation reports must distinguish PASS, FAIL, and BLOCKED. Historical validation reports remain historical evidence and are not rewritten to pretend later gates existed earlier.

## 14. Definition of Done

Phase 16 is complete when all of the following are true for one exact `new` head SHA:

- existing source validation remains green;
- the repository contains authentic, reproducible Unity 6 project metadata required to compile the actual project;
- Unity import/compile succeeds;
- required Unity EditMode tests succeed;
- applicable PlayMode tests succeed or are explicitly justified if none exist yet;
- import/build mutation guards are clean;
- Windows x64 production build succeeds and built-player runtime smoke reaches Boot -> Main Menu -> Start Game -> Dungeon Ready;
- Android ARM64/IL2CPP APK and AAB builds succeed and artifacts pass inspection;
- iOS ARM64/IL2CPP Xcode export succeeds and export artifacts pass inspection;
- visual acceptance is checked against the pinned canonical art and no release-blocking mismatch remains;
- no release secret is fabricated or committed;
- every unavailable signing/device/store-specific requirement is explicitly BLOCKED rather than represented as PASS;
- stale release-state documentation is reconciled;
- `/Gaudit` reports no unresolved repository-controlled release blocker;
- `/graphRepair` is run only if evidence requires repair, and any resulting change is revalidated;
- a clean `new -> main` pull request has green required CI on the exact final head.

## 15. Non-Goals

This phase does not add new heroes, monsters, biomes, monetization, online services, account systems, balance passes, or unrelated UI redesign. It does not use `OLD` as a source of implementation truth. It does not relax deterministic gameplay, exactly-once rewards, canonical art IDs, platform abstraction, save authority, or source mutation guards to make CI green.
