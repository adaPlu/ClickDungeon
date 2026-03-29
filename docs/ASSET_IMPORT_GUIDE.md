# Asset Import Guide - ClickDungeon

This document outlines the procedures for importing and validating game assets (sprites, sounds, icons, UI elements) into ClickDungeon.

---

## Overview

Assets in ClickDungeon are organized by type and resource density:

```
app/src/main/res/
├── drawable-nodpi/          ← Vector/scale-independent assets
│   ├── terrain_*.png        ← Terrain backgrounds (1920×1080)
│   ├── panel_*.xml          ← Shape drawables (UI components)
│   └── button_*.xml
├── drawable-xxhdpi/         ← High-resolution assets (540p+)
│   ├── icon_*.png           ← Class/item icons (64×64-128×128)
│   └── *_sprite_sheet.png   ← Animated character sheets
├── drawable/ (fallback)
├── mipmap-xxxhdpi/          ← App icon (adaptive icon with layers)
└── raw/                     ← Audio/data files
    ├── *.ogg                ← Sound effects
    └── *.json               ← Config files
```

---

## Asset Types & Specifications

### 1. Sprite Sheets (Animated Characters)

**Files**:
- `knight_sprite_sheet.png`
- `thief_sprite_sheet.png`
- `wizard_sprite_sheet.png`
- `ranger_sprite_sheet.png` (TODO)
- Monster sheets: `beast_sprite_sheet.png`, `humanoid_sprite_sheet.png`, etc.

**Format**:
- **Dimensions**: 64px × 64px per frame
- **Grid**: 4 rows × N columns
  - Row 0: IDLE (4 frames)
  - Row 1: MOVE (4 frames)
  - Row 2: ATTACK (4-6 frames)
  - Row 3: DEFEND (2 frames)
  - Rows 4+: Extended states (HIT, DEFEAT, CAST, LOOT, LEVEL_UP) — TODO
- **Color**: 32-bit RGBA PNG
- **Total size**: <1 MB per sheet
- **Transparency**: Yes (alpha channel for character outline)

**Naming convention**: `{entity}_sprite_sheet.png`

**Validation**:
- Check frame alignment (exact 64px grid)
- Verify alpha channel present (no pure white background)
- Confirm total frames ≥ minimum per state
- Test in-game: ensure no visual tearing/misalignment

### 2. Icon Assets (UI Elements)

**Files**:
- `icon_knight.png` — Class selection
- `icon_thief.png`
- `icon_wizard.png`
- `icon_ranger.png` (TODO)
- `icon_shield.png` — Equipment
- `icon_scroll.png` — Items
- `icon_potion_red.png` — Consumables
- `trap_*.png` — Trap indicators

**Format**:
- **Dimensions**: 64×64 or 128×128 (drawable-xxhdpi)
- **Scale**: 1x (drawable/) also required for fallback
- **Color**: 32-bit RGBA PNG
- **Style**: Consistent with game's minimalist/emoji aesthetic
- **Transparency**: Yes (alpha channel)

**Naming convention**: `icon_{entity_or_item}.png`

**Placement**:
```
drawable-xxhdpi/icon_*.png      (primary)
drawable/icon_*.png              (fallback 1x)
```

**Validation**:
- Check dimensions (64px or 128px square)
- Verify transparency against tile backgrounds
- Test in HUD layout (verify tap targets readable, no overflow)
- Check color contrast (accessibility)

### 3. Terrain Backgrounds

**Files**:
- `terrain_cavern.png`
- `terrain_crypt.png`
- `terrain_lava_field.png`
- `terrain_mire.png`
- `terrain_frozen_ruins.png`
- `terrain_thorn_wilds.png`
- `terrain_storm_plateau.png`
- `terrain_arcane_nexus.png`
- `terrain_sunken_temple.png`
- `terrain_ash_wastes.png`

**Format**:
- **Dimensions**: 1920×1080 (drawable-nodpi, scale-independent)
- **Color**: RGB or RGBA PNG (reduced transparency for background)
- **Style**: Atmospheric, tile-repeatable or full-screen
- **Optimization**: <500 KB per image (use PNG optimization tools)

**Naming convention**: `terrain_{type}.png`

**Placement**:
```
drawable-nodpi/terrain_*.png    (primary, scales to device)
```

**Validation**:
- Check for repeating patterns (if tileable)
- Verify legibility of 5×5 tile grid overlay
- Test on tablet (sw600dp+) and phone layouts
- Ensure not too bright (keeps text readable)

### 4. Sound Effects (Audio)

**Files**:
```
raw/
├── player_attack.ogg
├── player_move.ogg
├── player_defend.ogg
├── knight_attack.ogg
├── thief_move.ogg
├── wizard_attack.ogg
├── monster_attack.ogg
├── monster_defeat.ogg
├── effect_trap.ogg
├── effect_treasure.ogg
├── effect_level_up.ogg
├── effect_victory.ogg
├── effect_defeat.ogg
├── effect_shop_purchase.ogg
└── ... (see ROADMAP.md "Audio coverage checklist")
```

**Format**:
- **Codec**: OGG Vorbis (open standard, good compression)
- **Bitrate**: 128 kbps - 192 kbps (balance quality/size)
- **Duration**: 0.5s - 3s (most effects < 1s)
- **Channels**: Mono (effects), Stereo (music)
- **Sample rate**: 44.1 kHz or 48 kHz

**Naming convention**: `{entity|effect}_{action}.ogg`

**Placement**:
```
raw/                     (primary)
```

**Validation**:
- Check codec is OGG (Android compatibility)
- Verify no clicking at start/end (fade envelope)
- Test volume levels (normalize to -3dB peak)
- Ensure all cues registered in `SoundManager.java`
- Run audio diagnostics: Settings > Audio Diagnostics > check for missing keys

### 5. UI Layout Drawables (Vector)

**Files** (using Android ShapeDrawable):
- `panel_bg.xml` — Panel background shape
- `menu_button_bg.xml` — Button background
- `dungeon_door.xml` or `.png` — Menu background
- Grid tile overlays (programmatic, see `GameActivity.bindTileView()`)

**Format**:
- XML ShapeDrawable (preferred for scalable) or 9-patch PNG
- Must include border/shadow for depth
- Test on tablet + phone aspect ratios

**Validation**:
- Verify shape renders without artifacts on different screen sizes
- Check 9-patch grid (if using 9-patch PNG)
- Ensure touch targets ≥ 48dp (accessibility)

### 6. Configuration Files (JSON)

**Files**:
- `shop_items.json` — Shop catalog (items, prices, stock)
- `economy_config.json` — Balance multipliers
- `monster_templates.json` — Monster stats/variants
- `trusted_challenge_signers.json` — Signature verification (security)

**Format**:
- JSON (UTF-8, no BOM)
- Keys/values lowercase with underscores
- Comments not supported (strip before shipping)

**Validation**:
- Parse JSON using `Gson` or similar
- Validate schema against expected fields
- Test in-game: verify values load and apply correctly

---

## Import Procedure

### Step 1: Prepare Asset Files

1. **Gather assets** from design/audio team in source format (PNG, WAV, etc.)

2. **Optimize**:
   - Sprites/icons: Use PNGCrush or similar (target <500 KB per sheet)
   - Audio: Convert to OGG Vorbis at 128-192 kbps
   - Terrains: Optimize with ImageMagick or similar (keep <500 KB)

3. **Validate format**:
   - Sprites: Confirm 64px frame grid, 4+ rows
   - Icons: Confirm square (64px or 128px), RGBA
   - Audio: Confirm OGG format, 44.1-48 kHz

### Step 2: Copy to Resources

```powershell
# Navigate to res directory
cd app/src/main/res

# Copy sprites to drawable-xxhdpi
Copy-Item "path/to/ranger_sprite_sheet.png" drawable-xxhdpi/

# Copy icons to drawable & drawable-xxhdpi
Copy-Item "path/to/icon_ranger.png" drawable-xxhdpi/
Copy-Item "path/to/icon_ranger.png" drawable/ # 1x fallback

# Copy audio to raw
Copy-Item "path/to/*.ogg" raw/

# Copy terrains to drawable-nodpi
Copy-Item "path/to/terrain_*.png" drawable-nodpi/
```

### Step 3: Register in Code

**For sprites** (AnimatedPlayer.java / AnimatedMonster.java):
```java
private int getPlayerDrawable(PlayerClass playerClass) {
    switch (playerClass) {
        case RANGER:
            return R.drawable.ranger_sprite_sheet;  // ADD THIS
        // ... other cases
    }
}
```

**For icons** (class selection, HUD, inventory):
```java
// In ClassSelectionActivity or similar
int iconRes = getIconResource(playerClass);
imageView.setImageResource(iconRes);

private int getIconResource(PlayerClass playerClass) {
    switch (playerClass) {
        case RANGER:
            return R.drawable.icon_ranger;  // ADD THIS
        // ... other cases
    }
}
```

**For sounds** (SoundManager.java):
```java
public void loadSounds() {
    register("ranger_attack", R.raw.ranger_attack);
    register("ranger_move", R.raw.ranger_move);
    // ... other registrations
}
```

**For terrains** (GameActivity.java):
```java
private int getTerrainBackground(TerrainType terrain) {
    switch (terrain) {
        case CAVERN:
            return R.drawable.terrain_cavern;  // ADD or CONFIRM
        // ... other cases
    }
}
```

### Step 4: Run Build & Tests

```powershell
# Clean and rebuild to ensure resources are compiled
./gradlew clean

# Build project (will recompile resources)
./gradlew assembleDebug

# Run unit tests (includes font/drawable compilation checks)
./gradlew testDebugUnitTest

# Run on emulator to verify visually
./gradlew installDebugTest
# Then manually test in GameActivity/CombatDialogFragment
```

### Step 5: Validate Visually

#### Sprite Sheets
1. Launch app and select new class
2. Verify animation frames play smoothly
3. Check for frame misalignment or corruption
4. Test each state in combat (IDLE → MOVE → ATTACK → DEFEND → IDLE)

#### Icons
1. Navigate to class selection screen
2. Verify icons display at correct size
3. Check icon matches class description
4. Test on tablet (verify no overflow in grid)

#### Audio
1. Open Settings > Audio Diagnostics
2. Verify no missing-key warnings
3. Trigger combat and listen for audio cues
4. Verify volume levels are balanced (not too loud/quiet)

#### Terrains
1. Play through multiple floors
2. Verify background changes per terrain
3. Check visibility of 5×5 grid (background not too dark)
4. Test on tablet (verify background fills screen)

---

## Validations Checklist

### Pre-Import (Design/Audio Team)
- [ ] Sprites: All frames present, no corrupted pixels
- [ ] Icons: Square dimensions, transparent background
- [ ] Audio: OGG format, no silence/clipping
- [ ] Terrains: 1920×1080, optimized

### During Import (Developer)
- [ ] Files copied to correct drawable-* folder
- [ ] Code registrations added (AnimatedPlayer, SoundManager, etc.)
- [ ] R.drawable.* references resolve in IDE
- [ ] Build succeeds with `./gradlew assembleDebug`

### Post-Import (QA/Tester)
- [ ] Sprites animate smoothly without tearing
- [ ] Icons render correctly on phone & tablet
- [ ] Audio plays at appropriate volume
- [ ] Terrains render without stretching/distortion
- [ ] No "missing asset" errors in Logcat
- [ ] Audio Diagnostics (Settings) shows no missing keys
- [ ] Frame rate remains 60 FPS (profile with Android Profiler)

---

## Common Issues & Fixes

### Issue: Sprite Frames Misaligned

**Symptom**: Animation jumps or displays wrong frame

**Cause**: Actual frame dimensions ≠ 64px, or grid offset wrong

**Fix**:
1. Re-export sprite sheet with exact 64×64 frame size
2. Verify row/column count in PNG header (check with ImageMagick)
3. Clear Android build cache: `./gradlew clean`

### Issue: Icon Doesn't Display

**Symptom**: Icon shows as broken image or doesn't appear

**Cause**: 
1. File not in drawable-xxhdpi OR drawable
2. Resource ID typo in code
3. File format wrong (must be PNG)

**Fix**:
1. Verify file exists: `ls -la app/src/main/res/drawable*/icon_*.png`
2. Check code spelling: `R.drawable.icon_ranger` (not `icon_rangER`)
3. Confirm PNG format: `file app/src/main/res/drawable-xxhdpi/icon_ranger.png`

### Issue: Audio Doesn't Play

**Symptom**: Effect plays nothing or crashes

**Cause**:
1. File not registered in `SoundManager.java`
2. File format not OGG or corrupted
3. File path wrong in registration

**Fix**:
1. Check `SoundManager.loadSounds()`: is `register("ranger_attack", R.raw.ranger_attack);` present?
2. Verify file format: `file app/src/main/res/raw/ranger_attack.ogg`
3. Check Logcat for `SoundManager` errors
4. Run Settings > Audio Diagnostics to see missing keys

### Issue: Terrain Background Doesn't Appear

**Symptom**: Background black or wrong terrain shown

**Cause**:
1. Drawable not in drawable-nodpi
2. TerrainType enum case not mapping to drawable
3. Drawable ID typo

**Fix**:
1. Verify location: `ls -la app/src/main/res/drawable-nodpi/terrain_*.png`
2. Check `getTerrainBackground()` switch statement in GameActivity.java
3. Verify spelling: `R.drawable.terrain_cavern` (not `terrain_Cavern`)

---

## Asset Gaps (Current Status)

See [ANIMATION_ARCHITECTURE.md - Asset Gaps](ANIMATION_ARCHITECTURE.md#asset-gaps-current-status) for current missing assets and priorities.

**As of 2026-03-01**:
- [ ] Ranger sprite sheet (P0 - using thief fallback)
- [ ] Ranger icon (P0 - using thief fallback)
- [ ] Extended animation sprite sheets (P1 - HIT, DEFEAT, CAST, LOOT, LEVEL_UP)
- [ ] High-fidelity class sprites (P1 - awaiting design finalization)

---

## Performance Considerations

- **Sprite sheets**: Single animatedPlayer sheet (~500 KB) pre-loaded on class selection
- **Monster sheets**: Lazy-loaded per encounter (cached in memory)
- **Icons**: All pre-cached on app startup
- **Audio**: SoundPool pre-loads all OGG files (~2-3 MB total)
- **Terrains**: Single active background in memory; swapped on floor change

**Target memory footprint**: <10 MB for all assets

---

## Future Improvements

1. **Atlas optimization**: Combine multiple small sprites into atlases
2. **Audio compression**: Further optimize OGG bitrates
3. **WebP format**: Consider WebP for icons (smaller, requires API 18+)
4. **Procedural generation**: Generate some backgrounds procedurally
5. **Dynamic asset loading**: Load/unload monster sheets per floor

---

## References

- **Code**: [AnimatedPlayer.java - getPlayerDrawable()](../app/src/main/java/com/example/clickdungeon/model/AnimatedPlayer.java)
- **Code**: [SoundManager.java - loadSounds()](../app/src/main/java/com/example/clickdungeon/util/SoundManager.java)
- **Code**: [GameActivity.java - getTerrainBackground()](../app/src/main/java/com/example/clickdungeon/GameActivity.java)
- **Docs**: [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md)
- **Docs**: [ASSET_PIPELINE.md](ASSET_PIPELINE.md)

---

## Support

For asset import questions:
1. Check this guide's troubleshooting section
2. Review code reference sections (links above)
3. Check Logcat for `AssetManager`, `SoundManager`, or `Resources` errors
4. Run Settings > Audio Diagnostics to identify missing audio cues

