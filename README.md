# 🕹️ ClickDungeon - Android Dungelot-Inspired Roguelite

**ClickDungeon** is a premium Android dungeon-crawler inspired by the Dungelot series, developed in Java using Android Studio. Explore a mysterious grid-based dungeon, uncover treasures, survive enemies, and unlock achievements. Built for commercial use and monetization.

---

## 📱 Features

- 🧱 5x5 dungeon grid with fog-of-war mechanics
- 💰 Gold collection and persistent inventory system
- 💀 Instant game-over on enemy tile (future combat system planned)
- 🥇 Victory condition triggers on revealing all safe tiles
- 🔄 Continue saved game sessions seamlessly
- 🛍️ Interactive shop screen with mock purchases and inventory updates
- 🏆 Achievements screen with unlock tracking and saving
- 🧠 SharedPreferences used for reliable save/load functionality
- 🛠️ Modular design for commercial expansion: player stats, power-ups, monetization

---

## 🧩 Technologies Used

- Java (Android SDK)
- Android Studio
- SharedPreferences (persistent storage)
- Gson (for JSON serialization)
- RecyclerView (used in shop and achievement screens)

---

## 🗂️ Project Structure

```bash
ClickDungeon/
├── app/
│   ├── java/com/example/clickdungeon/
│   │   ├── GameActivity.java
│   │   ├── MainMenuActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── ShopActivity.java
│   │   ├── AchievementsActivity.java
│   │   ├── model/
│   │   │   ├── Tile.java
│   │   │   ├── TileType.java
│   │   │   ├── ShopItem.java
│   │   │   ├── InventoryItem.java
│   │   │   └── Achievement.java
│   │   ├── util/
│   │   │   ├── GameStateManager.java
│   │   │   ├── InventoryManager.java
│   │   │   └── AchievementManager.java
│   │   ├── adapter/
│   │   │   ├── ShopItemAdapter.java
│   │   │   └── AchievementAdapter.java
│   └── res/
│       ├── layout/
│       │   ├── activity_game.xml
│       │   ├── item_tile.xml
│       │   └── ...
│       ├── values/
│       │   └── strings.xml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 How to Run

1. Clone this repository:
   ```bash
   git clone https://github.com/adaplu/ClickDungeon.git
   ```
2. Open in Android Studio
3. Build & run on an emulator or physical device

---

## ✅ Progress Checklist

- [x] Core dungeon grid and tile interaction
- [x] Gold/inventory system with SharedPreferences
- [x] Game over and victory flows
- [x] Shop screen and inventory integration
- [x] Achievements UI + save logic
- [x] Game resume with Continue button
- [ ] Achievement unlocking logic (in progress)
- [ ] Combat system (planned)
- [ ] UI polish + commercial branding (planned)

---

## 💡 Future Roadmap

- Combat: HP system, enemy attack logic, turn-based battle
- Ads/IAP: integrate ad system or in-app purchases
- Achievements: trigger-based unlock logic (e.g., collect 5 gold)
- Enhanced UI: animations, icons, transitions
- Procedural dungeons, multiple difficulty levels

---

## 🛡️ License

**Commercial License**

This project is licensed for commercial use. Redistribution, sublicensing, or modification is allowed only under explicit written permission from the author.

> © 2025 ClickDungeon Studios. All rights reserved.

For licensing inquiries, please contact: `ClickDungeon@gmail.com`

