# ClickDungeon Development Roadmap

_Audit date: 2026-02-23_  
_Last doc sync: 2026-03-30 (Track 1 verified locally; Track 2 scaffolding added)_

This roadmap documents the verified state of the ClickDungeon Android project (Java sources under `app/src/main/java/com/example/clickdungeon`). Each assertion is tied to concrete files so future planning stays grounded in the current codebase.

## Documentation update note
- Updated `README.md`, `docs/INDEX.md`, `docs/DEVELOPER_SETUP.md`, and `docs/FIREBASE_SETUP.md` to match the current repository state.
- Added Track 1 support docs (`docs/TRACK1_TRIAGE.md`, `scripts/run_online_tests.ps1`) and verified `:app:testDebugUnitTest` locally on 2026-03-30.
- Added Track 2 preparation docs and scaffolding (`docs/CI_SECRETS.md`, GitHub issue templates/drafts, CI secret-injection placeholder, and `BillingManager`).
- Target: complete external Firebase / Play provisioning and then validate the connected-services rollout gates on real devices.

## Project Snapshot
- **Platform & build:** Android app targeting SDK 36, Kotlin-based Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`), and Java activities/fragments for every screen (Main Menu, Continue, Class Selection, Game, Shop, Achievements, Settings). Save-slot metadata lives in `util/SaveManager.java`.
- **Core loop:** `GameActivity.java` orchestrates dungeon generation (`DungeonGenerator.java`), tile metadata (`Tile.java`/`TileType.java`), trap/status resolution, class abilities, terrain assignment, monster families/affinities, inventory, gold rewards, XP, and save persistence. Class abilities are class-specific and expand at levels 1/5/10/15/20 with shared cooldown and targeting rules. Shared managers (`InventoryManager`, `AchievementManager`, `GameBalance`) keep the meta-layer consistent.
- **Combat & feedback:** `ui/CombatDialogFragment.java` owns combat turns, XP/gold previews, summary logs, and renders dedicated player/monster animation slots driven by `AnimatedPlayer` and `AnimatedMonster` (frame loop + SoundManager hooks). The dungeon grid also animates revealed enemies and the hero via a handler loop that iterates only active tiles (player + revealed enemies). `FeedbackManager` uses VibrationEffect on API 26+ with a legacy fallback.
- **UI theming:** Menu/continue screens now use door-themed backgrounds (`dungeon_door`, `dungeon_door_open`) while panel/button styling still uses shared assets (`panel_bg.xml`, `menu_button_bg.xml`). `GameActivity` now applies terrain-specific backgrounds dynamically based on the active terrain. Achievements show Locked/Completed state in the list.
- **Audio & settings:** `util/SoundManager.java` loads class/monster/effect cues once at app startup (`ClickDungeonApp.java`) with graceful fallbacks when a raw asset is absent and logs missing keys. `FeedbackManager` uses the same SoundPool and respects settings from `SettingsManager`. Audio/vibration/difficulty/color-blind preferences wire directly into `SettingsActivity`.
- **Persistence/UI surface:** Inventory, achievements, onboarding tips, and shop purchases are shared across runs via SharedPreferences + Gson with schema/checksum validation and backup recovery; Tink + Android Keystore encrypted prefs are used on API 23+ with fallback. Gold is the main shop currency; platinum is stored as a premium placeholder. Inventory UI supports equipment toggles, stat allocation, and MP display (inventory dialog + inventory activity), with an in-memory change log used for toast feedback. Merchant visits add sell/buyback lists on eligible floors. Multi-slot save/continue flows use ContinueActivity and GameActivity pause-triggered saves with debounced background persistence for non-critical events.
- **Tests:** Robolectric suites in app/src/test/java cover SaveManager integration, CombatDialog interactions (including animation/sound behavior), Settings UI, inventory/achievement helpers, dungeon generator, tile binding for the grid visuals, item catalog/merchant/loot roll helpers, persisted blob store and SecurePreferences coverage, plus added model/adapter/activity coverage. Core ability flows (cooldown, targeting, chooser) are unit-tested alongside terrain/affinity helpers and animation cloning with affinity metadata. Save snapshot immutability and coalesced save job coverage live alongside the SaveManager integration suite. Tests pin SDK 34 via robolectric.properties (local JDK 17 still required), and `app/src/online/java` is now included in the standard unit-test source set for smoke coverage.

## Confirmed Feature Coverage
1. **Dungeon exploration & status effects**
   - `GameActivity.java:323-1180` + `DungeonGenerator.java:21-102` implement 5x5 floors with fire/poison/acid/freeze/pitfall traps, random loot/enemy layouts, stair up/down/locked variations, key placement/consumption, and poison/freeze timers that persist between turns.
   - Grid tiles are reusable view holders with sprite+HP overlays (see `item_tile.xml`, `GameActivity.bindTileView`); `renderGrid` only rebinds dirty tiles, and a handler tick updates animated frames for active tiles (player + revealed enemies).
   - Terrain assignment per floor and monster family/affinity weighting now influence encounter selection and apply additional poison/freeze hazard hooks during combat turns (`app/src/main/java/com/example/clickdungeon/GameActivity.java:716`, `app/src/main/java/com/example/clickdungeon/GameActivity.java:771`, `app/src/main/java/com/example/clickdungeon/GameActivity.java:2559`, `app/src/main/java/com/example/clickdungeon/model/TerrainType.java:3`, `app/src/main/java/com/example/clickdungeon/model/MonsterFamily.java:3`, `app/src/main/java/com/example/clickdungeon/model/MonsterAffinity.java:3`).
   - Terrain visuals are now wired to gameplay progression in `GameActivity`: floor terrain selection updates the game-root background using terrain images (drawable-nodpi), and covered-tile interaction re-applies the current terrain backdrop.

2. **Combat loop & class abilities**
   - `CombatDialogFragment.java:160-640` provides attack/potion/flee options, monster intent telegraphs, summary logging, XP/gold payouts, and now hero/monster animation panels with HP bars. The handler-driven frame loop resets on dialog dismissal to prevent leaks.
   - `GameActivity.java:940-1150` enforces the ability system (five abilities per class at levels 1/5/10/15/20), shared cooldown, and three-tile targeting radius where applicable.

3. **Persistence, inventory, and achievements**
   - `SaveManager.java`, `InventoryManager.java`, and `AchievementManager.java` manage four save slots, gold/items, and unlocks. `ContinueActivity` routes both "New Game" and "Continue" flows, ensures overwrite confirmation, and passes slot metadata to `GameActivity`.
   - Achievements and the shop UI remain active (`AchievementsActivity.java`, `ShopActivity.java`); purchases adjust gold/items via `InventoryManager` and reflect immediately in the HUD.

4. **Settings, onboarding, and feedback**
   - `SettingsActivity.java` exposes audio/vibration/difficulty/color-blind/tutorial toggles that propagate through `SettingsManager`. `OnboardingManager` respects these flags when showing contextual tips.
   - `ClickDungeonApp.java` initializes SoundManager on startup, registers lifecycle callbacks, and pauses/resumes sound streams whenever activities move between foreground/background.

5. **Automated tests**
   - `CombatDialogFragmentTest.java` verifies potions/flee/victory flows and now asserts animation bitmaps + sound playback when attacks fire.
   - `GameActivityTileViewTest.java` covers tile binding behaviour (hidden tiles, enemy tiles with HP bars, hero overlay).
   - Utility suites (e.g., `InventoryManagerTest`, `SettingsActivityTest`, `DungeonGeneratorTest`, `GameActivitySaveIntegrationTest`) continue to guard persistence and configuration logic, with added model/adapter/activity coverage.
   - Model tests cover monster family/affinity parsing, terrain parsing, and animation clone metadata.

## Current Gaps & Technical Debt
1. **Audio coverage audit.** Exploration, trap, and ability flows call `SoundManager` with fallbacks/logging; verify every remaining event (new abilities, future classes) has a cue and keep logging missing keys so QA can spot packaging gaps.
2. **Inventory & economy UX polish.** The inventory change log now appears in the dialog/activity, but it lacks richer formatting and filtering. Expand item tooltips, add clearer sell/buyback confirmations in the merchant flow, and consider a dedicated inventory change log panel for long sessions. Shop prices/stock already come from `GameBalance` + `shop_items.json`; the debug-only reload helper exists but needs a release-safe workflow for live tuning.
3. **Testing gaps.** Coverage exists for abilities, grid animation throttling, and audio diagnostics; remaining gaps are merchant buyback edge cases, premium store delivery regressions, and instrumentation tests for sound/animation on-device.
4. **Persistence/security follow-ups.** Schema/checksum validation, backup recovery, schema-mismatch review prompts, encrypted-prefs failure policy, key rotation, and restore/mismatch telemetry hooks are implemented. Remaining work is deciding if/when to migrate to Room as the data model expands beyond SharedPreferences.
5. **Build/test friction.** CI runs `./gradlew test` on JDK 17, and the repo now includes clearer Windows guidance plus `scripts/run_online_tests.ps1`. Remaining work is triaging real online-backlog failures and deciding whether a dedicated `testOnlineDebugUnitTest` task is still needed.
6. **Premium currency roadmap.** Platinum now powers a placeholder premium store (no IAP); define the future loop for real purchases, premium-only items, and balancing between gold/platinum.

### Audio coverage checklist (keep updated)
- Tile reveal (safe/empty/loot): `effect_treasure` or class move fallback
- Trap triggers (fire/acid/poison/freeze/pitfall): `effect_trap` or trap-specific keys if added
- Class abilities: fireball (`wizard_attack`), scan (`thief_move` fallback), shield (`knight_defend`)
- Combat: attack/flee/potion class keys, monster intent/attack/defend with fallback to `player_<action>`
- Meta: victory/defeat dialogs (`effect_victory`/`effect_defeat`), level-up (`effect_level_up`), stair unlock/key pickup (`effect_key_pickup`), shop purchase (`effect_shop_purchase`), inventory gain/loss (`effect_inventory`), equip (`effect_equip`)
- Diagnostics: missing keys are captured in `SoundManager.getMissingKeys()` and viewable via Audio Diagnostics (Settings toggle).
- Sanity test: iterate all registered keys and call `SoundManager.playAndReport(key)` asserting the test playback listener fires; log missing keys to catch packaging errors early.

## Implementation Phases (ordered by dependency and effort)
### Phase 1 - Inventory/Economy UX (short, UI-first)
- [x] Inventory dialog: add visible change-log section (last N entries), clearer counts, and empty-state polish.
- [x] InventoryActivity: mirror the same counts/change-log so main-menu access stays consistent.
- [x] Shop tuning helper: add a dev-only screen or debug menu action to reload `shop_items.json` and preview prices/stock from `GameBalance`.
- [x] Tests: Robolectric coverage for change-log visibility, count labels, and tuning helper reload behavior.

### Phase 2 - Testing & Tooling Foundations (short, infra-first)
- [x] Create a lightweight local test target (e.g., `./gradlew testDebugUnitTest` shortcut script).
- [x] Add a minimal CI workflow that runs `./gradlew test` on JDK 17 (Linux).
- [x] Add test scaffolding guidelines (README) for new abilities and shop/inventory changes.
- [x] Extend grid animation throttling tests for any new animation additions.

### Phase 3 - Audio Coverage Audit (medium, content + QA loop)
- [x] Build a missing-cue checklist from `SoundManager` keys and actual call sites.
- [x] Add a dev toggle or diagnostic log view that dumps missing cues per session.
- [x] Add a smoke test that calls `SoundManager.playAndReport` for all registered keys.
- [x] Update the audio coverage checklist once audited.

### Phase 4 - Premium Store Groundwork (medium-high, new flows)
- [x] Add a platinum store shell (UI + inventory list + buy button) using a placeholder catalog.
- [x] Define premium items in JSON (parallel to `shop_items.json`).
- [x] Add purchase stubs (no real IAP) and persist platinum spend.
- [x] Add tests for spend/insufficient funds and item delivery.

### Phase 5 - Boss/Elite Encounters (high)
- [x] Define boss floor cadence and a boss encounter data model.
- [x] Implement boss encounter flow in `GameActivity` + `CombatDialogFragment` (phased abilities + telegraphs).
- [x] Add boss rewards and achievement hooks.
- [x] Add tests for boss encounter triggers and win/loss outcomes.

### Phase 6 - Class & Progression Expansion (high)
- [ ] Add one new class or perk track scaffolding.
- [ ] Extend class ability metadata and ability selection UI.
- [ ] Add balance hooks in `GameBalance`.
- [ ] Add tests for new class unlocks, ability targeting, and cooldowns.

### Phase 7 - Monetization & Live Ops (high)
- [ ] Introduce IAP interface stubs and remote config hook points.
- [ ] Add event logging/analytics wrapper (no vendor lock-in).
- [ ] Add daily/weekly challenge rules and reward hooks.

### Phase 8 - UX Polish & Localization (high, cross-cutting)
- [x] Core strings externalized and accessibility labels in place (color-blind mode, tile descriptions).
- [ ] Expand animation states and add haptic/particle FX.
- [ ] Accessibility improvements (content descriptions, tap targets, focus order).
- [ ] Localization scaffolding (locale folders, translation workflow).

### Phase 9 - Connected Services (highest)
- [ ] Abstract save system for cloud sync (Room or cloud provider later).
- [ ] Add leaderboard/challenge endpoints and offline queueing.
- [ ] Add conflict resolution and last-known-good recovery.

## Known Risks & Code-Quality Notes
- **Sound asset resilience:** `SoundManager.register()` guards against missing `.ogg` files but still relies on QA to monitor missing-key logs.
- **Handler lifecycle:** `GameActivity` and `CombatDialogFragment` stop their animation handlers on pause/destroy and only iterate active tiles; keep an eye on churn if the animated set grows beyond 5x5.
- **SharedPreferences storage:** Encrypted prefs are used on API 23+ with fallback; integrity checks and backup restore are already in place. Consider adding clearer reset or migration prompts for tamper detection.

_Next audit:_ after integrating inventory UI improvements and additional test coverage (target early 2026 or once the above Near-Term actions are complete).
