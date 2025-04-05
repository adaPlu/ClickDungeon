# ??? ClickDungeon - Android Dungelot-Inspired Roguelite

**ClickDungeon** is a premium Android dungeon-crawler inspired by the Dungelot series, developed in Java using Android Studio. Players explore layered floors of a hidden-tile dungeon, collect gold, unlock achievements, and use special class abilities to survive and progress. Designed for commercial use with built-in expansion points for monetization and further gameplay depth.

---

## ?? Key Features

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
   - Pitfall traps cause you to advance downward.  
   - Survive deeper floors with increased challenge and better rewards (planned).

6. **Achievements & Progress**  
   - Collect multiple achievements (e.g., “First Blood,” “Low HP Survivor”).  
   - Earn them by uncovering tiles, winning with minimal HP, or reaching boss floors.  
   - Achievements stored and displayed in **AchievementsActivity**.

7. **Game Over & Victory**  
   - You lose if your HP drops to 0.  
   - Reveal all safe tiles on a floor to claim victory and proceed to the next.  
   - Automatic saving: resume where you left off from the main menu.

8. **Modular Architecture**  
   - **SharedPreferences** used throughout for saving grid states, gold, and achievements.  
   - Separate activities for each feature (shop, achievements, settings, etc.).  
   - Clean codebase supports expansions like IAP, ads, new classes, and more.

---

## ?? Technologies Used

- **Java (Android SDK)**
- **Android Studio**  
- **SharedPreferences** for persistent state
- **Gson** for JSON serialization
- **RecyclerView** for item listing (Shop & Achievements)

---

## ??? Project Structure

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

## ?? How to Run

1. **Clone the repository**  
   ```bash
   git clone https://github.com/adaplu/ClickDungeon.git
   ```
2. **Open in Android Studio**  
   **File ? Open** ? Select the `ClickDungeon` folder
3. **Build & Run**  
   - Deploy on an emulator or physical Android device.

---

## ? Progress Checklist

- [x] Multi-floor dungeon with pitfall traps  
- [x] Class selection (Knight, Thief, Wizard)  
- [x] Traps & status effects (freeze, poison)  
- [x] New “Trap Disarm Kit” usage  
- [x] Achievements UI & unlocking logic  
- [x] Inventory & gold system (Shop integration)  
- [x] Resume game from main menu  
- [ ] Boss floors & advanced enemy AI (planned)  
- [ ] Additional class abilities & skill trees (planned)  

---

## ?? Future Roadmap

1. **Extended Combat & Bosses**  
   - Introduce boss floors with unique mechanics.  
   - Add multi-turn battles and enemy variety.
2. **More Achievements & Classes**  
   - Additional classes (Ranger, Paladin, Necromancer, etc.).  
   - Deepen achievement list (speedruns, no-damage runs, etc.).
3. **Monetization**  
   - Implement ads or in-app purchases for premium items.  
   - Shop expansions with more advanced gear.
4. **Polished UI/UX**  
   - Enhanced animations, transitions, and theming.  
   - Accessibility improvements & language localization.
5. **Cloud Saves & Leaderboards**  
   - Allow cross-device progress syncing.  
   - Global scoreboard for top dungeon delvers.

---

## ??? License

**Commercial License**  
This project is licensed for commercial use. Redistribution, sublicensing, or modification is allowed only under explicit written permission from the author.

> © 2025 ClickDungeon Studios. All rights reserved.

For licensing inquiries, please contact: `ClickDungeon@gmail.com`
