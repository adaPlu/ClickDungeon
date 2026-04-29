# ClickDungeon Screenshot Specification

## Play Console Requirements

- Minimum **2 phone screenshots** required for Google Play listing.
- Accepted resolutions: **1080x1920 px** (16:9) or **1080x2340 px** (19.5:9).
- Format: PNG or JPEG, 24-bit color, no alpha.
- All screenshots must be captured from an external Android phone running the
  **release build** (not debug) so UI looks production-accurate.
- Recommended device: **Pixel 6** (1080x2340, Android 12+) or any 1080p phone.

---

## Screenshot 1 - Main Menu (REQUIRED)

**Screen:** `MainMenuActivity`

**What to show:**
- Dungeon-door background drawable (`bg_screen_dungeon`) fully visible.
- App title "ClickDungeon" visible at top.
- All three primary buttons visible and not obscured:
  - **Play** (new game)
  - **Continue** (enabled - requires an existing save slot)
  - **Settings**
- No system status bar overlapping the logo if possible (use immersive mode).

**Setup steps before capturing:**
1. Launch the app with an existing save so the Continue button is active.
2. Do not open any dialog - capture the idle main menu state.

**Recommended device orientation:** Portrait.

**Caption suggestion for Play Console:**
> "Choose your class and descend into the dungeon."

---

## Screenshot 2 - Active Dungeon (REQUIRED)

**Screen:** `GameActivity` - mid-run on any floor between 2 and 10.

**What to show:**
- The full **5x5 dungeon grid** with a mix of tile states:
  - At least 3 revealed empty tiles.
  - At least 1 revealed enemy tile (monster icon visible).
  - At least 1 revealed gold/chest tile.
  - Some unrevealed (fog) tiles remaining.
- **Player HUD** fully visible at the bottom:
  - HP bar with current/max HP text.
  - Class icon (e.g., Knight shield icon).
  - Ability button (lit if ability is ready).
  - Inventory button.
  - Potion button (with potion count).
- Floor number indicator visible.

**Setup steps before capturing:**
1. Start a new game as **Knight** (most visually readable class icon).
2. Reveal roughly half the tiles manually - mix of enemy, gold, and empty.
3. Keep at least 1 potion in inventory so the potion button shows a count.
4. Capture before any enemy combat dialog opens.

**Recommended device orientation:** Portrait.

**Caption suggestion for Play Console:**
> "Reveal tiles, battle monsters, and manage your resources."

---

## Screenshot 3 - Boss Combat Dialog (OPTIONAL)

**Screen:** `CombatDialogFragment` shown over `GameActivity` on a boss floor.

**What to show:**
- Boss health bar at the top of the dialog (red/orange fill, nearly full).
- Monster sprite/animation frame centered in the dialog.
- Combat log (at least 2 lines of prior action text visible).
- **Attack** and **Flee** buttons both visible at the bottom of the dialog.
- Player HP visible in the HUD behind the dialog (partially visible is fine).

**Setup steps before capturing:**
1. Reach floor 10, the first boss floor in the campaign.
2. Open combat with the boss - pause before taking any action.
3. Dismiss any telegraph overlay first if it obscures the health bar.

**Recommended device orientation:** Portrait.

**Caption suggestion for Play Console:**
> "Face powerful bosses with telegraphed attacks - survive or flee."

---

## Screenshot 4 - Class Selection (OPTIONAL)

**Screen:** `ClassSelectionActivity`

**What to show:**
- All **4 class buttons** visible without scrolling:
  - Knight (shield/sword icon)
  - Ranger (bow icon)
  - Thief (dagger icon)
  - Wizard (staff/wand icon)
- Each button shows the class name and icon clearly.
- No class pre-selected (initial state, all buttons equal weight).

**Setup steps before capturing:**
1. Start a new game to reach the class selection screen.
2. Do not tap any class button - capture the initial selection state.

**Recommended device orientation:** Portrait.

**Caption suggestion for Play Console:**
> "Choose from four classes, each with unique abilities and progression."

---

## Export Checklist

| # | Screen | Status | File name |
|---|--------|--------|-----------|
| 1 | Main Menu | PENDING | `screenshot_01_main_menu.png` |
| 2 | Active Dungeon | PENDING | `screenshot_02_dungeon_grid.png` |
| 3 | Boss Combat | PENDING | `screenshot_03_boss_combat.png` |
| 4 | Class Selection | PENDING | `screenshot_04_class_selection.png` |

Place captured files in `docs/screenshots/output/` before uploading to the
Google Play Console. The feature graphic is already present at
`docs/store_assets/feature_graphic.png`; the phone screenshots remain pending
until this external-device capture is completed.

## Windows / ADB Capture Workflow

Use the guided PowerShell workflow from the repository root:

```powershell
.\scripts\capture_play_screenshots.ps1
```

The script checks that `adb` is available, verifies that one authorized Android
phone is connected, creates `docs/screenshots/output/`, and captures the two
required screenshots with the file names listed above.

Useful options:

```powershell
.\scripts\capture_play_screenshots.ps1 -IncludeOptional
.\scripts\capture_play_screenshots.ps1 -DeviceSerial SERIAL
.\scripts\capture_play_screenshots.ps1 -Force
.\scripts\capture_play_screenshots.ps1 -Help
```

If no live phone is connected, the script exits with instructions for installing
Android Platform-Tools, connecting a phone, accepting USB debugging
authorization, and confirming `adb devices` shows state `device`.
