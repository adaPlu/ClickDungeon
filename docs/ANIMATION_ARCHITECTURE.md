# Animation Architecture - ClickDungeon

This document specifies the complete animation system used in ClickDungeon for player characters, monsters, and UI feedback effects.

---

## Overview

ClickDungeon uses sprite-sheet-based animation driven by frame-by-frame timing. Two main animated classes handle game entities:

- **`AnimatedPlayer`** — Player character animations
- **`AnimatedMonster`** — Enemy/boss animations
- **`UiFx`** — Overlay effects (floating text, spark bursts)

All animations are driven by a handler-based tick system that updates only active tiles (player + revealed enemies) to minimize UI overhead.

---

## Animation States

### Core States (Implemented)

| State | Description | Usage |
|-------|-------------|-------|
| **IDLE** | Character at rest, awaiting input | Default state between actions |
| **MOVE** | Character moving to new position/floor | Player walks to tile; enemy emerges |
| **ATTACK** | Melee/ranged attack action | Combat turns, special abilities |
| **DEFEND** | Shield/block/defensive pose | Potion use, trap dodge preparation |

### Extended States (WIP - TODO)

| State | Description | Usage | Priority |
|-------|-------------|-------|----------|
| **HIT** | Character takes damage | Enemy attack resolves; player hit animation | P0 |
| **DEFEAT** | Character falls/dies | Combat loss, floor wipe | P0 |
| **CAST** | Spell casting (wizard focus) | Ability: Fireball, Frost Nova, Chain Lightning | P0 |
| **LOOT** | Treasure collection animation | Gold/item pickup, chest open | P1 |
| **LEVEL_UP** | Celebration/power-up animation | XP threshold reached | P1 |

---

## Sprite Sheet Format

### Requirements

**File format**: PNG (32-bit RGBA)  
**Grid layout**: 4 rows × N columns at fixed frame size  

### Row Mapping

```
Row 0: IDLE frames
Row 1: MOVE frames
Row 2: ATTACK frames
Row 3: DEFEND frames

[Extended states stored separately or in rows 4+]
```

### Frame Dimensions

| Class | Frame Width | Frame Height | Total Frames | Notes |
|-------|-------------|--------------|--------------|-------|
| **Knight** | 64px | 64px | 8 (2x4) | Basic hilt/shield poses |
| **Thief** | 64px | 64px | 8 (2x4) | Agile dash/jump poses |
| **Wizard** | 64px | 64px | 8 (2x4) | Spell casting poses, robes |
| **Ranger** | 64px | 64px | 8 (2x4) | Bow draw/aim poses (PENDING) |
| **Monster** | 64px | 64px | 6-12 | Varies by family |

### Example Sprite Sheet Layout

```
[KNIGHT SPRITE SHEET - 480px × 256px]
╌───────────────────────────────────────╌
│ IDLE-1   │ IDLE-2   │ IDLE-3   │ IDLE-4   │  
│ MOVE-1   │ MOVE-2   │ MOVE-3   │ MOVE-4   │
│ ATK-1    │ ATK-2    │ ATK-3    │ ATK-4    │
│ DEF-1    │ DEF-2    │ DEF-3    │ DEF-4    │
╌───────────────────────────────────────╌
```

---

## Implementation Details

### AnimatedPlayer.java

**Location**: `app/src/main/java/com/example/clickdungeon/model/AnimatedPlayer.java`

```java
public class AnimatedPlayer {
    enum AnimationState { IDLE, MOVE, ATTACK, DEFEND }
    
    private PlayerClass playerClass;
    private int currentFrame;
    private AnimationState state;
    private Bitmap spriteSheet;
    
    public Bitmap getFrameBitmap(AnimationState state, int frameIndex) {
        int row = state.ordinal(); // 0=IDLE, 1=MOVE, 2=ATTACK, 3=DEFEND
        int col = frameIndex % framesPerRow;
        return extractFrame(spriteSheet, row, col);
    }
}
```

### Sprite Mapping

Player sprites are mapped by class in `AnimatedPlayer.java`:

```java
private int getPlayerDrawable(PlayerClass playerClass) {
    switch (playerClass) {
        case KNIGHT:
            return R.drawable.knight_sprite_sheet;
        case THIEF:
            return R.drawable.thief_sprite_sheet;
        case WIZARD:
            return R.drawable.wizard_sprite_sheet;
        case RANGER:
            // TODO: Add ranger_sprite_sheet
            // CURRENT: Using thief_sprite_sheet as fallback
            return R.drawable.thief_sprite_sheet;
        default:
            return R.drawable.knight_sprite_sheet; // Fallback
    }
}
```

**TODO**: Add Ranger sprite sheet and update mapping.

### AnimatedMonster.java

**Location**: `app/src/main/java/com/example/clickdungeon/model/AnimatedMonster.java`

Similar structure to `AnimatedPlayer`, but uses monster family/rarity for sprite selection:

```java
private int getMonsterDrawable(MonsterFamily family, int rarity) {
    switch (family) {
        case BEAST:
            return rarity > 50 ? R.drawable.beast_elite_sheet : R.drawable.beast_sprite_sheet;
        case HUMANOID:
            return R.drawable.humanoid_sprite_sheet;
        case UNDEAD:
            return R.drawable.undead_sprite_sheet;
        // ... more families
    }
}
```

---

## Animation Timing

### Frame Rate

**Default**: 10 FPS (100ms per frame)  
**Handler tick interval**: 100ms (via `Handler.postDelayed()`)

### State-Specific Timing

| State | Frames | Duration | Notes |
|-------|--------|----------|-------|
| IDLE | 4 | 400ms (loop) | Breathing/stance loop |
| MOVE | 4 | 400ms | Step animation |
| ATTACK | 4-6 | 400-600ms | Strike/windup/recovery |
| DEFEND | 2 | 200ms | Brace/shield pose |
| HIT | 2 | 200ms | Recoil animation (TODO) |
| DEFEAT | 4 | 400ms | Fall/collapse animation (TODO) |
| CAST | 6 | 600ms | Incant/release sequence (TODO) |
| LOOT | 3 | 300ms | Pickup/chest open (TODO) |
| LEVEL_UP | 4 | 400ms | Celebration jump (TODO) |

### Example: Attack Animation Flow

```
Time:       0ms      100ms     200ms     300ms     400ms
            │         │         │         │         │
State:    ATTACK    ATTACK    ATTACK    ATTACK    IDLE
Frame:     0         1         2         3         0
```

---

## Handler Lifecycle

### Active Animation Handler

```java
// In GameActivity.java
private Handler animationHandler = new Handler(Looper.getMainLooper());
private Runnable animationTick = () -> {
    updateActiveAnimations();
    animationHandler.postDelayed(animationTick, 100); // 100ms tick
};

// Start on Activity resume
@Override
protected void onResume() {
    super.onResume();
    animationHandler.post(animationTick);
}

// Stop on Activity pause to avoid leaks
@Override
protected void onPause() {
    animationHandler.removeCallbacks(animationTick);
    super.onPause();
}
```

### Tile Iteration for Efficiency

Only animate active tiles (player + revealed monsters):

```java
private void updateActiveAnimations() {
    // Update player animation
    if (playerTile != null && playerTile.isVisible()) {
        updateAnimationFrame(animatedPlayer);
    }
    
    // Update revealed enemies only
    for (Tile tile : grid) {
        if (tile.hasMonster() && tile.isRevealed()) {
            updateAnimationFrame(animatedMonster);
        }
    }
    
    renderGrid(); // Only dirty tiles update UI
}
```

---

## Combat Animation Integration

### CombatDialogFragment Animation Flow

```java
// In CombatDialogFragment.java
private void playAttackAnimation() {
    playerAnimated.setState(AnimationState.ATTACK);
    playerAnimated.setCurrentFrame(0);
    
    // Start frame loop
    animationHandler.post(() -> {
        playerAnimated.nextFrame();
        if (playerAnimated.isComplete()) {
            SoundManager.play(playerClass.getAttackSound());
            showDamageNumber(damageDealt);
            monsterAnimated.setState(AnimationState.HIT);
        }
    });
}
```

---

## UI Effects (UiFx)

### Location

`app/src/main/java/com/example/clickdungeon/util/UiFx.java`

### Supported Effects

| Effect | Parameters | Usage |
|--------|-----------|-------|
| **Floating Text** | Text, X/Y, Color, Duration | Damage numbers, gold gained, xp |
| **Spark Burst** | Center X/Y, Count, Color | Critical hit, level up, loot |
| **Shake** | Magnitude, Duration | Hit feedback, explosion |
| **Fade** | Duration, StartAlpha | Trap trigger reveal, ghost appear |

### Example Usage

```java
// Show damage number
uiFx.floatingText("25 DMG", x, y, Color.RED, 1000); // 1 second

// Spark burst on critical hit
uiFx.sparkBurst(x, y, 12, Color.YELLOW);

// Shake on heavy hit
uiFx.shake(0.5f, 300); // Half-screen, 300ms
```

---

## State Diagram

```
        ┌─────────────┐
        │    IDLE     │◄──────┐
        └─────┬───────┘       │
              │               │
    ┌─────────┴─────────┐     │
    │                   │     │
    ▼                   ▼     │
+─────────+          +──────+─┘
│  MOVE   │          │ ATTACK│
└──┬──────┘          └───┬──┬┘
   │                     │  │
   │    ┌────────────────┘  │
   │    │                   │
   ▼    ▼                   │
+──────────────+            │
│   DEFEND     │────────────┘
└──────────────┘

Extended (TODO):
    HIT ──► IDLE
    DEFEAT ──[Game Over]
    CAST ──► IDLE
    LOOT ──► IDLE
    LEVEL_UP ──► IDLE
```

---

## Adding New Animation States

### Steps to Implement HIT State (Example)

1. **Add to AnimationState enum**
   ```java
   enum AnimationState { IDLE, MOVE, ATTACK, DEFEND, HIT }
   ```

2. **Update sprite sheet row mapping**
   - Ensure sprite sheet has HIT frames in row 4
   - Update `getFrameBitmap()` to map row 4 to HIT state

3. **Add transition logic**
   ```java
   public void playHitAnimation() {
       setState(AnimationState.HIT);
       setCurrentFrame(0);
       // After HIT frames complete, revert to IDLE
       handler.postDelayed(() -> setState(AnimationState.IDLE), 200);
   }
   ```

4. **Integrate into combat flow**
   ```java
   // In CombatDialogFragment.java
   void resolveEnemyAttack() {
       playerAnimation.playHitAnimation();
       SoundManager.play("player_hit");
       uiFx.shake(0.3f, 200);
   }
   ```

5. **Add tests**
   ```java
   @Test
   public void testHitAnimationCompletes() {
       animatedPlayer.playHitAnimation();
       // Wait for animation frames
       assertEquals(AnimationState.IDLE, animatedPlayer.getState());
   }
   ```

---

## Asset Gaps (Current Status)

| Asset | Status | Priority | Owner |
|-------|--------|----------|-------|
| `ranger_sprite_sheet` | ❌ Missing (using thief fallback) | P0 | Design |
| `icon_ranger` | ❌ Missing (using icon_thief) | P0 | Design |
| Extended state sheets (HIT, DEFEAT, CAST, LOOT, LEVEL_UP) | ❌ Not created | P1 | Design |
| Monster elite variants | ⚠️ Scaffolded, content TBD | P2 | Design |
| Boss-specific animations | ⚠️ Using standard templates | P2 | Design |

**Status as of 2026-03-01**: Asset delivery is external; code is ready to ingest assets once provided.

---

## Performance Considerations

### Optimization Strategies

1. **Frame Update Throttling**
   - Only tick handler when animations are active
   - Stop handler on pause to prevent battery drain

2. **Bitmap Caching**
   - Pre-load sprite sheets on class selection
   - Cache extracted frames to avoid per-tick extraction

3. **Dirty Tile Rendering**
   - Only `invalidate()` tiles with animation updates
   - Use `GridLayout.setViewHolderUpdates()` for efficient rasterization

### Memory Budget

- **Player sprite sheet**: ~500KB (all 4 classes pre-loaded)
- **Monster sprite sheets**: ~1.5MB (lazy-loaded per encounter)
- **UI overlay textures**: ~200KB (reusable across screens)

**Total targeted**: <5 MB for animation assets

---

## Testing Animation Behavior

### Unit Tests

```java
// AnimatedPlayerTest.java
@Test
public void testStateTransition() {
    animatedPlayer.setState(AnimationState.ATTACK);
    animatedPlayer.nextFrame();
    assertTrue(animatedPlayer.getCurrentFrame() < animatedPlayer.getMaxFrames());
}

@Test
public void testFrameWrapping() {
    animatedPlayer.setState(AnimationState.IDLE);
    // Iterate past max frames
    for (int i = 0; i < 100; i++) {
        animatedPlayer.nextFrame();
    }
    // Should wrap back to 0
    assertTrue(animatedPlayer.getCurrentFrame() < 4);
}
```

### Robolectric Integration Tests

```java
// CombatDialogFragmentTest.java
@Test
public void testAttackAnimationPlaysSound() {
    // Trigger attack
    fragment.attackMonster();
    
    // Assert animation frame incremented
    assertEquals(AnimationState.ATTACK, player.getState());
    
    // Assert sound registered
    assertThat(soundManager.lastPlayedEffect()).isEqualTo("player_attack");
}
```

---

## References

- **Code**: [AnimatedPlayer.java](../app/src/main/java/com/example/clickdungeon/model/AnimatedPlayer.java)
- **Code**: [AnimatedMonster.java](../app/src/main/java/com/example/clickdungeon/model/AnimatedMonster.java)
- **Code**: [UiFx.java](../app/src/main/java/com/example/clickdungeon/util/UiFx.java)
- **Code**: [CombatDialogFragment.java](../app/src/main/java/com/example/clickdungeon/ui/CombatDialogFragment.java)
- **Config**: [robolectric.properties](../app/robolectric.properties)

---

## TODO

- [ ] Implement HIT animation state (code + sprite frames)
- [ ] Implement DEFEAT animation state (code + sprite frames)
- [ ] Implement CAST animation state (wizard focus)
- [ ] Implement LOOT animation state (chest/treasure)
- [ ] Implement LEVEL_UP animation state (celebration)
- [ ] Add Ranger sprite sheet and icon
- [ ] Finalize extended state sprite sheets
- [ ] Add extended state tests to CombatDialogFragmentTest
- [ ] Performance profile: measure animation handler overhead

