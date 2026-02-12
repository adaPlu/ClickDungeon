# ClickDungeon - Android Dungelot-Inspired Roguelite

**ClickDungeon** is a premium Android dungeon-crawler inspired by the Dungelot series, developed in Java using Android Studio. Players explore layered floors of a hidden-tile dungeon, collect gold, unlock achievements, and use special class abilities to survive and progress. Platinum is stored and can be spent in a placeholder premium store (no real IAP yet). Designed for commercial use with built-in expansion points for monetization and further gameplay depth.

---

## Key Features

1. **Grid-Based Dungeon Exploration**
   - 5x5 dungeon grid with hidden tiles and multiple floor levels.
   - Traps, enemies, and treasure are shuffled each time, creating replay value.
   - Grid rendering updates only dirty tiles, and animation ticks focus on active tiles (player/revealed enemies) to reduce UI overhead.

2. **Class Selection & Abilities**
   - **Knight** (L1/5/10/15/20): Shield Wall, Taunt, Fortify, Valiant Strike, Guardian's Oath.
   - **Thief** (L1/5/10/15/20): Trap Scan, Shadowstep, Disarm Expert, Ambush, Veil of Smoke.
   - **Wizard** (L1/5/10/15/20): Fireball, Frost Nova, Chain Lightning, Arcane Shield, Meteor.
   - Ability effects currently map to legacy gameplay hooks (trap reveals/clears, ambush/taunt combat hooks, smoke veil trap avoidance) while unique per-ability behavior is phased in.
   - Abilities share a two-floor cooldown and a three-tile targeting radius when applicable.
   - Wizard abilities consume MP (costs vary by spell).

3. **Traps & Status Effects**
   - Fire, acid, poison, freeze, and pitfall traps.
   - Freeze stops you for multiple turns; poison deals damage over time.
   - Pitfall traps drop you deeper into the dungeon (multi-floor exploration).

4. **Terrain Types & Creature Effects**
   - **Cavern**: Beasts/humanoids are favored; monsters gain +1 DEF and ranged attacks deal -1 ATK.
   - **Crypt**: Undead are favored; undead gain +10% ATK.
   - **Lava Field**: Fire-affinity monsters are favored; fire gains +15% DEF and non-fire attacks can inflict poison.
   - **Mire**: Beasts/elementals are favored; beasts gain +1 ATK and attacks have a 15% poison chance.
   - **Frozen Ruins**: Undead/constructs are favored; ice gains +10% DEF and non-ice attacks have a 15% freeze chance.
   - **Thorn Wilds**: Beasts are favored; beasts gain +1 DEF and attacks have a 10% poison chance.
   - **Storm Plateau**: Arcane/constructs are favored; lightning gains +1 ATK and attacks have a 10% freeze chance.
   - **Arcane Nexus**: Arcane is favored; arcane gains +1 ATK and non-arcane lose 1 DEF.
   - **Sunken Temple**: Beasts/elementals are favored; fire-affinity attacks deal 10% less ATK.
   - **Ash Wastes**: Demonic/elemental are favored; elementals gain +5% ATK and attacks have a 10% poison chance.

5. **Inventory & Currency**
   - Persistent gold, platinum, and inventory stored via SharedPreferences.
   - New runs start with 100 platinum and 50 gold; gold is earned in the dungeon.
   - Platinum is a premium placeholder currency with a shell store (spendable, no real IAP yet).
   - Stat point allocation is available from the inventory screen and the in-run inventory dialog; a **Level Up** button appears when points are available. MP is shown in the HUD for MP-using classes.
   - Items like **Trap Disarm Kits** prevent trap damage.
   - Player can acquire items through the **Shop**. A merchant can appear every three floors (50% chance) with sell and buyback lists; all prices use gold.

6. **Multiple Floors**
   - Pitfall traps and stairs cause you to advance downward.
   - Survive deeper floors with increased challenge and better rewards.

7. **Achievements & Progress**
   - Collect multiple achievements (e.g., First Blood, Low HP Survivor).
   - Earn them by uncovering tiles, winning with minimal HP, or reaching boss floors.
   - Achievements stored and displayed in **AchievementsActivity**, with Locked/Completed status shown in the list.

8. **Game Over & Victory**
   - You lose if your HP drops to 0.
   - Reveal all safe tiles on a floor to claim victory and proceed to the next.
   - Automatic saving across four slots lets you resume each run from the Continue or New Game slot selector, with overwrite prompts and corrupted-slot handling.

9. **Interactive Combat Encounters**
    - Turn-based combat dialog with attack, potion, and flee options.
    - Enemies telegraph upcoming attacks with animated intent bars and contextual combat summaries that call out turns, damage dealt/taken, and potion usage on victory, retreat, or defeat.
    - Monsters scale per floor, drawing from a template pool with unique emoji and damage variance.
    - Boss floors (5/10/15) spawn a boss with phased intent scaling and bonus rewards.
    - Animated combatants use `AnimatedPlayer`/`AnimatedMonster` sprite sheets with class/monster audio cues.

10. **Modular Architecture**
    - **SharedPreferences** used throughout for saving grid states, gold/platinum, and achievements (schema + checksum validation with backup recovery; Tink + Android Keystore encrypted prefs on API 23+ with fallback; see `SECURITY.md` for details).
    - Save writes are queued off the UI thread and coalesced; pause and critical events trigger immediate save jobs.
    - Separate activities for each feature (shop, achievements, settings, etc.).
    - Clean codebase supports expansions like IAP, ads, new classes, and more.

11. **Customizable Experience**
    - Settings screen controls audio, vibration, and dungeon difficulty; vibration uses VibrationEffect on API 26+ with a legacy fallback.
    - Tone and haptic feedback respect player preferences through a shared manager.
    - Difficulty tuning scales monster stats and trap lethality across floors.
    - Dungeon-themed backgrounds and panel styling are applied across menu screens.
    - Audio diagnostics toggle in Settings can surface missing cue keys in a diagnostics screen.

12. **Accessibility & Onboarding**
    - Color-blind mode adds letter codes to emoji tiles and enriches screen reader descriptions.
    - First-run tutorial surfaces difficulty-aware tips that respect player preferences.

13. **Automated Verification**
    - Robolectric suites cover settings UI, inventory, achievements, save slots, balance, onboarding, feedback, and combat flows.
    - Expanded unit tests now cover model classes, adapters, shop flows, animation helpers, terrain/affinity helpers, item catalog/merchant/loot roll helpers, persistence store/security helpers, class ability behaviors (range/cooldown/effects), and save snapshot/coalesced persistence behavior.
    - SoundManager smoke tests ensure all registered audio keys are exercised in unit tests (Robolectric SDK 34 via `robolectric.properties`).

---

## Technologies Used

- **Java (Android SDK)**
- **Android Studio**
- **SharedPreferences** for persistent state
- **Gson** for JSON serialization
- **RecyclerView** for item listing (Shop & Achievements)

---

## Project Structure

```bash
ClickDungeon/
+-- app/
|   +-- java/com/example/clickdungeon/
|   |   +-- MainMenuActivity.java       # Main menu & navigation
|   |   +-- ClassSelectionActivity.java # Choose class (Knight, Thief, Wizard)
|   |   +-- GameActivity.java           # Core dungeon gameplay with multi-floor logic
|   |   +-- ShopActivity.java           # Purchasing items & managing currencies
|   |   +-- AchievementsActivity.java   # Viewing unlocked achievements
|   |   +-- SettingsActivity.java       # Basic settings screen
|   |   +-- InventoryActivity.java      # Inventory, equipment, and stat allocation
|   |   +-- model/
|   |   |   +-- CharacterProfile.java
|   |   |   +-- InventoryItem.java
|   |   |   +-- ShopItem.java
|   |   |   +-- Achievement.java
|   |   |   +-- ItemDefinition.java
|   |   |   +-- PricedItem.java
|   |   |   +-- Tile.java
|   |   |   +-- TileType.java
|   |   |   +-- PlayerClass.java
|   |   +-- util/
|   |   |   +-- SaveManager.java
|   |   |   +-- InventoryManager.java
|   |   |   +-- AchievementManager.java
|   |   |   +-- ItemCatalog.java
|   |   |   +-- MerchantManager.java
|   |   |   +-- MonsterLootRoll.java
|   |   +-- adapter/
|   |   |   +-- ShopItemAdapter.java
|   |   |   +-- AchievementAdapter.java
|   |   |   +-- InventoryAdapter.java
|   |   |   +-- PricedItemAdapter.java
|   +-- res/
|       +-- layout/
|       |   +-- activity_main_menu.xml
|       |   +-- activity_class_selection.xml
|       |   +-- activity_game.xml
|       |   +-- activity_shop.xml
|       |   +-- activity_achievements.xml
|       |   +-- activity_settings.xml
|       |   +-- item_tile.xml
|       |   +-- ...
|       +-- values/
|       |   +-- strings.xml
|       |   +-- ...
+-- build.gradle.kts
+-- settings.gradle.kts
+-- AndroidManifest.xml
```

---

## How to Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/adaplu/ClickDungeon.git
   ```
2. **Open in Android Studio**
   Go to **File > Open**, and select the `ClickDungeon` folder.
3. **Build & Run**
   Deploy on an emulator or physical Android device.

---

## Progress Checklist & Implementation Phases

### Implementation Phases (summary)
- [x] Phase 1 - Class reset + level cap (20-level cap and base class ability kits).
- [x] Phase 2 - Stat system foundation (STR/INT/CON/DEX; HP/MP derived; ATK/DEF derived).
- [x] Phase 3 - Base class kits (base stats and base abilities per class).
- [x] Phase 4 - Level progression (20 levels, stat points per level, HP/MP scaling).
- [x] Phase 5 - Class abilities (five abilities per class at levels 1/5/10/15/20 with cooldown/range rules).
- [x] Phase 6 - Ability + stat tuning (current tuning constants in CharacterProfile).
- [x] Phase 7 - Core economy + persistence (gold + platinum; save/load; shop/merchant).
- [x] Phase 8 - Merchant access + cadence (shop access, merchant visits, buyback scaffolding).
- [x] Phase 9 - XP system + rewards (floor clear, items found, trap disabled, monster defeats).
- [x] Phase 10 - Keys, chests, and exit flow (small keys, chests, locked stairs, chest loot).
- [x] Phase 11 - Loot tables + itemization (monster loot, weapon/armor tiers, magic affixes).
- [x] Phase 12 - Equipment tracking + bonuses (equip/unequip, bonuses tracked; combat uses base + equipment bonuses).
- [ ] Phase 13 - UX polish + additional audio/animation states (remaining polish and accessibility tuning).

### Feature Checklist
- [x] Multi-floor dungeon with pitfall traps and stairs.
- [x] Class selection (Knight, Thief, Wizard) with base kits and stat progression.
- [x] Traps & status effects (freeze, poison).
- [x] Terrain-based encounter weighting and status hooks.
- [x] Trap Disarm Kit item integration.
- [x] Achievements UI & unlocking logic.
- [x] Inventory & currency system with shop and merchant buyback.
- [x] Key/stair integration for big-key exits and small-key chests.
- [x] Combat with various monsters.
- [x] Animated combatants and grid sprite updates.
- [x] Expanded Robolectric/unit test coverage for models, adapters, and flows.
- [x] Boss floors (phased encounters) with rewards and tests.
- [ ] Advanced enemy AI (planned).
- [ ] Additional classes and abilities (planned).

---

## Roadmap

See `docs/ROADMAP.md` for the current, detailed roadmap and milestone tracking. The detailed phase checklist is maintained there to avoid drift.

---

## License

**Commercial License**
This project is licensed for commercial use. Redistribution, sublicensing, or modification is allowed only under explicit written permission from the author.

> Copyright 2025 ClickDungeon Studios. All rights reserved.
> Contact: `ClickDungeon@gmail.com`

## Local development & testing
- Install Android SDK with platform 36 and ensure `sdk.dir` in `local.properties` points to it (see `local.properties.example`).
- Robolectric tests can be run with `./gradlew test` (SDK 34 pinned via `app/src/test/resources/robolectric.properties`); Android Studio or the Gradle daemon will reuse the configured SDK and cached dependencies.
- For a lightweight local target, run `scripts/testDebugUnitTest.ps1` (Windows PowerShell) to execute `testDebugUnitTest`.
- If running in a restricted network environment, pre-seed the Gradle wrapper and Android SDK offline to avoid proxy download failures during CI.

### Test scaffolding guidelines
- Add unit tests for new abilities in both targeting (range/cooldown) and effect execution (damage/heal/status).
- When changing inventory or shop UX, include a Robolectric test that asserts labels, counts, and any change-log entries.
- For new animations or grid effects, extend `GameActivityGridAnimationThrottleTest` to confirm active tiles are tracked and updated correctly.
