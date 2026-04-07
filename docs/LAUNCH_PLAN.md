# ClickDungeon Launch Plan

**Created:** 2026-04-06  
**Current version:** 0.06 (versionCode=1)  
**Branch at audit:** PreError  
**Goal:** Ship v1.0 on Google Play

---

## Current State Summary

### What Works (Do Not Break)
- Full dungeon loop: 5x5 grid, terrain-per-floor, traps, boss floors, XP/gold, status effects
- 3 playable classes with full ability trees: Knight, Thief, Wizard (5 abilities each, levels 1/5/10/15/20)
- Combat dialog with animation panels, monster telegraphs, flee/potion/attack
- Save system: 4 slots, encrypted prefs (Tink + Keystore), HMAC integrity, backup restore
- Shop, buyback/sell flow, PremiumStore (placeholder)
- Achievements, onboarding tips, settings (audio/vibration/difficulty/color-blind)
- SoundManager with graceful fallbacks + Audio Diagnostics screen
- 60+ Robolectric/unit tests — all 20 backlog items COMPLETE
- Terrain backgrounds, animated sprites for all monster families

### Uncommitted Changes on `PreError` (commit these first)
| File | Change |
|------|--------|
| `GameActivity.java` | Game-over restart now resets `currentFloor=1` and `currentTerrain`; `MerchantManager.clearBuybackItems()` called on floor advance |
| `ShopActivity.java` | Exit Shop button wired |
| `activity_shop.xml` | Exit button layout added |
| `activity_main_menu.xml` | Button margin reduced 12dp → 6dp |
| `BillingManagerTest.java` | Additional billing manager smoke tests |

### Known Gaps Before v1.0
1. **Ranger class is incomplete** — exists in `PlayerClass` enum with base stats but: no sprite sheet (`ranger_sprite_sheet` missing), no icon (`icon_ranger` missing), no abilities defined in `PlayerClass`, not wired into `ClassSelectionActivity` (no button). Currently safe (unreachable), but must be finished or deferred.
2. **Release signing not configured** — `build.gradle.kts` has no `signingConfig` block for release builds.
3. **Minification disabled** — `isMinifyEnabled = false` in release; should be enabled with proper ProGuard rules.
4. **Performance plan unimplemented** — all 4 phases in `PERFORMANCE_PLAN.md` are "Planned" — especially the CombatDialogFragment bitmap-in-bundle issue which can trigger TransactionTooLargeException on config changes.
5. **No `google-services.json`** — Firebase not provisioned; BillingManager scaffolding exists but not connected.
6. **App icon is default** — launcher icons in mipmap folders are the Android default webp; need custom art.
7. **Localization scaffolding missing** — strings are externalized but no locale folders exist.
8. **No privacy policy URL** — required by Google Play.

---

## Launch Plan — Ordered by Dependency

### GATE 0 — Commit Current Fixes (1 session)
**Must do before any other work.**

- [ ] Commit all uncommitted changes on `PreError` (game-over floor reset, exit shop button, margin fix, billing tests)
- [ ] Merge `PreError` → `main`

---

### GATE 1 — Critical Bug Fixes ✅ COMPLETE

**1.1 CombatDialogFragment bitmap bundle** — ALREADY CLEAN  
`newInstance()` passes only a `String monsterType`. `onSaveInstanceState` saves only primitives. No bitmaps in bundles. Verified 2026-04-06.

**1.2 ProGuard keep rules** — DONE  
`proguard-rules.pro` updated with keep rules for all Gson-serialized models (`CharacterProfile`, `Tile`, `Monster`, `Achievement`, `InventoryItem`, `ShopItem`, `PricedItem`, `SaveBlob`, enums). `isMinifyEnabled = true` and `isShrinkResources = true` set in `build.gradle.kts`.

**1.3 Ranger fallback** — DONE (Option A)  
Explicit `case RANGER:` added to `getPlayerSpriteSheetResource()` falling through to knight. Unit test in `GameActivityTileViewTest` asserts `RANGER` returns `knight_sprite_sheet` without crash. Ranger button remains `visibility=gone` in `ClassSelectionActivity`.

---

### GATE 2 — Performance Fixes ✅ ALREADY IMPLEMENTED

All three phases were completed during the "Optimization Overhaul" commit series (2025-2026):

**2.1 Dirty-flag grid rendering** — ALREADY DONE  
`Tile.isDirty()` / `setDirty()` implemented in `model/Tile.java`. `renderGrid()` skips tiles where `!tile.isDirty()` at line 1251 and clears flag after bind at line 1470.

**2.2 Active-tile animation set** — ALREADY DONE  
`activeAnimatedTiles` (`Map<String, View>`) tracks only player tile + revealed enemy tiles. `animateGridFrame()` iterates only that map. `updateAnimatedTileRegistry()` maintains the set on every bind.

**2.3 Bitmap cache pre-warm** — ALREADY DONE  
`preWarmMonsterBitmaps(getMonsterPoolForFloor(currentFloor))` called at floor generation (line 426). `initBitmapCache()` uses a conservative LruCache sized from available memory.

---

### GATE 3 — Release Build Config ✅ SCAFFOLDED (keystore generation pending)

**3.1 Signing config scaffold** — DONE  
`signingConfigs { release { ... } }` block added to `build.gradle.kts`. Reads credentials from `~/.gradle/gradle.properties` via `project.findProperty()` — safe to commit, no secrets in repo. `*.jks`, `*.keystore`, `keystore.properties` added to `.gitignore`.

**Remaining manual step — generate the keystore (do once, back up offline):**
```
keytool -genkey -v -keystore clickdungeon.jks -alias clickdungeon \
        -keyalg RSA -keysize 2048 -validity 10000
```
Then add to `~/.gradle/gradle.properties`:
```
CLICKDUNGEON_STORE_FILE=/absolute/path/to/clickdungeon.jks
CLICKDUNGEON_STORE_PASSWORD=<keystore password>
CLICKDUNGEON_KEY_ALIAS=clickdungeon
CLICKDUNGEON_KEY_PASSWORD=<key password>
```

**3.2 Minification** — DONE  
`isMinifyEnabled = true`, `isShrinkResources = true` set. ProGuard rules in `proguard-rules.pro`.

**3.3 Version bump** — DONE  
`versionCode = 2`, `versionName = "1.0.0"` in `build.gradle.kts`.

**3.4 Build and smoke-test** — PENDING (requires keystore)  
```
./gradlew bundleRelease          # produces .aab for Play Console
./gradlew assembleRelease        # produces .apk for device sideload test
```
Smoke-test path: new game → floor 1 → combat → shop → save/quit → continue.

---

### GATE 4 — Store Assets & Legal (1 session)

**4.1 Custom launcher icon**  
- Replace default webp in all mipmap densities (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi) with custom dungeon-themed icon
- Update `mipmap-anydpi-v26` adaptive icon XML if using vector-based foreground/background

**4.2 Store listing content**  
- Short description (80 chars): dungeon crawler tagline
- Full description (4000 chars): features, how to play, class descriptions
- 2–8 screenshots (phone): main menu, dungeon floor, combat, shop, achievements
- Feature graphic (1024×500)
- Content rating questionnaire (violence: mild fantasy, no sexual content, no real gambling)

**4.3 Privacy policy**  
- Required by Play. Cover: no PII collected server-side, local encrypted save data, optional future analytics
- Host at a stable URL (GitHub Pages or similar), add URL to Play Console

**4.4 App category & tags**  
- Category: Games → Role Playing

---

### GATE 5 — Google Play Console Setup (1 session, external)

**5.1 Create app in Play Console**  
- App name: "Click Dungeon"
- Default language: English
- App or game: Game
- Free or paid: Free (with future IAP)

**5.2 Internal testing track**  
- Upload signed AAB (`./gradlew bundleRelease`)
- Add internal testers (yourself + any QA)
- Run through full test matrix: new game, all 3 classes, boss floor, save/restore, shop, achievements, settings

**5.3 Production release**  
- After internal test passes: promote to production or open beta
- Set rollout percentage to 20% initially, ramp after 48h if crash-free rate >99%

---

### GATE 6 — Firebase Baseline (parallel with Gate 4, external)
Minimum Firebase needed before v1.0 ship: **Crashlytics only** (no auth, no leaderboards).

**6.1 Firebase project**  
- Create project in Firebase Console
- Add Android app with package `com.example.clickdungeon`
- Download `google-services.json` → `app/`
- Follow `docs/FIREBASE_SETUP.md`

**6.2 Add Crashlytics**  
- Add `firebase-bom` + `firebase-crashlytics` + `firebase-analytics` to `build.gradle.kts`
- Add `google-services` plugin
- Initialize in `ClickDungeonApp.java` (already has app class)
- Test by forcing a non-fatal crash in debug build, verify it shows in console

> Firebase Auth, Firestore, leaderboards, and cloud save are v1.1+ work (Track 2).

---

## Post-Launch: v1.1 Roadmap

Once v1.0 is live and crash-free for 2 weeks, tackle in order:

| Priority | Work | Notes |
|----------|------|-------|
| High | **Ranger class** — Option B above | Sprite sheet + 5 abilities + ClassSelection wire-up |
| High | **Real IAP** — consumable platinum packs | Replace `BillingManager` stub with Play Billing Library 6 |
| Medium | **Firebase Auth + Cloud Save** | Anonymous auth first, cloud save via Firestore |
| Medium | **Leaderboard** | Floor-reached + gold-collected scores |
| Medium | **Daily challenges** | Seeded run modifiers, bonus platinum reward |
| Low | **Localization** | Spanish first (large Android market), then French/German |
| Low | **Accessibility** | Content descriptions on all dungeon tiles, focus order |
| Low | **Extended animations** | Trap trigger FX, ability particle overlays |

---

## Checklist Summary

```
GATE 0 — Commit & Merge ✅ COMPLETE (2026-04-06)
[x] Commit GameActivity floor reset + MerchantManager.clearBuybackItems
[x] Commit ShopActivity exit button + layout
[x] Commit main menu margin tweak
[x] Commit BillingManager tests
[x] Merge PreError → main

GATE 1 — Critical Bug Fixes ✅ COMPLETE (2026-04-07)
[x] CombatDialogFragment bitmap-in-bundle — already clean (verified)
[x] RANGER explicit fallback in getPlayerSpriteSheetResource + unit test added
[x] ProGuard keep rules written in proguard-rules.pro

GATE 2 — Performance ✅ ALREADY IMPLEMENTED (verified 2026-04-07)
[x] Tile dirty-flag system + renderGrid skip — already in Tile.java / GameActivity
[x] Animation loop active-tile set — activeAnimatedTiles Map already implemented
[x] Bitmap cache pre-warm — preWarmMonsterBitmaps() already called on floor gen

GATE 3 — Release Build ✅ COMPLETE (2026-04-07)
[x] signingConfigs block added to build.gradle.kts (reads from gradle.properties)
[x] isMinifyEnabled = true, isShrinkResources = true
[x] proguard-rules.pro written with full Gson model keep rules
[x] versionCode=2, versionName="1.0.0"
[x] *.jks / *.keystore added to .gitignore
[x] Keystore at ~/AndroidStudioProjects/ClickDungeon/clickdungeon.jks
[x] ./gradlew bundleRelease — BUILD SUCCESSFUL, signed AAB at app/build/outputs/bundle/release/
[ ] Smoke test release APK on physical device

GATE 4 — Store Assets ⏳ NOT STARTED
[ ] Custom launcher icon (all mipmap densities)
[ ] Screenshots (phone, ≥2)
[ ] Feature graphic (1024×500)
[ ] Short + full store description written
[ ] Privacy policy hosted at stable URL

GATE 5 — Play Console ⏳ NOT STARTED
[ ] App created in Play Console
[ ] Internal test AAB uploaded + tested
[ ] Content rating questionnaire complete
[ ] Production release submitted

GATE 6 — Firebase ⏳ NOT STARTED
[ ] Firebase project created, google-services.json added
[ ] Crashlytics + Analytics dependencies added to build.gradle.kts
[ ] Initialized in ClickDungeonApp.java
[ ] Non-fatal test crash verified in Firebase console
```

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| ~~TransactionTooLargeException from bitmap bundle~~ | ~~High~~ | Already clean — no bitmaps in bundles ✅ |
| keystore loss after first upload | Critical — permanent app lock-out | Back up keystore + passwords to secure offline storage immediately after creation |
| ~~Ranger selected via saved profile~~ | ~~Medium~~ | Explicit RANGER fallback + test added ✅ |
| ~~ProGuard stripping Gson model fields~~ | ~~High~~ | proguard-rules.pro keep rules written ✅ |
| ~~versionCode=1 already used in Play Console~~ | ~~Build fail~~ | Bumped to versionCode=2 ✅ |
| google-services.json committed to repo | Security | Add to `.gitignore` now, use CI secrets per `docs/CI_SECRETS.md` |
