# 🧱 ClickDungeon - Android Dungelot-Inspired Roguelite

**ClickDungeon** is a premium Android dungeon-crawler inspired by the Dungelot series, developed in Java using Android Studio. Players explore layered floors of a hidden-tile dungeon, collect gold, unlock achievements, and use special class abilities to survive and progress. Designed for commercial use with built-in expansion points for monetization and further gameplay depth.

---

## 🔑 Key Features

1. **Grid-Based Dungeon Exploration**  
   - 5x5 dungeon grid with hidden tiles and multiple floor levels.  
   - Traps, enemies, and treasure are shuffled each time, creating replay value.

2. **Class Selection & Abilities**  
   - **Knight**: Higher HP for direct confrontations.  
   - **Thief**: Can scan adjacent tiles for traps.  
   - **Wizard**: Reveals all traps on demand.

3. **Traps & Status Effects**  
   - Fire, acid, poison, freeze, and pitfall traps.  
   - Freeze stops you for multiple turns; poison deals damage over time.  
   - Pitfall traps drop you deeper into the dungeon (multi-floor exploration).

4. **Inventory & Gold**  
   - Persistent gold and inventory stored via SharedPreferences.  
   - Items like **Trap Disarm Kits** prevent trap damage.  
   - Player can acquire items through the **Shop**.

5. **Multiple Floors**  
   - Pitfall traps and stairs cause you to advance downward.  
   - Survive deeper floors with increased challenge and better rewards.

6. **Achievements & Progress**  
   - Collect multiple achievements (e.g., “First Blood,” “Low HP Survivor”).  
   - Earn them by uncovering tiles, winning with minimal HP, or reaching boss floors.  
   - Achievements stored and displayed in **AchievementsActivity**.

7. **Game Over & Victory**
   - You lose if your HP drops to 0.
   - Reveal all safe tiles on a floor to claim victory and proceed to the next.
   - Automatic saving across four slots lets you resume each run from the Continue menu.

8. **Interactive Combat Encounters**
   - Turn-based combat dialog with attack, potion, and flee options.
   - Enemies telegraph upcoming attacks with animated intent bars and contextual combat summaries that call out turns, damage dealt/taken, and potion usage on victory, retreat, or defeat.
   - Monsters scale per floor, drawing from a template pool with unique emoji and damage variance.

9. **Modular Architecture**
   - **SharedPreferences** used throughout for saving grid states, gold, and achievements.
   - Separate activities for each feature (shop, achievements, settings, etc.).
   - Clean codebase supports expansions like IAP, ads, new classes, and more.

10. **Customizable Experience**
    - Settings screen controls audio, vibration, and dungeon difficulty.
    - Tone and haptic feedback respect player preferences through a shared manager.
    - Difficulty tuning scales monster stats and trap lethality across floors.

11. **Accessibility & Onboarding**
    - Color-blind mode adds letter codes to emoji tiles and enriches screen reader descriptions.
    - First-run tutorial surfaces difficulty-aware tips that respect player preferences.

12. **Automated Verification**
    - Robolectric suites cover settings (including the Settings screen UI), inventory, achievement, save-slot, balance, onboarding, feedback, and game state helpers.
    - Grid generation and reward tuning now have regression protection via deterministic tests.

---

## 🧰 Technologies Used

- **Java (Android SDK)**
- **Android Studio**  
- **SharedPreferences** for persistent state
- **Gson** for JSON serialization
- **RecyclerView** for item listing (Shop & Achievements)

---

## 📁 Project Structure

```bash
ClickDungeon/
+-- app/
¦   +-- java/com/example/clickdungeon/
¦   ¦   +-- MainMenuActivity.java       # Main menu & navigation
¦   ¦   +-- ClassSelectionActivity.java # Choose class (Knight, Thief, Wizard)
¦   ¦   +-- GameActivity.java           # Core dungeon gameplay with multi-floor logic
¦   ¦   +-- ShopActivity.java           # Purchasing items & managing gold
¦   ¦   +-- AchievementsActivity.java   # Viewing unlocked achievements
¦   ¦   +-- SettingsActivity.java       # Basic settings screen
¦   ¦   +-- model/
¦   ¦   ¦   +-- CharacterProfile.java
¦   ¦   ¦   +-- InventoryItem.java
¦   ¦   ¦   +-- ShopItem.java
¦   ¦   ¦   +-- Achievement.java
¦   ¦   ¦   +-- Tile.java
¦   ¦   ¦   +-- TileType.java
¦   ¦   ¦   +-- PlayerClass.java
¦   ¦   +-- util/
¦   ¦   ¦   +-- GameStateManager.java
¦   ¦   ¦   +-- InventoryManager.java
¦   ¦   ¦   +-- AchievementManager.java
¦   ¦   +-- adapter/
¦   ¦   ¦   +-- ShopItemAdapter.java
¦   ¦   ¦   +-- AchievementAdapter.java
¦   +-- res/
¦       +-- layout/
¦       ¦   +-- activity_main_menu.xml
¦       ¦   +-- activity_class_selection.xml
¦       ¦   +-- activity_game.xml
¦       ¦   +-- activity_shop.xml
¦       ¦   +-- activity_achievements.xml
¦       ¦   +-- activity_settings.xml
¦       ¦   +-- item_tile.xml
¦       ¦   +-- ...
¦       +-- values/
¦       ¦   +-- strings.xml
¦       ¦   +-- ...
+-- build.gradle.kts
+-- settings.gradle.kts
+-- AndroidManifest.xml
```

---

## ▶️ How to Run

1. **Clone the repository**  
   ```bash
   git clone https://github.com/adaplu/ClickDungeon.git
   ```
2. **Open in Android Studio**  
   Go to **File → Open**, and select the `ClickDungeon` folder
3. **Build & Run**  
   Deploy on an emulator or physical Android device

---

## ✅ Progress Checklist

- [x] Multi-floor dungeon with pitfall traps and stairs  
- [x] Class selection (Knight, Thief, Wizard) with abilities  
- [x] Traps & status effects (freeze, poison)  
- [x] “Trap Disarm Kit” item integration  
- [x] Achievements UI & unlocking logic  
- [x] Inventory & gold system with shop  
- [x] Resume game from main menu with save slots  
- [x] Key/stair integration and colored stair mechanics
- [x] Combat with various monsters
- [ ] Boss floors & advanced enemy AI (planned)  
- [ ] Additional classes and abilities (planned)

---

## 🗺️ Roadmap

### Near-Term Priorities
1. **Balance difficulty & economy** – Iterate on `GameBalance` reward formulas, drop rates, and difficulty multipliers using playtest feedback.
2. **Deepen combat presentation** – Layer enemy telegraphs, lightweight animations, and richer victory/defeat summaries into the combat dialog.
3. **Broaden accessibility & onboarding** – Expand color-blind affordances, add contextual tips for new mechanics, and surface difficulty guidance inline.
4. **Prototype boss and class expansions** – Outline boss floor structure plus perk trees or new abilities that build on the balanced combat loop.
5. **Instrumentation & localization prep** – Add analytics/remote config hooks and ready strings/tooltips for future translation.

### Long-Term Milestones
- **Milestone A — Boss Floor Launch:** Boss-only floors with scripted encounters, multi-phase combat behaviors, and tailored loot pacing to capstone each dungeon run.
- **Milestone B — Class & Progression Overhaul:** New playable classes, bespoke skill tracks, and achievement rewrites tied to advanced builds.
- **Milestone C — Monetization & Live Ops:** Cosmetic/IAP offerings, rewarded ad hooks, and analytics-driven tuning without undermining fair play.
- **Milestone D — UX Polish & Localization:** Audio/haptic feedback, accessibility upgrades, UI animation, and multi-language support.
- **Milestone E — Connected Services:** Cloud saves, leaderboards, rotating challenge dungeons, and social competition loops.

---

## 📜 License

**Commercial License**  
This project is licensed for commercial use. Redistribution, sublicensing, or modification is allowed only under explicit written permission from the author.

> © 2025 ClickDungeon Studios. All rights reserved.  
> Contact: `ClickDungeon@gmail.com`

## 🧪 Local development & testing
- Install Android SDK with platform 35 and ensure `sdk.dir` in `local.properties` points to it (see `local.properties.example`).
- Robolectric tests can be run with `./gradlew test`; Android Studio or the Gradle daemon will reuse the configured SDK and cached dependencies.
- If running in a restricted network environment, pre-seed the Gradle wrapper and Android SDK offline to avoid proxy download failures during CI.
