# ClickDungeon Launch Plan

**Created:** 2026-04-06  
**Last updated:** 2026-04-09  
**Current version:** 1.0.0 (`versionCode=2`)  
**Current branch at audit:** `scaffold/pr-store-assets`  
**Package name:** `com.adaplu.clickdungeon`  
**Goal:** Ship v1.0 on Google Play

---

## Current State Summary

### Verified in Code
- Full dungeon loop: 5x5 grid, terrain-per-floor, traps, boss floors, XP/gold,
  status effects, and save/continue flow
- Three playable classes with ability progression: Knight, Thief, Wizard
- Combat dialog with animations, monster telegraphs, and action flow
- Save system with encrypted preferences and integrity checks
- Shop, buyback flow, achievements, onboarding, settings, and audio diagnostics
- Release build scaffold in place with signing config support, minification, and
  resource shrinking
- Animated and static dungeon board art is committed, and board tiles now render
  with images instead of text letters
- Unit test suite passes locally via `:app:testDebugUnitTest`

### Verified Artifacts
- Release AAB present under `app/build/outputs/bundle/release/`
- Keystore-based signing scaffold present in Gradle config

### Still Open Before Release
1. Store/legal assets are only partially scaffolded; launcher icons,
   screenshots, feature graphic, and hosted privacy-policy URL are still
   missing.
2. Firebase/Crashlytics is still not wired in code. There is no
   `google-services.json`, no Google Services plugin, and no Firebase
   dependency setup in the app module.
3. Play Console setup and internal testing are not yet complete.
4. Localization scaffolding is still absent.
5. Ranger remains intentionally incomplete and must stay unreachable until it is
   properly finished.

---

## Gate Status

### Gate 0 - Commit and Merge
Status: complete historically. Earlier notes referring to the `PreError`
branch are obsolete and should not drive current release work.

### Gate 1 - Critical Bug Fixes
Status: complete

- `CombatDialogFragment` no longer stores bitmaps in fragment arguments
- ProGuard keep rules and minify/resource-shrink settings are in place
- `RANGER` has a safe fallback to Knight assets and test coverage

### Gate 2 - Performance
Status: complete

- Dirty-tile rendering is implemented
- Active tile animation tracking is implemented
- Monster bitmap cache pre-warming is implemented

### Gate 3 - Release Build
Status: mostly complete

- `signingConfigs` scaffold is present in `app/build.gradle.kts`
- `isMinifyEnabled = true` and `isShrinkResources = true`
- `versionCode = 2`, `versionName = "1.0.0"`
- Release bundle generation has succeeded locally

Remaining work:
- Smoke-test the release build on a physical device and record the result

### Gate 4 - Store Assets and Legal
Status: started, not shippable

In repo:
- Store copy draft exists
- Privacy policy draft exists
- Store asset naming README exists
- Store-asset validation script exists

Still required:
- Replace default launcher icons in mipmap resources
- Produce final screenshots
- Produce final feature graphic
- Replace draft privacy policy with approved final text
- Host privacy policy at a stable public URL

### Gate 5 - Google Play Console
Status: not started in repo-verifiable work

Required:
- Create Play Console app entry
- Upload signed AAB to internal testing
- Run test matrix on internal track
- Complete content rating and store listing metadata

### Gate 6 - Firebase Baseline
Status: not started in code

Required:
- Create Firebase project for `com.adaplu.clickdungeon`
- Add local `app/google-services.json` for testing only
- Wire Google Services plugin in Gradle
- Add Crashlytics and Analytics dependencies
- Initialize connected-service collection policy in app startup
- Verify a non-fatal event in Firebase console

---

## Ordered Next Steps

1. Finish Gate 4 cleanup work
   - Replace patch-artifact docs with final draft docs
   - Produce launcher icon assets
   - Produce screenshots and feature graphic
   - Host privacy policy URL

2. Run release smoke test on device
   - New game
   - Dungeon board tile rendering
   - Combat
   - Shop
   - Save and continue
   - Boss floor

3. Start Gate 6 only after Gate 4 docs are clean
   - Provision Firebase against `com.adaplu.clickdungeon`
   - Keep `google-services.json` out of git
   - Wire Crashlytics and validate it

4. Complete Gate 5
   - Internal track upload
   - Internal QA pass
   - Production rollout prep

---

## Release Checklist

```text
Gate 1 - Critical bug fixes
[x] Bitmap bundle issue resolved
[x] Ranger fallback added
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
[ ] Launcher icons replaced
[ ] Screenshots added
[ ] Feature graphic added
[ ] Final privacy policy hosted publicly

Gate 5 - Play Console
[ ] App created
[ ] Internal test upload completed
[ ] Internal QA completed
[ ] Production release prepared

Gate 6 - Firebase baseline
[ ] Firebase project created
[ ] google-services.json added locally only
[ ] Google Services plugin wired
[ ] Crashlytics + Analytics dependencies wired
[ ] Firebase event verified
```

---

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Keystore loss after first upload | Critical | Back up keystore and passwords offline immediately |
| Draft legal/store docs mistaken for final assets | High | Keep Gate 4 status explicit and require review before release |
| Firebase provisioned against wrong package name | High | Use `com.adaplu.clickdungeon` consistently in all setup docs |
| Default launcher icons shipped by mistake | Medium | Treat icon replacement as a hard release gate |
| Ranger accidentally exposed before implementation | Medium | Keep class-selection wiring disabled until complete |
