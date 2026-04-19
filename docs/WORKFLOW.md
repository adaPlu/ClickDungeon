# ClickDungeon Development Workflow

**Established:** 2026-04-18
**Owner:** Lead (pluguezadam)

---

## Branch → Review → Merge Process

This process is mandatory for all code going into `main`. No direct pushes to main.

```
1. Worker creates feature branch from main
   git checkout main && git pull
   git checkout -b worker/<feature-name>

2. Worker develops, commits often, pushes branch
   git push origin worker/<feature-name>

3. Worker opens PR on GitHub targeting main
   Title: [Agent N] Brief description
   Body: What changed, what was tested, checklist complete?

4. Lead reviews PR
   - Checks: compile clean, tests green, no regressions, follows code style
   - Requests changes OR approves

5. Lead merges PR to main (never the worker)
   - Merge commit (--no-ff) with lead review notes
   - Delete worker branch after merge

6. Next worker branches from updated main
```

---

## Worker Agent Assignments (current round)

### Agent 1 — worker/gate4-store-assets
**Goal:** Complete Gate 4 so the app can enter Play Console.

Tasks:
1. Create `docs/screenshots/` folder with at minimum:
   - `phone_01_main_menu.png` placeholder (1080×1920 or real capture)
   - `phone_02_dungeon.png` placeholder
   Note: Real screenshots must be taken on a device. Create XML layout
   descriptor `docs/screenshots/SCREENSHOT_SPEC.md` specifying exact screens,
   device frame, and caption text so a human can capture them.
2. Create `docs/feature_graphic_spec.md` — 1024×500 design brief describing
   colors, text, and composition for the feature graphic.
3. Update `docs/PRIVACY_POLICY.md`:
   - Replace `[CONTACT EMAIL]` placeholder with `adapluguez@gmail.com`
   - Replace `[APP NAME]` with `ClickDungeon`
   - Replace `[DEVELOPER NAME]` with `Ada Pluguez`
   - Add a "Hosted at:" line noting where this will be published (GitHub Pages)
4. Create `docs/GATE4_CHECKLIST.md` tracking every item with DONE/PENDING status.
5. Update Gate 4 checklist in `docs/LAUNCH_PLAN.md`.
6. Add a `scripts/validate_store_assets.sh` (or `.ps1`) that checks that the
   required files exist and prints a pass/fail for each.

**PR title:** `feat(gate4): store asset specs and privacy policy finalization`
**Branch base:** main
**Do NOT touch:** any Java/Kotlin source files, Gradle files

---

### Agent 2 — worker/gate6-firebase-telemetry
**Goal:** Complete all code-side Gate 6 items and unblock Firebase verification.

Tasks:
1. Gate `AudioDiagnosticsActivity` behind `BuildConfig.DEBUG`:
   - File: `app/src/main/java/com/adaplu/clickdungeon/ui/SettingsActivity.java`
     (or wherever the diagnostics nav is wired)
   - Wrap the diagnostics menu item / button with `if (BuildConfig.DEBUG)` so
     it never appears in release builds.
   - Add a Robolectric test in `AudioDiagnosticsActivityTest.java` asserting
     the diagnostics entry is gone in non-debug builds.

2. Add `floor_reached` telemetry call:
   - Add `logFloorReached(int floor, String terrain, boolean isBossFloor)` to
     `TelemetryManager.java`
   - Call it from `GameActivity` when the player descends stairs
     (search for `currentFloor++` or `startNextFloor`)
   - Add test case to `TelemetryManagerTest.java`

3. Add `ability_used` telemetry call:
   - Add `logAbilityUsed(String abilityName, String playerClass, int floor)` to
     `TelemetryManager.java`
   - Call it from `GameActivity.consumeAbilityUse()` passing current ability + class
   - Add test case to `TelemetryManagerTest.java`

4. Create `docs/FIREBASE_PROVISIONING_RUNBOOK.md`:
   - Step-by-step: create Firebase project → add Android app →
     download google-services.json → place at app/ → build release APK →
     enable Analytics DebugView → verify events
   - Note: google-services.json must NEVER be committed (in .gitignore already)

5. Update Gate 6 checklist in `docs/LAUNCH_PLAN.md`.

**PR title:** `feat(gate6): audio gate + telemetry completions + provisioning runbook`
**Branch base:** main
**Tests required:** All new TelemetryManager methods covered by null-guard tests

---

### Agent 3 — worker/ranger-polish
**Goal:** Polish the Ranger integration and scaffold localization.

Tasks:
1. Add Ranger balance constants to `GameBalance.java`:
   - `RANGER_PIERCING_SHOT_BASE = 3` (damage before level scale)
   - `RANGER_RAPID_VOLLEY_BASE = 2`
   - `RANGER_EAGLE_EYE_CRIT_MULT = 2`
   - Replace the hardcoded values in `GameActivity` Ranger ability methods
     with `GameBalance.RANGER_*` constants.

2. Register Ranger sound keys in `SoundManager` (explicit Knight fallback):
   - In `SoundManager.java`, wherever Knight keys are registered, also register
     `ranger_attack`, `ranger_defend`, `ranger_move` mapped to the same raw
     resource as the Knight equivalents.
   - This removes the silent fallback and makes the mapping explicit and auditable.
   - Add test in `SoundManagerKeysTest.java` asserting ranger keys are registered.

3. Add Ranger to `GameActivityTileViewTest.java`:
   - Add a test: `getPlayerSpriteSheetResource_ranger_fallsBackToKnight()`
     asserting RANGER returns `knight_sprite_sheet` until dedicated assets ship.

4. Add Ranger selection to `ClassSelectionActivityTest.java`:
   - Add test: `selectingRanger_disablesRangerButton_enablesOthers()`

5. Localization scaffold:
   - Create `app/src/main/res/values-es/strings.xml` with a skeleton containing
     only `app_name` and 3-5 key UI strings translated to Spanish as a
     proof-of-concept. All others should be `<!-- TODO: translate -->` stubs.
   - Create `docs/LOCALIZATION.md` documenting the process for adding a new locale.

6. Update `docs/ROADMAP.md` Phase 6 and Phase 8 checkboxes to reflect completed work.

**PR title:** `feat(ranger-polish): balance constants, sound keys, localization scaffold`
**Branch base:** main
**Tests required:** SoundManager keys test, sprite fallback test, class selection test

---

## Lead Review Checklist (apply to every PR)

Before merging any worker PR to main, the lead must verify:

```
[ ] ./gradlew :app:compileDebugJavaWithJavac — CLEAN (only pre-existing warnings)
[ ] ./gradlew :app:testDebugUnitTest — all tests pass
[ ] No direct Firebase calls without try/catch guard
[ ] No hardcoded credentials, package names, or emails in source files
[ ] New tests cover both happy path and at least one edge/failure case
[ ] LAUNCH_PLAN.md gate checklist updated if applicable
[ ] No gradle files modified without lead approval
[ ] No changes to main branch signing config
```

---

## Context Handoff Protocol

When context is running low (indicated by lead), before stopping:

1. Run `./gradlew :app:testDebugUnitTest` — confirm green
2. Commit and push everything on active branches
3. Update `docs/LAUNCH_PLAN.md` with current gate status
4. Write a handoff block at the bottom of this file (see template below)
5. Push to remote

### Handoff Template

```
## Handoff — [DATE]

### Completed this session
- [item]

### In progress (branch: worker/X)
- [what was done]
- [what remains]

### Not started
- [item + assigned agent]

### Lead merge queue
- [branch] — [review status]
```
