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

### GATE 1 — Critical Bug Fixes (1–2 sessions)
These are blocking bugs that will cause crashes or bad game-state in production.

**1.1 Fix CombatDialogFragment bitmap bundle (TransactionTooLargeException)**  
- File: [CombatDialogFragment.java](../app/src/main/java/com/example/clickdungeon/ui/CombatDialogFragment.java)
- Change: Remove `Bitmap` passing through fragment arguments. Pass only `monsterId`/`monsterType` string; have the fragment fetch its own frames from `MonsterAnimationHelper` / `LruCache` on `onCreateView`.
- Reference: `PERFORMANCE_PLAN.md` Phase 3, Action 3.1–3.2

**1.2 Enable ProGuard/R8 for release**  
- File: [build.gradle.kts](../app/build.gradle.kts)
- Change: Set `isMinifyEnabled = true`, add ProGuard keep rules for Gson models, Robolectric is test-only (already fine)
- Keep rules needed for: `CharacterProfile`, `InventoryItem`, `ShopItem`, `PricedItem`, `Achievement`, `AnimatedPlayer`, `AnimatedMonster`

**1.3 Decide: finish Ranger or hide it**  
Two options:
- **Option A (defer to v1.1):** Ranger is not on `ClassSelectionActivity` UI now — confirm no code path reaches `PlayerClass.RANGER` during normal play, add a `getPlayerSpriteSheetResource` case for it (return knight fallback explicitly), add a unit test asserting Ranger falls back cleanly.
- **Option B (ship in v1.0):** Add `ranger_sprite_sheet.png` + `icon_ranger.png` assets, define 5 Ranger abilities in `PlayerClass`, wire a Ranger button in `ClassSelectionActivity`, add Ranger ability handling in `GameActivity.useAbility()`.

Recommendation: **Option A for v1.0**, ship Ranger as v1.1 content.

---

### GATE 2 — Performance Fixes (1–2 sessions)
Required before Play Store submission to avoid ANRs on mid/low-end devices.

**2.1 Grid dirty-flag optimization**  
- `PERFORMANCE_PLAN.md` Phase 1: Add `isDirty()` flag to `Tile`, update `renderGrid()` to skip clean tiles, add `refreshTile(row, col)` for targeted updates.

**2.2 Animation loop active-tile set**  
- `PERFORMANCE_PLAN.md` Phase 2: Replace polling all 25 tiles in `gridAnimationRunnable` with a maintained `List<TileView>` of active animated tiles (player + revealed enemies only).

**2.3 Memory cache tuning**  
- `PERFORMANCE_PLAN.md` Phase 4: Profile heap during 15-floor run, adjust `initBitmapCache` to a fixed or conservative memory fraction, pre-warm floor monster bitmaps before `renderGrid`.

> Note: Phase 3 (bitmap bundle) is already captured in Gate 1.1.

---

### GATE 3 — Release Build Config (1 session)

**3.1 Create release signing config**  
- Generate a keystore: `keytool -genkey -v -keystore clickdungeon.jks -alias clickdungeon -keyalg RSA -keysize 2048 -validity 10000`
- Store credentials in `~/.gradle/gradle.properties` (not in repo)
- Add `signingConfigs` block to `build.gradle.kts`, reference it in `release` buildType
- Add `clickdungeon.jks` to `.gitignore`

**3.2 Enable minification**  
- `isMinifyEnabled = true`, `isShrinkResources = true`
- Write `proguard-rules.pro` keep rules for all Gson-serialized models

**3.3 Bump version**  
- `versionCode = 2`, `versionName = "1.0.0"` in `build.gradle.kts`

**3.4 Build and smoke-test the release APK**  
- `./gradlew assembleRelease`
- Install on a physical device and run through at least: new game → floor 1 → combat → shop → save/quit → continue

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
GATE 0 — Commit & Merge
[ ] Commit GameActivity floor reset + MerchantManager.clearBuybackItems
[ ] Commit ShopActivity exit button + layout
[ ] Commit main menu margin tweak
[ ] Commit BillingManager tests
[ ] Merge PreError → main

GATE 1 — Critical Bug Fixes
[ ] Fix CombatDialogFragment bitmap-in-bundle (pass monsterId only)
[ ] Confirm Ranger is unreachable or add explicit fallback + test
[ ] ProGuard keep rules for Gson models (prep for Gate 3)

GATE 2 — Performance
[ ] Tile dirty-flag system + renderGrid skip
[ ] Animation loop active-tile set
[ ] Bitmap cache pre-warm + size tuning

GATE 3 — Release Build
[ ] Generate keystore, add signingConfigs to build.gradle.kts
[ ] isMinifyEnabled = true, write proguard-rules.pro
[ ] versionCode=2, versionName="1.0.0"
[ ] ./gradlew bundleRelease — green build
[ ] Smoke test release APK on device

GATE 4 — Store Assets
[ ] Custom launcher icon (all densities)
[ ] Screenshots (phone, ≥2)
[ ] Feature graphic (1024×500)
[ ] Short + full store description written
[ ] Privacy policy hosted at stable URL

GATE 5 — Play Console
[ ] App created in Play Console
[ ] Internal test APK uploaded + tested
[ ] Content rating questionnaire complete
[ ] Production release submitted

GATE 6 — Firebase
[ ] Firebase project created, google-services.json added
[ ] Crashlytics integrated and tested
[ ] firebase-bom version pinned in build.gradle.kts
```

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| TransactionTooLargeException from bitmap bundle | High — crash on rotation during combat | Gate 1.1 fix |
| keystore loss after first upload | Critical — permanent app lock-out | Back up keystore + passwords to secure offline storage immediately after creation |
| Ranger selected via saved profile from a future test | Medium — falls back to knight sprite silently | Add explicit RANGER case in `getPlayerSpriteSheetResource` now |
| ProGuard stripping Gson model fields | High — save data parse failure | Gate 1.2/3.2 keep rules |
| `versionCode=1` already used in Play Console | Build fail at upload | Bump to `versionCode=2` before any Play Console upload |
| google-services.json committed to repo | Security | Add to `.gitignore` now, use CI secrets per `docs/CI_SECRETS.md` |
