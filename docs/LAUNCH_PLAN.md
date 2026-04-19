# ClickDungeon Launch Plan

**Created:** 2026-04-06
**Last updated:** 2026-04-18 (Lead review + main merge)
**Current version:** 1.0.0 (`versionCode=2`)
**Package name:** `com.adaplu.clickdungeon`
**Goal:** Ship v1.0 on Google Play

---

## Branch / Review Process (enforced from this point forward)

```
worker/feature-branch  →  PR to main  →  Lead review  →  merge to main
```

No code goes directly to main. Every worker pushes their branch, opens a PR,
and the lead must approve before merge. This document tracks which worker owns
which branch at any given time.

---

## Current State Summary (as of 2026-04-18 lead review)

### Verified in Code (main branch)
- Full dungeon loop: 5×5 grid, terrain per floor, traps, boss floors, XP/gold,
  status effects, save/continue across the 1-99 campaign
- All four classes fully playable: Knight, Ranger (wired this session),
  Thief, Wizard — each with 5 ability unlocks and targeted combat abilities
- Combat dialog with animations, monster telegraphs, and action flow
- Save system: encrypted preferences, integrity checks, force-save on
  TRIM_MEMORY_RUNNING_CRITICAL, debounced background saves
- Shop, buyback, achievements, onboarding, settings, audio diagnostics
- Release build scaffold: signing config, minification, resource shrinking
- Animated dungeon board art committed; board tiles render with images
- Unit test suite: 65+ Robolectric tests covering all P0/P1/P2 backlog items,
  plus new MonsterCatalog and TelemetryManager coverage
- Firebase: plugins wired, dependencies wired, Crashlytics initialized with
  release-only collection, try/catch guard in ClickDungeonApp for test safety
- TelemetryManager: 8 typed log methods (run_start, run_failed, run_completed,
  combat_ended, level_up, purchase, save_failed, session_start)

### Still Open Before Release

| # | Item | Owner Branch | Status |
|---|------|-------------|--------|
| 1 | Phone screenshots (min 2) + feature graphic (1024×500) | worker/gate4-store-assets | NOT STARTED |
| 2 | Privacy policy: final text + contact address + hosted URL | worker/gate4-store-assets | NOT STARTED |
| 3 | Gate Gate 3 physical-device smoke test recorded | worker/gate4-store-assets | NOT STARTED |
| 4 | AudioDiagnosticsActivity gated behind BuildConfig.DEBUG | worker/gate6-firebase-telemetry | NOT STARTED |
| 5 | floor_reached + ability_used telemetry events added | worker/gate6-firebase-telemetry | NOT STARTED |
| 6 | Firebase provisioning runbook finalized | worker/gate6-firebase-telemetry | NOT STARTED |
| 7 | Play Console app entry, AAB upload, internal track | (manual — Play Console) | NOT STARTED |
| 8 | Ranger: GameBalance constants, SoundManager keys, sprite test | worker/ranger-polish | NOT STARTED |
| 9 | Localization scaffolding (locale folders, baseline strings) | worker/ranger-polish | NOT STARTED |

---

## Gate Status

### Gate 1 — Critical Bug Fixes
**Status: COMPLETE**
- Bitmap bundle issue resolved
- Ranger fallback: icon_knight + knight_sprite_sheet, accepted for launch
- ProGuard keep rules in place
- HEALING_POTION_STRENGTH made public for cross-class access

### Gate 2 — Performance
**Status: COMPLETE**
- Dirty-tile rendering
- Active tile animation tracking
- Monster bitmap cache pre-warming

### Gate 3 — Release Build
**Status: COMPLETE (smoke test pending)**
- signingConfigs scaffold present, reads from ~/.gradle/gradle.properties
- isMinifyEnabled = true, isShrinkResources = true
- versionCode=2, versionName="1.0.0"
- bundleRelease completed locally

**Remaining:** Physical-device smoke test (new game → combat → shop → boss → save/continue)
recorded in this document.

### Gate 4 — Store Assets and Legal
**Status: STARTED — not shippable**

In repo:
- Store copy draft (docs/STORE_COPY.md)
- Privacy policy draft (docs/PRIVACY_POLICY.md)
- Store asset README and validation script
- Custom launcher icons in all mipmap densities

Still required:
- 2+ phone screenshots (1080×1920 minimum)
- Feature graphic (1024×500)
- Final privacy policy text with real contact address (adapluguez@gmail.com)
- Privacy policy hosted at stable public URL
- URL entered in Play Console listing

**Owner:** worker/gate4-store-assets

### Gate 5 — Google Play Console
**Status: NOT STARTED**
- Create Play Console app entry
- Upload signed AAB to internal testing track
- Complete content rating questionnaire
- Complete store listing metadata
- Run internal QA pass

**Note:** This gate is manual Play Console work and cannot be done in code.
Document results in this file under Gate 5 results section once complete.

### Gate 6 — Firebase Baseline
**Status: CODE COMPLETE — provisioning + verification pending**

Done:
- Google Services + Crashlytics plugins wired (root + app build.gradle.kts)
- firebase-bom, firebase-crashlytics, firebase-analytics in app/build.gradle.kts
- FirebaseCrashlytics initialized in ClickDungeonApp (release-only collection)
- TelemetryManager wraps FirebaseAnalytics with 8 typed methods
- google-services.json in .gitignore

Remaining:
- Provision Firebase project for com.adaplu.clickdungeon
- Download and place google-services.json at app/ (local only, never commit)
- Build release APK and verify events appear in Firebase DebugView
- Gate AudioDiagnosticsActivity behind BuildConfig.DEBUG
- Add floor_reached and ability_used telemetry calls (spec in TELEMETRY_EVENTS.md)

**Owner:** worker/gate6-firebase-telemetry

---

## Ordered Next Steps

1. **Lead:** Assign and create three worker branches (done — see below)
2. **worker/gate4-store-assets** — Screenshots, feature graphic, privacy policy
3. **worker/gate6-firebase-telemetry** — Firebase runbook, audio gate, telemetry completions
4. **worker/ranger-polish** — Ranger balance constants, SoundManager keys, localization scaffold
5. **Lead review** each PR before merge to main
6. **Gate 5** — Manual Play Console steps (no code)
7. **Release** — after Gate 5 internal QA passes

---

## Active Worker Branches

| Branch | Owner | Task | Base |
|--------|-------|------|------|
| worker/gate4-store-assets | Agent 1 | Gate 4 completion | main |
| worker/gate6-firebase-telemetry | Agent 2 | Gate 6 telemetry + audio gate | main |
| worker/ranger-polish | Agent 3 | Ranger balance + localization scaffold | main |

---

## Release Checklist

```
Gate 1 - Critical bug fixes
[x] Bitmap bundle issue resolved
[x] Ranger fallback added and accepted for launch
[x] ProGuard keep rules added

Gate 2 - Performance
[x] Dirty-tile render path
[x] Active animation set
[x] Bitmap cache pre-warm

Gate 3 - Release build
[x] Signing config scaffold
[x] Minify + shrink resources
[x] versionCode=2 / versionName=1.0.0
[x] bundleRelease completed locally
[ ] Physical-device smoke test recorded

Gate 4 - Store assets and legal
[x] Draft store copy added
[x] Draft privacy policy added
[x] Store asset README added
[x] Store asset validation script added
[x] Custom launcher icons in all mipmap densities
[ ] Screenshots produced (minimum 2 phone)
[ ] Feature graphic produced (1024x500)
[ ] Final privacy policy text approved + contact address added
[ ] Privacy policy hosted at stable public URL
[ ] URL added to Play Console listing

Gate 5 - Play Console
[ ] App created
[ ] Internal test upload completed
[ ] Internal QA completed
[ ] Production release prepared

Gate 6 - Firebase baseline
[ ] Firebase project created
[ ] google-services.json placed locally (never commit)
[x] Google Services plugin wired
[x] Crashlytics + Analytics dependencies wired
[x] FirebaseCrashlytics initialized (release-only collection)
[x] TelemetryManager: 8 typed event methods
[ ] AudioDiagnosticsActivity gated behind BuildConfig.DEBUG
[ ] floor_reached telemetry call added
[ ] ability_used telemetry call added
[ ] Firebase event verified on release build
```

---

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Keystore loss after first upload | Critical | Back up keystore + passwords offline — alias: clickdungeon, store: clickdungeon.jks |
| Draft legal/store docs mistaken for final | High | Gate 4 status must be COMPLETE before any Play Console submission |
| Firebase provisioned against wrong package name | High | Use com.adaplu.clickdungeon consistently everywhere |
| Worker pushes directly to main | High | Branch protection enforced — all changes via PR + lead review |
| Ranger placeholder art ships permanently | Low | Dedicated icon_ranger + ranger_sprite_sheet.png on post-launch backlog |

---

## Gate 3 Smoke Test Results (fill in when run)

```
Date:
Device:
Build type: release
New game → class selection: PASS/FAIL
Dungeon board renders: PASS/FAIL
Combat (at least 1 win, 1 loss): PASS/FAIL
Boss floor (floor 5): PASS/FAIL
Shop: PASS/FAIL
Save and continue: PASS/FAIL
Notes:
```
