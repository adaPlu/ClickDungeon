# ClickDungeon Development Roadmap

_Audit date: 2025-11-16_

This document reflects the **verified** state of the Java/Android code under `app/src/main/java/com/example/clickdungeon`. Each section ties observed behavior back to concrete files or tests so it is clear which features are real, which are partial, and what comes next.

## Project Snapshot
- Android app targeting SDK 35 using Kotlin Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`) and Java activities for the main menu (`MainMenuActivity.java`), save-slot selection (`ContinueActivity.java`), class setup, dungeon gameplay, shop, achievements, and settings screens.
- Core loop lives in `GameActivity.java` where `DungeonGenerator.java` builds 5x5 floors populated with `Tile`/`TileType` objects (traps, enemies, gold, keys, stairs) and where progression counters (`safeTilesToReveal`, XP, gold) are enforced.
- Combat is delivered via `ui/CombatDialogFragment.java`, exposing attack/potion/flee options, intent previews, and summary logging while delegating XP/gold calculations to `util/GameBalance.java`.
- Persistence is consolidated behind `util/SaveManager.java` (four slots stored in `SaveSlotX` SharedPreferences), `util/InventoryManager.java` (items & gold), and `util/AchievementManager.java` (SharedPreferences + Gson). `MainMenuActivity` now routes both “New Game” and “Continue” through `ContinueActivity` with overwrite-safe flows.
- Meta/UX systems include `SettingsActivity` + `SettingsManager` for audio/vibration/difficulty/color-blind toggles, `OnboardingManager` for first-run tutorials, `FeedbackManager`/`SoundManager` hooks (partially unused), and `ShopActivity` for mock purchases.
- Regression coverage: Robolectric tests under `app/src/test/java/com/example/clickdungeon` validate SaveManager interactions (`GameActivitySaveIntegrationTest`), inventory/achievement utilities, settings UI, combat dialog behavior, and new key-pickup scenarios.

## Confirmed Feature Coverage

### Core gameplay & persistence
- **Dungeon exploration** (`GameActivity.java:323-640`, `DungeonGenerator.java:21-90`): supports random trap types (fire/poison/acid/freeze/pitfall), multi-floor stair up/down, pitfall drops, safe-tile tracking, and poison/freeze status effects.
- **Combat loop** (`CombatDialogFragment.java`, `GameActivity.java:700-850`): enemies scale by floor, telegraph intents, support potions/flee penalties, and reward XP/gold through `GameBalance`.
- **Multi-slot saves** (`MainMenuActivity.java`, `ContinueActivity.java`, `GameActivity.java:120-214`, `util/SaveManager.java`): four slots can be created, overwritten, or resumed via `ContinueActivity.EXTRA_FORCE_NEW_GAME`, and `GameActivity` auto-persists on pause.
- **Key & lock progression** (`DungeonGenerator.java:40-75`, `GameActivity.java:566-637`, `InventoryManager.java`): keys include floor labels, feed into the shared inventory, and locked stairs consume the matching key item before permitting descent.
- **Class abilities** (`GameActivity.java:120-940`): Wizard fireballs, Thief trap scans, and Knight shields share a two-floor cooldown and three-tile range, giving each class a tactical utility beyond raw stats.
- **Player feedback & settings** (`SettingsActivity.java`, `OnboardingManager.java`, `FeedbackManager.java`): difficulty, audio, vibration, color-blind mode, and tutorial hints are persisted and referenced throughout gameplay (tile glyph suffixes, tutorials, vibration cues).
- **Achievements, shop, and meta UI** (`AchievementsActivity.java`, `ShopActivity.java`, adapters/models): achievements load via `AchievementManager`, the shop adjusts gold/items through `InventoryManager`, and navigation between activities is wired from the main menu.

### Supporting infrastructure
- Shared models for characters (`CharacterProfile`), monsters (including animated subclasses), tiles, achievements, shop items, and inventory entries exist and are used consistently.
- `SoundManager.java` and `AnimatedPlayer`/`AnimatedMonster` provide ready-to-use audiovisual hooks even though they are not yet integrated into the UI.
- Automated tests cover persistence helpers, onboarding prompts, save/load flows, and now key pickup, giving confidence in regression-sensitive areas.

## Current Gaps & Technical Debt
1. **Animations and advanced audio are unused.** `CharacterProfile` exposes `AnimatedPlayer`, `MonsterFactory` builds `AnimatedMonster`, and `SoundManager` loads sprite/audio assets, but nothing renders or plays them. Combat and exploration still rely on emoji glyphs and ToneGenerator cues (`AnimatedPlayer.java`, `AnimatedMonster.java`, `SoundManager.java`).
2. **Economy & inventory UX is minimal.** The shop sells a static mock list, gold adjustments happen silently, there is no in-run inventory screen, and dungeon rewards besides gold/keys do not surface. Balance knobs (`GameBalance.java`) exist but lack tooling/tests around tuning.
3. **Testing gaps remain.** While key pickups and save flows are covered, there are no automated tests for the refreshed ability button, trap scanning targets, or inventory/shop interactions.

## Near-Term Roadmap (next 1-2 iterations)
1. **Integrate animations & richer feedback.** Instantiate `AnimatedPlayer`/`AnimatedMonster` sprites in the dungeon and combat screens, pipe actions through `SoundManager`, and provide graceful fallbacks for devices lacking the assets.
2. **Improve inventory/economy UX.** Add a lightweight inventory view (from the main menu and/or in-run pause), inform players when items (Trap Disarm Kits, keys, consumables) are granted or consumed, and expand shop offerings tied to `GameBalance`.
3. **Expand automated coverage.** Add tests around the new class abilities, shop transactions, and inventory display so regressions in core progression and economy flows are caught early.

## Longer-Term Milestones
- **Boss floor launch.** Scripted boss encounters, multi-phase behaviors, and unique loot pacing to cap each run.
- **Class & progression overhaul.** Additional classes or perk trees, reworked achievements, and advanced builds once core abilities are stable.
- **Monetization & live ops.** Cosmetic/IAP hooks, rewarded ads, analytics/remote config, and data-driven tuning layered atop the hardened economy.
- **UX polish & localization.** Visual updates, animation polish, deeper accessibility, richer haptics/audio, and externalized strings for translation.
- **Connected services.** Cloud saves, leaderboards, rotating challenges, and social features after local persistence and live ops are dependable.
