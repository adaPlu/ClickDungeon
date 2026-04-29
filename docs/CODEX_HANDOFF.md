# ClickDungeon — Codex Agent Team Handoff

**Date:** 2026-04-29  
**Branch:** `main` (HEAD: `3b15928`)  
**Pushed to:** `https://github.com/adaPlu/ClickDungeon.git`  
**Test suite:** 294 tests, 0 failures, 0 errors (`:app:testDebugUnitTest`)

---

## What This Project Is

Android dungeon-crawler game (Java, minSdk 26, targetSdk 34, Robolectric test suite).
Package: `com.adaplu.clickdungeon`. Goal: ship v1.0.0 on Google Play.

---

## Where We Are Right Now

All in-repo code work for v1.0 launch is complete. Every remaining blocker
is an external action (device capture, console setup, Firebase provisioning).
The test suite is fully green.

### Gate Status Summary

| Gate | Name | Status |
|------|------|--------|
| 1 | Critical bug fixes | Complete |
| 2 | Performance | Complete |
| 3 | Release build | Complete (smoke test pending — manual, device required) |
| 4 | Store assets & legal | In-repo complete; screenshots + privacy-policy URL pending externally |
| 5 | Play Console | External only — needs human with Google account |
| 6 | Firebase baseline | In-repo wiring complete; needs `google-services.json` provisioned locally |

### Remaining External Actions (no code required)

1. **Gate 3 smoke test** — Install the release APK on a physical Pixel device:
   new game → dungeon board renders → combat → shop → save/continue → boss floor.
   Record PASS/FAIL in `docs/LAUNCH_PLAN.md` Release Checklist.

2. **Gate 4 screenshots** — Run `.\scripts\capture_play_screenshots.ps1` on a
   connected device. Place PNGs in `docs/screenshots/output/`. Minimum 2 phone
   screenshots required for Play Console.

3. **Gate 4 privacy policy URL** — Host `docs/PRIVACY_POLICY.md` at a stable
   public URL (GitHub Pages: `adaplu.github.io/clickdungeon/privacy-policy` or
   any HTTPS URL). Add the URL to the Play Console store listing.

4. **Gate 5 Play Console** — Human action only:
   - Create app entry in Play Console for `com.adaplu.clickdungeon`
   - Upload the signed AAB from `app/build/outputs/bundle/release/`
   - Complete content rating questionnaire
   - Fill store listing metadata (copy at `docs/STORE_COPY.md`)
   - Run internal testing track QA pass

5. **Gate 6 Firebase** — Follow `docs/FIREBASE_PROVISIONING_RUNBOOK.md`:
   - Create Firebase project targeting `com.adaplu.clickdungeon`
   - Download `google-services.json` and place at `app/google-services.json`
     (never commit — already in `.gitignore`)
   - Build and sideload release APK, verify events in Firebase DebugView

---

## What Code Work Remains (Agent-assignable)

After the external actions above are done, the following code tasks remain before
or just after launch. Assign these to worker agents using the branch workflow in
`docs/WORKFLOW.md`:

### Priority 1 — Launch Blockers

None. All code blockers are resolved.

### Priority 2 — Post-Launch Polish (Phase 7–9 from ROADMAP.md)

**P2-A: Dedicated Ranger assets** (`worker/ranger-assets`)
- Add `res/drawable/icon_ranger.png` and `res/raw/ranger_sprite_sheet.png`
- Remove fallback references in `AnimatedPlayer.java` and `CombatDialogFragment.java`
- Update `SoundManager.java` to use real `ranger_attack`, `ranger_defend`, `ranger_move` audio

**P2-B: IAP server-side verification** (`worker/iap-verification`)
- Wire `BillingManager.java` to the Firebase backend functions in `backend/functions/`
- Add purchase verification endpoint to `backend/functions/index.js`
- Gate platinum currency on verified purchase receipt

**P2-C: Cloud save sync** (`worker/cloud-save`)
- Add Firestore document schema for per-UID save slots
- Wire `SaveManager.java` to upload/download save blobs on login
- Gate behind `SettingsManager.cloudSyncEnabled`

**P2-D: Leaderboard** (`worker/leaderboard`)
- Add Firestore `leaderboard` collection (floor + class + score)
- Wire `GameActivity.onFloorComplete()` to post score
- Add leaderboard screen to main menu

---

## Branch and Review Workflow (mandatory)

All code must go through:
```
1. Worker creates branch: git checkout -b worker/<feature-name>
2. Worker develops on that branch only
3. Worker pushes branch: git push origin worker/<feature-name>
4. Lead reviews via PR (compile + tests + no credentials + LAUNCH_PLAN updated)
5. Lead merges to main after approval
6. No direct pushes to main
```

See `docs/WORKFLOW.md` for the full checklist lead must run before merging.

---

## Key Files and Architecture

| File | Purpose |
|------|---------|
| `app/src/main/java/com/adaplu/clickdungeon/GameActivity.java` | Main game loop, combat, abilities (~4000 LOC) |
| `app/src/main/java/com/adaplu/clickdungeon/model/PlayerClass.java` | 4 classes: KNIGHT, RANGER, THIEF, WIZARD with ability progressions |
| `app/src/main/java/com/adaplu/clickdungeon/util/GameBalance.java` | All balance constants (damage, XP, costs) — edit here only |
| `app/src/main/java/com/adaplu/clickdungeon/util/TelemetryManager.java` | Firebase Analytics null-guard wrapper |
| `app/src/main/java/com/adaplu/clickdungeon/util/SaveManager.java` | Encrypted save with backup/restore |
| `app/src/main/java/com/adaplu/clickdungeon/ui/CombatDialogFragment.java` | Combat dialog, animations, telegraphs |
| `docs/LAUNCH_PLAN.md` | Authoritative gate status and release checklist |
| `docs/WORKFLOW.md` | Branch/PR/merge rules |
| `docs/FIREBASE_PROVISIONING_RUNBOOK.md` | Firebase setup steps |

---

## Test Setup

- Runner: Robolectric `@Config(sdk = 34)` for Android tests
- Firebase cannot initialize in unit tests — `ClickDungeonApp.onCreate()` wraps
  Firebase calls in try/catch so Robolectric doesn't crash
- `TelemetryManager`: every public method has a null-guard (`if (sAnalytics == null) return;`)
  making it safe to call before `init()`
- Run locally: `./gradlew :app:testDebugUnitTest`
- Never commit `google-services.json`, keystore files, or signing passwords

---

## Signing and Build

- Keystore: `clickdungeon-upload.jks` (in project root, not committed)
- Local signing config: `~/.gradle/gradle.properties` with keys:
  `CLICKDUNGEON_STORE_FILE`, `CLICKDUNGEON_STORE_PASSWORD`,
  `CLICKDUNGEON_KEY_ALIAS` (= `clickdungeon`), `CLICKDUNGEON_KEY_PASSWORD`
- Release AAB: `./gradlew :app:bundleRelease`
- See `docs/CI_SECRETS.md` for CI setup instructions

---

## Contacts and Resources

- **Package:** `com.adaplu.clickdungeon`
- **Play Console account:** `adapluguez@gmail.com`
- **Firebase project:** create for `com.adaplu.clickdungeon` (not yet provisioned)
- **Privacy policy host target:** `adaplu.github.io/clickdungeon/privacy-policy`
- **Store copy draft:** `docs/STORE_COPY.md`
- **Privacy policy draft:** `docs/PRIVACY_POLICY.md`

---

## How to Pick Up

```bash
git clone https://github.com/adaPlu/ClickDungeon.git
cd ClickDungeon
# Run tests to confirm green baseline
./gradlew :app:testDebugUnitTest
# Read the gate status
cat docs/LAUNCH_PLAN.md
# Read the workflow rules before creating any branch
cat docs/WORKFLOW.md
```

All external tasks (screenshots, Firebase provisioning, Play Console) require
the human developer (adapluguez@gmail.com) to act. Codex agents should focus
on the P2 code tasks listed above using the branch workflow.
