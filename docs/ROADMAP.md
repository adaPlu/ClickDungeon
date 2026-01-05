# ClickDungeon Development Roadmap

_Audit date: 2025-11-18_

This roadmap documents the verified state of the ClickDungeon Android project (Java sources under `app/src/main/java/com/example/clickdungeon`). Each assertion references concrete files so future work stays grounded in reality.

## Project Snapshot
- **Platform & build:** Android app targeting SDK 35, Kotlin-based Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`), and Java activities/fragments for every screen (Main Menu, Continue, Class Selection, Game, Shop, Achievements, Settings). Save-slot metadata lives in `util/SaveManager.java`.
- **Core loop:** `GameActivity.java` orchestrates dungeon generation (`DungeonGenerator.java`), tile metadata (`Tile.java`/`TileType.java`), trap/status resolution, class abilities, inventory, gold, XP, and save persistence. Shared managers (`InventoryManager`, `AchievementManager`, `GameBalance`) keep the meta-layer consistent.
- **Combat & feedback:** `ui/CombatDialogFragment.java` owns combat turns, XP/gold previews, summary logs, and now renders dedicated player/monster animation slots driven by `AnimatedPlayer` and `AnimatedMonster` (frame loop + SoundManager hooks). The dungeon grid also animates revealed enemies and the hero via a throttled handler loop.
- **Audio & settings:** `util/SoundManager.java` loads class/monster/effect cues once at app startup (`ClickDungeonApp.java`) with graceful fallbacks when a raw asset is absent and logs missing keys. `FeedbackManager` uses the same SoundPool and respects settings from `SettingsManager`. Audio/vibration/difficulty/color-blind preferences wire directly into `SettingsActivity`.
- **Persistence/UI surface:** Inventory, achievements, onboarding tips, and shop purchases are shared across runs via SharedPreferences + Gson. Multi-slot save/continue flows use `ContinueActivity` and `GameActivity` auto-persistence on pause.
- **Tests:** Robolectric suites in `app/src/test/java` cover SaveManager integration, CombatDialog interactions (now including animation/sound behavior), Settings UI, inventory/achievement helpers, dungeon generator, and tile binding for the new grid visuals. `robolectric.properties` pins SDK 34 for deterministic runs (local JDK 17 still required).

## Confirmed Feature Coverage
1. **Dungeon exploration & status effects**
   - `GameActivity.java:323-1180` + `DungeonGenerator.java:21-102` implement 5×5 floors with fire/poison/acid/freeze/pitfall traps, random loot/enemy layouts, stair up/down/locked variations, key placement/consumption, and poison/freeze timers that persist between turns.
   - Grid tiles are reusable view holders with sprite+HP overlays (see `item_tile.xml`, `GameActivity.bindTileView`), and a throttled handler tick updates animated frames for visible enemies and the hero.

2. **Combat loop & class abilities**
   - `CombatDialogFragment.java:160-640` provides attack/potion/flee options, monster intent telegraphs, summary logging, XP/gold payouts, and now hero/monster animation panels with HP bars. The handler-driven frame loop resets on dialog dismissal to prevent leaks.
   - `GameActivity.java:940-1150` ensures Wizard fireball, Thief scan, and Knight shield share a two-floor cooldown, enforce the three-tile targeting radius, and integrate with inventory/shield metadata throughout a run.

3. **Persistence, inventory, and achievements**
   - `SaveManager.java`, `InventoryManager.java`, and `AchievementManager.java` manage four save slots, gold/items, and unlocks. `ContinueActivity` routes both “New Game” and “Continue” flows, ensures overwrite confirmation, and passes slot metadata to `GameActivity`.
   - Achievements and the shop UI remain active (`AchievementsActivity.java`, `ShopActivity.java`); purchases adjust gold/items via `InventoryManager` and reflect immediately in the HUD.

4. **Settings, onboarding, and feedback**
   - `SettingsActivity.java` exposes audio/vibration/difficulty/color-blind/tutorial toggles that propagate through `SettingsManager`. `OnboardingManager` respects these flags when showing contextual tips.
   - `ClickDungeonApp.java` initializes SoundManager on startup, registers lifecycle callbacks, and pauses/resumes sound streams whenever activities move between foreground/background.

5. **Automated tests**
   - `CombatDialogFragmentTest.java` verifies potions/flee/victory flows and now asserts animation bitmaps + sound playback when attacks fire.
   - `GameActivityTileViewTest.java` covers tile binding behaviour (hidden tiles, enemy tiles with HP bars, hero overlay).
   - Utility suites (e.g., `InventoryManagerTest`, `SettingsActivityTest`, `DungeonGeneratorTest`, `GameActivitySaveIntegrationTest`) continue to guard persistence and configuration logic.

## Current Gaps & Technical Debt
1. **Audio coverage audit.** Exploration, trap, and ability flows call `SoundManager` with fallbacks/logging; verify every remaining event (new abilities, future classes) has a cue and keep logging missing keys so QA can spot packaging gaps.
2. **Inventory & economy UX remains barebones.** Ship an in-run inventory drawer/dialog in `GameActivity` showing gold, keys, trap kits, potions, and a short change log; show snackbars/toasts on gain/use. Wire shop prices/stock to `GameBalance` and a JSON seed (e.g., `shop_items.json`); on purchase, show success/error feedback and decrement stock. Add a small tuning helper (script or dev-only screen) to reload JSON and preview prices/stock without rebuilding. Tests: inventory gain/use updates UI and persists; shop purchase adjusts gold/stock and emits feedback; JSON load falls back safely when missing/invalid.
3. **Testing gaps.** Add cases for ability targeting radius/cooldown enforcement; trap-scan coverage across varied floor layouts; shop price/stock decrement; inventory add/remove consuming keys/trap kits; grid animation loop advances frames and pauses on detach (fake time progression).
4. **Persistence/security trade-offs.** Saves/inventory/achievements are plain SharedPreferences + JSON with no schema versioning, integrity, or encryption. Add `schemaVersion` + checksum/HMAC per blob, validate on load with migrate/reset prompts, keep a “last-known-good” backup, and consider `EncryptedSharedPreferences` or Room with encrypted columns. Centralize prefs access and log validation/encryption failures.
5. **Build/test friction.** Document JDK 17 + Windows console setup and provide a “getting tests to run locally” snippet. Wire CI (e.g., GitHub Actions) to run `./gradlew test` on Linux with JDK 17. If locals must skip heavy suites, add a lightweight target or profile Robolectric suites with shell guidance.

### Audio coverage checklist (keep updated)
- Tile reveal (safe/empty/loot): `effect_treasure` or class move fallback
- Trap triggers (fire/acid/poison/freeze/pitfall): `effect_trap` or trap-specific keys if added
- Class abilities: fireball (`wizard_attack`), scan (`thief_move` fallback), shield (`knight_defend`)
- Combat: attack/flee/potion → class keys; monster intent/attack/defend → `<monster>_<action>` with fallback to `player_<action>`
- Meta: victory/defeat dialogs (`effect_victory`/`effect_defeat`), level-up (needs key), stair unlock/key pickup (needs key), shop purchase (`effect_positive`), inventory gain/loss (needs key)
- Sanity test: iterate all registered keys and call `SoundManager.play(key)` asserting the test playback listener fires; log missing keys to catch packaging errors early.

## Near-Term Action Plan (next 1–2 iterations)
1. **Inventory/economy UX improvements**
   - Introduce an inventory dialog/drawer accessible from `GameActivity` and the main menu that lists gold, keys, consumables, and class kits. Show contextual toasts/snackbars when loot is acquired or expended.
   - Expand `ShopActivity` to pull dynamic prices/stock from `GameBalance`, add purchase-result feedback, and seed balances via JSON/config plus a lightweight editor or seed data.

2. **Testing & tooling**
   - Add Robolectric coverage for class abilities (range checks, shield persistence, scan outcomes), shop purchase flows, inventory state changes, and SoundManager invocations in the overworld; include grid animation handler tests with faked frame/time progression.
   - Document or script the JDK 17 + console prerequisites, add a lightweight/local test target if heavy suites are skipped, and wire CI (e.g., GitHub Action) that runs `./gradlew test` on Linux with JDK 17.
   - Missing test cases to add immediately: ability targeting radius and cooldown enforcement; trap-scan coverage on varied floor layouts; shop purchase price/stock decrements; inventory add/remove consumes keys/trap kits; grid animation loop advances frames and pauses on detach.

3. **Persistence hardening**
   - Add metadata/versioning to `SaveManager` records to detect incompatible saves. Add validation on load, checksum/signature for tamper detection, and optional encryption (e.g., EncryptedSharedPreferences) or lightweight Room if commercial builds demand it.

## Mid-Term Milestones
1. **Boss/elite encounters** — Scripted floors with multi-phase enemies, bespoke loot pacing, and achievement hooks once core combat/animation loops are fully reliable.
2. **Class & progression depth** — New classes or perk trees, expanded achievements, and balance passes informed by the upcoming inventory/shop telemetry.
3. **Monetization & live ops** — Cosmetic/IAP scaffolding, rewarded ads, analytics hooks (Firebase/Remote Config), and seasonal dungeon modifiers after economy UX is production-ready.
4. **UX polish & localization** — Additional animation states, haptic/particle FX, richer accessibility cues, and externalized strings/assets for translation.
5. **Connected services** — Cloud saves, leaderboards, challenge modes, and social features once persistence, security, and telemetry foundations are in place.

## Known Risks & Code-Quality Notes
- **Sound asset resilience:** `SoundManager.register()` guards against missing `.ogg` files but silently skips cues; logging is in place, but QA should monitor for missing keys.
- **Handler lifecycle:** `GameActivity` and `CombatDialogFragment` stop their animation handlers on pause/destroy and throttle off-screen tiles, but the grid loop still iterates every slot; monitor for ANR if grids ever grow beyond 5×5.
- **SharedPreferences storage:** All saves and inventory data are human-readable. Document this in release notes or introduce integrity checks before shipping on Play Store.

_Next audit:_ after integrating inventory UI improvements and additional test coverage (target early 2026 or once the above Near-Term actions are complete).
