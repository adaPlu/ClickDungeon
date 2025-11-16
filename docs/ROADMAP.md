# ClickDungeon Development Roadmap

This document inventories the current state of the ClickDungeon Android project and highlights the most impactful areas for follow-up work. It is structured to make it easy to see what is **complete**, **in progress**, **not yet started**, and **prioritized next**.

## Architecture & Project Snapshot
- Android app targeting SDK 35 with Kotlin-based Gradle configuration (`build.gradle.kts`, `settings.gradle.kts`).
- Activities orchestrate the main menu, dungeon gameplay, class selection, shop, achievements, settings, and a dedicated multi-slot continue screen (`ContinueActivity`).
- SharedPreferences + Gson back all persistence helpers for player profiles, dungeon grids, inventory, achievements, and slot-based saves (`GameStateManager`, `InventoryManager`, `AchievementManager`, `SaveManager`).
- UI is built with XML layouts in `app/src/main/res/layout`, including reusable dialogs such as `dialog_combat.xml`, and styled with Material/AppCompat components.
- Interactive combat is delivered through `CombatDialogFragment`, while dungeon exploration logic lives in `GameActivity` alongside tile/monster models.

## Completed / Stable Functionality
### Core Loop & Navigation
- **Main menu routing:** `MainMenuActivity` launches class creation, the dungeon, the shop, achievements, and settings.
- **Character creation:** `ClassSelectionActivity` captures hero name/class, instantiates `CharacterProfile`, and saves it to `player_profile` preferences.
- **Dungeon gameplay:** `GameActivity` generates 5×5 floors with traps, monsters, stairs, and keys; handles trap resolution, class abilities, and XP gains; and persists state with `GameStateManager`.
- **Inventory & shop:** `ShopActivity` reads/writes gold and items via `InventoryManager`, offering mock purchasable `ShopItem`s that populate the inventory.
- **Achievements display:** `AchievementsActivity` renders achievements using `AchievementAdapter`, seeding mock data when preferences are empty.
- **Multi-slot continuation:** `ContinueActivity`, `SaveManager`, and slot-aware launch extras in `GameActivity`/`ClassSelectionActivity` allow players to create, resume, or overwrite any of four save slots.

### Combat & Encounter Flow
- **Interactive battles:** `GameActivity` opens `CombatDialogFragment` when enemies are revealed, enabling attack, potion, and flee actions with persistent HP tracking.
- **Dynamic monsters:** Floor-scaled monster templates ensure each enemy tile spawns a fresh `Monster` instance with emoji-driven feedback and variable stats.
- **Post-combat rewards:** Victories grant XP, gold, and tile conversions, feeding back into inventory and save data.
- **Telegraphed intents:** Animated combat intent bars preview incoming damage, with end-of-fight summaries highlighting turns, damage dealt/taken, potion usage, XP, and gold whether players win, flee, or fall.

### Player Feedback, Progression & Stability
- **Configurable settings:** `SettingsActivity` exposes audio, vibration, and difficulty controls backed by `SettingsManager`, while `FeedbackManager` routes cues through those preferences.
- **Achievement lifecycle:** `AchievementManager` seeds the full catalogue on first launch and unlocks milestones from combat wins, trap dodges, tile reveals, and gold accumulation.
- **Unified inventory & gold:** `InventoryManager` owns item stacks and currency, with the dungeon, shop, and save systems syncing through a single API to avoid stale quantities.
- **Accessibility & onboarding:** Settings-driven color-blind mode augments emoji with letter codes, and a first-run tutorial surfaces difficulty-aware tips.
- **Automated verification:** Robolectric suites now cover settings (including the UI toggles), inventory, achievements, save slots, onboarding prompts, feedback cues, game state, balance helpers, and combat dialog edge cases to catch persistence regressions early.

### Supporting Infrastructure
- **Models & adapters:** Domain models for tiles, monsters, inventory, and achievements back both gameplay logic and UI binding.
- **Persistence helpers:** `GameStateManager`, `InventoryManager`, `AchievementManager`, and `SaveManager` abstract SharedPreferences serialization/deserialization.
- **Testing utilities:** Mock data providers (e.g., `MockAchievements`) help populate UI when real saves are missing.

## In Progress / Partially Implemented
- **Economy tuning reviews:** `GameBalance` scaffolding is live, and sustained balancing of rewards versus difficulty remains under observation as more floors and encounters are added.
- **Accessibility onboarding:** Tutorial toggles and color-blind aids are in place, with further revisions planned as new encounters and tooltips arrive.
- **Combat presentation polish:** Animated telegraphs and richer end-of-fight summaries are scoped but not yet implemented beyond the existing dialog.

## Not Yet Started / Backlog Items
- **Content expansion:** README ambitions for bosses, varied enemy AI, additional classes, monetization hooks, analytics, and online features have no supporting code yet.
- **Quality-of-life:** Visual polish, accessibility, localization, tutorialization, and richer audio/haptic feedback remain future work.
- **Live operations:** Remote configuration, analytics funnels, ad mediation, and purchase flows are still conceptual only.

## Near-Term Roadmap
1. **Balance difficulty & economy** – Iterate on `GameBalance` reward formulas, drop rates, and difficulty multipliers using playtest feedback.
2. **Deepen combat presentation** – Layer enemy telegraphs, lightweight animations, and richer victory/defeat summaries into the combat dialog.
3. **Broaden accessibility & onboarding** – Expand color-blind affordances, add contextual tips for new mechanics, and surface difficulty guidance inline.
4. **Prototype boss and class expansions** – Outline boss floor structure plus perk trees or new abilities that build on the balanced combat loop.
5. **Instrumentation & localization prep** – Add analytics/remote config hooks and ready strings/tooltips for future translation.

## Long-Term Milestones (Post-Core Loop)
- **Milestone A — Boss Floor Launch:** Boss-only floors with scripted encounters, multi-phase combat behaviors, and tailored loot pacing to capstone each dungeon run.
- **Milestone B — Class & Progression Overhaul:** New playable classes, bespoke skill tracks, and achievement rewrites tied to advanced builds.
- **Milestone C — Monetization & Live Ops:** Cosmetic/IAP offerings, rewarded ad hooks, and analytics-driven tuning without undermining fair play.
- **Milestone D — UX Polish & Localization:** Audio/haptic feedback, accessibility upgrades, UI animation, and multi-language support.
- **Milestone E — Connected Services:** Cloud saves, leaderboards, rotating challenge dungeons, and social competition loops.
