# ClickDungeon Development Roadmap

_Audit date: 2025-11-18_

This roadmap documents the verified state of the ClickDungeon Android project (Java sources under `app/src/main/java/com/example/clickdungeon`). Each assertion references concrete files so future work stays grounded in reality.

## Project Snapshot
- **Platform & build:** Android app targeting SDK 35, Kotlin-based Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`), and Java activities/fragments for every screen (Main Menu, Continue, Class Selection, Game, Shop, Achievements, Settings). Save-slot metadata lives in `util/SaveManager.java`.
- **Core loop:** `GameActivity.java` orchestrates dungeon generation (`DungeonGenerator.java`), tile metadata (`Tile.java`/`TileType.java`), trap/status resolution, class abilities, inventory, gold, XP, and save persistence. Shared managers (`InventoryManager`, `AchievementManager`, `GameBalance`) keep the meta-layer consistent.
- **Combat & feedback:** `ui/CombatDialogFragment.java` owns combat turns, XP/gold previews, summary logs, and now renders dedicated player/monster animation slots driven by `AnimatedPlayer` and `AnimatedMonster` (frame loop + SoundManager hooks). The dungeon grid also animates revealed enemies and the hero via a handler-based loop.
- **Audio & settings:** `util/SoundManager.java` loads class/monster/effect cues once at app startup (`ClickDungeonApp.java`) with graceful fallbacks when a raw asset is absent. `FeedbackManager` uses the same SoundPool and respects settings from `SettingsManager`. Audio/vibration/difficulty/color-blind preferences wire directly into `SettingsActivity`.
- **Persistence/UI surface:** Inventory, achievements, onboarding tips, and shop purchases are shared across runs via SharedPreferences + Gson. Multi-slot save/continue flows use `ContinueActivity` and `GameActivity` auto-persistence on pause.
- **Tests:** Robolectric suites in `app/src/test/java` cover SaveManager integration, CombatDialog interactions (now including animation/sound behavior), Settings UI, inventory/achievement helpers, dungeon generator, and tile binding for the new grid visuals. `robolectric.properties` pins SDK 34 for deterministic runs (local JDK 17 still required).

## Confirmed Feature Coverage
1. **Dungeon exploration & status effects**
   - `GameActivity.java:323-1180` + `DungeonGenerator.java:21-102` implement 5×5 floors with fire/poison/acid/freeze/pitfall traps, random loot/enemy layouts, stair up/down/locked variations, key placement/consumption, and poison/freeze timers that persist between turns.
   - Grid tiles are now reusable view holders with sprite+HP overlays (see `item_tile.xml`, `GameActivity.bindTileView`), and a `Handler` tick updates animated frames for revealed enemies and the hero.

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
1. **Audio/animation still mid-integration.** Combat and grid sprites animate, but exploration actions (tile reveals, trap hits, ability triggers) still rely on `FeedbackManager` vibration/toasts rather than SoundManager cues. There’s no dynamic throttling—off-screen enemies animate until their view is recycled.
2. **Inventory & economy UX remains barebones.** No in-run inventory panel, no toast/snackbar when keys or trap kits are acquired/consumed, and Shop items are static with placeholder prices. `GameBalance` tuning knobs lack tooling/tests for iterative balance.
3. **Testing gaps.** Ability targeting, trap-scan coverage, shop transactions, and inventory state changes have no direct tests. Grid animation handler isn’t covered beyond the binding unit test, so regressions in frame loops would go unnoticed.
4. **Persistence/security trade-offs.** Saves/inventory/achievements use plain SharedPreferences + JSON without encryption or validation. That’s acceptable for local QA but needs documentation or mitigation before commercial release.
5. **Build/test friction.** The current environment can’t run `./gradlew test` due to console handle issues (requires Windows console + JDK 17). Until CI or local developers can verify Robolectric suites, regressions may slip by.

## Near-Term Action Plan (next 1–2 iterations)
1. **Complete audiovisual integration**
   - Route tile reveal, trap hit, ability activation, and class-specific effects through `SoundManager`; add fallbacks when assets are missing (the new `register()` guard prevents crashes, but we still need user-facing cues).
   - Pause grid animation frames when tiles are off-screen or invisible; consider battery-friendly throttling (e.g., animate only the hero tile and the currently selected enemy).

2. **Inventory/economy UX improvements**
   - Introduce an inventory dialog accessible from `GameActivity` and the main menu that lists gold, keys, consumables, and class kits. Show contextual toasts/snackbars when loot is acquired or expended.
   - Expand `ShopActivity` to pull data from `GameBalance`, add limited-time offers, and write tests verifying price/stock logic.

3. **Testing & tooling**
   - Add Robolectric coverage for class abilities (range checks, shield persistence, scan outcomes), shop purchase flows, and SoundManager invocations in the overworld.
   - Document or script the JDK 17 + console prerequisites so contributors can run `./gradlew test` reliably; consider wiring a CI job that already meets those constraints.

4. **Persistence hardening**
   - Add metadata/versioning to `SaveManager` records to detect incompatible saves. Evaluate encrypted SharedPreferences or a lightweight Room database if commercial builds demand tamper resistance.

## Mid-Term Milestones
1. **Boss/elite encounters** – Scripted floors with multi-phase enemies, bespoke loot pacing, and achievement hooks once core combat/animation loops are fully reliable.
2. **Class & progression depth** – New classes or perk trees, expanded achievements, and balance passes informed by the upcoming inventory/shop telemetry.
3. **Monetization & live ops** – Cosmetic/IAP scaffolding, rewarded ads, analytics hooks (Firebase/Remote Config), and seasonal dungeon modifiers after economy UX is production-ready.
4. **UX polish & localization** – Additional animation states, haptic/particle FX, richer accessibility cues, and externalized strings/assets for translation.
5. **Connected services** – Cloud saves, leaderboards, challenge modes, and social features once persistence, security, and telemetry foundations are in place.

## Known Risks & Code-Quality Notes
- **Sound asset resilience:** `SoundManager.register()` now guards against missing `.ogg` files, but missing keys silently disable cues. Consider logging which sounds failed to load so QA can spot packaging issues.
- **Handler lifecycle:** `GameActivity` and `CombatDialogFragment` stop their animation handlers on pause/destroy, but the grid loop still iterates over every tile even when the game is backgrounded just before pause completes. Monitor for ANR reports if the dungeon ever exceeds 5×5.
- **SharedPreferences storage:** All saves and inventory data are human-readable. Document this in release notes or introduce integrity checks before shipping on Play Store.

_Next audit:_ after integrating SoundManager cues into overworld actions + inventory UI improvements (target early 2026 or once the above Near-Term actions are complete).
