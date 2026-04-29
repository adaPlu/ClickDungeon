# ClickDungeon Launch Plan

**Created:** 2026-04-06
**Last updated:** 2026-04-26
**Current version:** 1.0.0 (`versionCode=2`)
**Current branch at doc sync:** `main`
**Package name:** `com.adaplu.clickdungeon`
**Goal:** Ship v1.0 on Google Play

---

## Current State Summary

### Verified in Code
- Full dungeon loop: 5x5 grid with one health/attack/defense boost tile per
  floor, terrain-per-floor, traps, boss floors, XP/gold, status effects, and
  save/continue flow across the 1-99 campaign
- Four playable classes with ability progression: Knight, Ranger, Thief, Wizard
- Combat dialog with animations, monster telegraphs, and action flow
- Save system with encrypted preferences and integrity checks
- Shop, buyback flow, achievements, onboarding, settings, and audio diagnostics
- Release build scaffold in place with signing config support, minification, and
  resource shrinking
- Animated and static dungeon board art is committed, and board tiles now render
  with images instead of text letters
- Current verification lane is green: debug APK builds, `:app:testDebugUnitTest`
  passes, and `:app:lintDebug` passes
- Backend `npm audit fix` has been applied for non-breaking advisories. Remaining
  backend audit findings are Firebase Admin / Google Cloud transitive dependency
  advisories that npm only resolves via a breaking forced downgrade; resolve
  during Firebase provisioning with a tested dependency path.
- Tracked `gradle.properties` no longer carries signing secrets or a local JDK
  path
- English-only is the adopted v1 locale policy; translation scaffolding is
  post-launch work

### Verified Artifacts
- Debug APK build completed in the latest verification pass
- Keystore-based signing scaffold is present in Gradle config
- Release bundle generation has succeeded in prior local verification

### Still Open Before Release
1. Gate 4 is blocked only by external capture/hosting: final phone
   screenshots, hosted privacy-policy URL, and Play listing URL entry. In-repo
   copy, specs, policy text, launcher icons, feature graphic, and validation
   script are present.
2. Gate 5 Play Console work remains external to the repository: app entry,
   internal track upload, content rating, store listing metadata, and internal QA.
3. Gate 6 Firebase code wiring is in repo. External Firebase Console
   provisioning, local-only `google-services.json`, and console verification are
   still pending.
4. Ranger gameplay is enabled, and the temporary icon/sprite-sheet fallback is
   an acceptable launch fallback rather than a blocker. Dedicated Ranger art is
   a post-launch polish item.

---

## Gate Status

### Gate 0 - Commit and Merge
Status: complete historically. No current branch-specific action is required.

### Gate 1 - Critical Bug Fixes
Status: complete

- `CombatDialogFragment` no longer stores bitmaps in fragment arguments
- ProGuard keep rules and minify/resource-shrink settings are in place
- `RANGER` is enabled with safe icon/sprite fallbacks and test coverage

### Gate 2 - Performance
Status: complete

- Dirty-tile rendering is implemented
- Active tile animation tracking is implemented
- Monster bitmap cache pre-warming is implemented

### Gate 3 - Release Build
Status: verification lane green; release smoke pending

- `signingConfigs` scaffold is present in `app/build.gradle.kts`
- Signing secrets and local JDK paths are not tracked in `gradle.properties`
- `isMinifyEnabled = true` and `isShrinkResources = true`
- `versionCode = 2`, `versionName = "1.0.0"`
- Debug APK build completed in the latest verification pass
- `:app:testDebugUnitTest` and `:app:lintDebug` pass
- Release bundle generation has succeeded locally

Remaining work:
- Smoke-test the release build on a physical device and record the result

### Gate 4 - Store Assets and Legal
Status: in-repo work complete; blocked only by external screenshots and hosting

In repo:
- Store copy draft exists
- Privacy policy text exists
- Store asset naming README exists
- Screenshot and feature graphic specs exist
- Feature graphic exists at `docs/store_assets/feature_graphic.png`
- Store-asset validation script exists
- Custom launcher icons are present in all mipmap densities
- English-only v1 locale policy is adopted

Still required externally:
- Capture final screenshots (minimum 2 phone) to `docs/screenshots/output/`
- Host privacy policy at a stable public URL
- Add hosted URL to Play Console listing

### Gate 5 - Google Play Console
Status: external Console work pending; in-repo inputs are ready

In repo:
- Package name is documented as `com.adaplu.clickdungeon`
- Version is documented as `1.0.0` / `versionCode=2`
- Store copy, privacy policy text, launcher icons, and asset specs are present
- Signing scaffold exists, with secrets kept out of tracked files

Still required in Play Console:
- Create Play Console app entry
- Upload signed AAB to internal testing
- Complete content rating and store listing metadata
- Run test matrix on internal track

### Gate 6 - Firebase Baseline
Status: in-repo wiring complete; external Firebase provisioning pending

Done in repo:
- Google Services and Crashlytics plugins wired in `build.gradle.kts` (root and app module)
- `firebase-bom`, `firebase-crashlytics`, `firebase-analytics` dependencies in `app/build.gradle.kts`
- `FirebaseCrashlytics` initialized in `ClickDungeonApp.java` (disabled in debug, enabled in release)
- `google-services.json` is ignored and should remain local-only

Still required in Firebase Console/local environment:
- Create Firebase project for `com.adaplu.clickdungeon`
- Download and place `google-services.json` at `app/google-services.json` locally only
- Run release build and verify a non-fatal event appears in Firebase Console

---

## Ordered Next Steps

1. Finish Gate 4 external screenshots and hosting
   - Capture screenshots on a real device with `.\scripts\capture_play_screenshots.ps1`
   - Host the privacy policy URL
   - Add the URL to the Play Console listing

2. Run release smoke test on device
   - New game
   - Dungeon board tile rendering
   - Combat
   - Shop
   - Save and continue
   - Boss floor

3. Complete Gate 5 external Play Console setup
   - Create app entry
   - Upload signed AAB to internal testing
   - Complete listing metadata/content rating
   - Run internal QA pass

4. Complete Gate 6 external Firebase setup
   - Provision Firebase against `com.adaplu.clickdungeon`
   - Keep `google-services.json` out of git
   - Validate Crashlytics event delivery

---

## Release Smoke-Test Runbook

Use this runbook after the current verification lane is green. As of the latest
doc sync on 2026-04-26, the debug APK builds, `:app:testDebugUnitTest` passes,
and `:app:lintDebug` passes. Record the device model, Android version, build
type, APK path, tester, date, and pass/fail notes when completing the smoke.

### Preconditions
- Use a physical Android device with USB debugging enabled and the screen
  unlocked.
- Confirm only one device is attached, or note the target serial:
  `adb devices`
- Build the APK under test before installation:
  - Debug APK: `.\gradlew.bat :app:assembleDebug --no-daemon`
  - Signed release APK: `.\gradlew.bat :app:assembleRelease --no-daemon`
- Keep the automated lane green before using manual smoke as release evidence:
  `.\gradlew.bat :app:testDebugUnitTest :app:lintDebug --no-daemon`

### Install, Clear Data, and Launch

Option A: run the helper from the repo root.

```powershell
# Default debug APK install
.\scripts\release_smoke_check.ps1

# Signed release APK install, if produced locally
.\scripts\release_smoke_check.ps1 -ApkPath app\build\outputs\apk\release\app-release.apk

# If multiple devices are connected
.\scripts\release_smoke_check.ps1 -DeviceSerial <adb-serial>
```

Option B: run the ADB commands manually.

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell pm clear com.adaplu.clickdungeon
adb shell am start -n com.adaplu.clickdungeon/.MainMenuActivity
```

For a signed release APK, replace the install path with the exact signed APK
path, for example:

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

### Manual Checklist

Mark each item PASS, FAIL, or NOT RUN with notes and screenshots/logcat excerpts
for any failure.

```text
Device/build setup
[ ] App installs without adb errors
[ ] App data was cleared before the run
[ ] Main menu launches without crash or ANR
[ ] Continue is disabled or marked unavailable on a clean install

New game
[ ] New Game opens save-slot/class flow
[ ] A class can be selected and starts a run
[ ] Initial HUD shows floor, HP, gold/platinum, and status area correctly

Board rendering
[ ] 5x5 board renders with image tiles, not text placeholders
[ ] Revealing covered tiles updates only the selected tile area visibly
[ ] Hero marker, terrain/backdrop, loot, keys, stairs, traps, and enemy tiles are legible
[ ] Rotation is not required; portrait layout remains stable on the test device

Combat
[ ] Opening an enemy tile shows the combat dialog with player and monster art
[ ] Attack/defend/potion/flee controls are visible and tappable as applicable
[ ] At least one attack exchange updates HP and combat summary text correctly
[ ] Victory path closes cleanly and awards expected loot/XP/gold feedback
[ ] Loss or flee path is not required unless encountered naturally; record if observed

Shop
[ ] Main-menu Shop opens and renders item list, prices, stock, and currency
[ ] Refresh/exit controls work without crash
[ ] During a run, collect gold and verify an eligible purchase or insufficient-funds message
[ ] If a merchant appears between floors, verify merchant launch, exit, and return to run

Save and continue
[ ] Reveal at least one tile or clear one combat, then leave the app via Home/Back
[ ] Relaunch with `adb shell am start -n com.adaplu.clickdungeon/.MainMenuActivity`
[ ] Continue is enabled and opens the save-slot flow
[ ] Continuing restores class, floor, HP/currency, and revealed board state

Boss-floor path
[ ] If feasible, keep clearing floors until floor 10, the first boss floor
[ ] Floor 10 shows boss-floor labeling or a boss enemy encounter
[ ] Boss combat opens with boss labeling/phases if presented
[ ] Defeating or safely exiting the boss encounter does not crash
[ ] If not feasible in the smoke window, record NOT RUN with the highest floor reached
```

### Smoke Result Template

```text
Date:
Tester:
Device model / Android version:
Build type: debug | signed release
APK path:
Git commit:
Automated lane: debug APK build PASS, testDebugUnitTest PASS, lintDebug PASS
Smoke result: PASS | FAIL | BLOCKED
Highest floor reached:
Boss-floor result: PASS | FAIL | NOT RUN
Notes:
```

Screenshot evidence is captured separately with
`.\scripts\capture_play_screenshots.ps1` and written to
`docs/screenshots/output/`. The feature graphic is already present at
`docs/store_assets/feature_graphic.png`.

---

## Release Checklist

```text
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
[x] Signing secrets/local JDK path absent from tracked gradle.properties
[x] Minify + shrink resources
[x] versionCode=2 / versionName=1.0.0
[x] Debug APK build completed
[x] testDebugUnitTest completed
[x] lintDebug completed
[x] bundleRelease completed locally
[ ] Physical-device smoke test recorded

Gate 4 - Store assets and legal
[x] Draft store copy added
[x] Privacy policy text added
[x] Store asset README added
[x] Store asset validation script added
[x] Custom launcher icons present in all mipmap densities
[x] Screenshot spec document created
[x] Feature graphic spec created
[x] Feature graphic produced
[x] English-only v1 locale policy adopted
[ ] Screenshots captured on device under docs/screenshots/output/
[ ] Privacy policy hosted at public URL
[ ] URL added to Play Console listing

Gate 5 - Play Console
[ ] App created
[ ] Internal test upload completed
[ ] Internal QA completed
[ ] Production release prepared

Gate 6 - Firebase baseline
[ ] Firebase project created
[ ] google-services.json added locally only
[x] Google Services plugin wired
[x] Crashlytics + Analytics dependencies wired
[x] FirebaseCrashlytics initialized in ClickDungeonApp (release-only collection)
[ ] Firebase event verified
```

---

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Keystore loss after first upload | Critical | Back up keystore and passwords offline immediately |
| Signing secrets or local paths accidentally committed | High | Keep tracked `gradle.properties` sanitized and place secrets only in local files or CI/console secret stores |
| Store assets treated as complete before capture/hosting | High | Keep Gate 4 blocked until screenshots and hosted privacy URL are externally verified |
| Firebase provisioned against wrong package name | High | Use `com.adaplu.clickdungeon` consistently in all setup docs |
| Ranger placeholder art is intentionally retained for launch | Low | Keep dedicated Ranger icon/sprite import on the post-launch content backlog |
