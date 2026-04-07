# Telemetry Events Catalog - ClickDungeon

This document specifies all telemetry events captured by ClickDungeon and their payload schemas for analytics and debugging.

---

## Overview

ClickDungeon captures behavioral events through a vendor-neutral `TelemetryManager` interface to log:
- User actions (class selection, floor progression, combat outcomes)
- Feature usage (achievements, shop purchases, ability activation)
- System health (crashes, save failures, network issues)
- Performance metrics (frame rate, animation lag)

All events are logged without personally identifiable information (PII) and are stored locally via SharedPreferences initially, then synced to backend services (if enabled).

---

## Event Structure

### Standard Event Schema

All events follow this structure:

```json
{
  "event_type": "string",           // Unique event identifier
  "timestamp": "ISO-8601 string",   // UTC timestamp (e.g., "2026-03-03T14:30:00Z")
  "session_id": "UUID string",      // Unique per app session
  "user_id": "UUID string",         // Anonymous user ID (no username/email)
  "flavor": "offline|online",       // Build flavor
  "app_version": "string",          // App version (e.g., "0.06")
  "android_version": "integer",     // API level (21, 34, 36, etc.)
  "device_model": "string",         // Device model (anonymized)
  "payload": {                      // Event-specific data (see below)
    // ... type-specific fields
  },
  "duration_ms": "integer",         // Optional: duration in milliseconds
  "error": "string"                 // Optional: error message if failure
}
```

### Event Retention & Privacy

- **Retention**: 90 days (local), 30 days (backend)
- **PII Protection**: No usernames, emails, or device IDs
- **Session anonymity**: New session_id and user_id per app install
- **Compliance**: GDPR-compliant (no tracking across apps)

---

## Event Catalog

### 1. SESSION EVENTS

#### `session_start`
Fired when app launches and session begins.

```json
{
  "event_type": "session_start",
  "timestamp": "2026-03-03T14:30:00Z",
  "session_id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "user_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "payload": {
    "first_launch": false,           // true = first app install
    "previous_session_duration_s": 1824,  // Duration of last session
    "crash_on_previous_close": false // true = app crashed last time
  }
}
```

**Location**: `ClickDungeonApp.onCreate()`  
**Frequency**: Once per app launch

---

#### `session_end`
Fired when app exits or session ends.

```json
{
  "event_type": "session_end",
  "timestamp": "2026-03-03T15:00:00Z",
  "payload": {
    "session_duration_s": 1800,      // Total session time
    "total_playtime_s": 84600,       // Cumulative playtime
    "save_count": 12,                // # of saves in session
    "last_active_screen": "GameActivity"
  }
}
```

**Location**: `ClickDungeonApp.onTerminate()` or Activity termination  
**Frequency**: Once per session close

---

### 2. GAME STATE EVENTS

#### `run_start`
Fired when player creates a new dungeon run.

```json
{
  "event_type": "run_start",
  "timestamp": "2026-03-03T14:35:00Z",
  "payload": {
    "class_selected": "WIZARD",      // PlayerClass
    "difficulty": "NORMAL",          // Game difficulty
    "save_slot": 0,                  // Slot number (0-3)
    "starting_level": 1,
    "starting_hp": 25,
    "starting_platinum": 100,
    "starting_gold": 50,             // Gold earned from runs
    "total_playtime_s": 82800        // Player's cumulative time
  }
}
```

**Location**: `GameActivity.onCreate()` after class selection  
**Frequency**: Once per run start

---

#### `floor_reached`
Fired when player progresses to a new floor.

```json
{
  "event_type": "floor_reached",
  "timestamp": "2026-03-03T14:37:30Z",
  "payload": {
    "floor_number": 3,
    "terrain": "LAVA_FIELD",         // TerrainType
    "is_boss_floor": false,          // true = boss encounter floor
    "current_hp": 22,
    "current_level": 1,
    "time_on_floor_s": 45            // Time spent on previous floor
  }
}
```

**Location**: `GameActivity.floorTransition()` or stair interact  
**Frequency**: Per floor progression

---

#### `trap_triggered`
Fired when player hits a trap.

```json
{
  "event_type": "trap_triggered",
  "timestamp": "2026-03-03T14:38:00Z",
  "payload": {
    "trap_type": "FIRE",             // TrapType (FIRE, POISON, ACID, FREEZE, PITFALL)
    "damage_taken": 5,
    "current_hp_after": 17,
    "prevented_by_kit": false        // true = Disarm Kit used
  }
}
```

**Location**: `GameActivity.revealTile()` or `resolveTrapDamage()`  
**Frequency**: Per trap hit

---

#### `run_completed`
Fired when player wins a run (all tiles revealed on final floor).

```json
{
  "event_type": "run_completed",
  "timestamp": "2026-03-03T15:10:00Z",
  "payload": {
    "final_floor": 15,               // Successfully reached
    "total_duration_s": 1800,        // Total run playtime
    "final_hp": 8,                   // Remaining HP at end
    "final_level": 5,                // Final character level
    "gold_earned": 2500,
    "platinum_earned": 250,          // From premium sources
    "achievements_unlocked": ["FIRST_BLOOD", "BOSS_SLAYER"],
    "boss_defeats": 3,
    "highest_damage": 75             // Single hit damage
  }
}
```

**Location**: `GameActivity.revealAllTiles()` or victory trigger  
**Frequency**: Per successful run completion

---

#### `run_failed`
Fired when player loses (HP reaches 0).

```json
{
  "event_type": "run_failed",
  "timestamp": "2026-03-03T14:55:00Z",
  "payload": {
    "failure_floor": 7,              // Floor where player died
    "failure_cause": "MONSTER_DEFEAT", // MONSTER_DEFEAT, TRAP_DAMAGE, etc.
    "total_duration_s": 1200,
    "final_level": 3,
    "gold_earned": 800,
    "damage_taken_last_turn": 12,
    "enemy_name": "Goblin",          // Last combatant
    "was_boss": false
  }
}
```

**Location**: `GameActivity.playerDefeated()` or combat loss  
**Frequency**: Per failed run

---

### 3. COMBAT EVENTS

#### `combat_started`
Fired when player enters combat dialog.

```json
{
  "event_type": "combat_started",
  "timestamp": "2026-03-03T14:42:00Z",
  "payload": {
    "enemy_name": "Fire Mage",
    "enemy_family": "HUMANOID",      // MonsterFamily
    "enemy_level": 3,
    "enemy_hp": 18,
    "player_hp": 20,
    "player_class": "WIZARD",
    "floor_number": 4,
    "terrain": "LAVA_FIELD"
  }
}
```

**Location**: `GameActivity.startCombat()` → `CombatDialogFragment.onShow()`  
**Frequency**: Per combat encounter

---

#### `combat_turn`
Fired after each combat turn resolution.

```json
{
  "event_type": "combat_turn",
  "timestamp": "2026-03-03T14:42:15Z",
  "payload": {
    "turn_number": 1,
    "action_taken": "ATTACK",        // ATTACK, POTION, FLEE
    "damage_dealt": 8,
    "damage_taken": 5,
    "enemy_action": "ATTACK",        // What enemy did
    "player_hp_after": 15,
    "enemy_hp_after": 10,
    "ability_used": null,            // e.g., "FIREBALL" if ability triggered
    "critical_hit": false            // true = damage crit
  }
}
```

**Location**: `CombatDialogFragment.resolveTurn()`  
**Frequency**: Per player action in combat

---

#### `combat_ended`
Fired when combat dialog closes (victory, defeat, or flee).

```json
{
  "event_type": "combat_ended",
  "timestamp": "2026-03-03T14:43:00Z",
  "payload": {
    "outcome": "VICTORY",            // VICTORY, DEFEAT, FLED
    "total_turns": 4,
    "total_damage_dealt": 28,
    "total_damage_taken": 12,
    "final_player_hp": 8,
    "gold_earned": 150,
    "xp_earned": 75,
    "items_looted": ["POTION_RED", "TRAP_KIT"],
    "duration_s": 60                 // Combat duration
  }
}
```

**Location**: `CombatDialogFragment.dismissDialog()` or battle resolution  
**Frequency**: Per combat end

---

### 4. CLASS & ABILITY EVENTS

#### `class_selected`
Fired when player selects a class.

```json
{
  "event_type": "class_selected",
  "timestamp": "2026-03-03T14:33:00Z",
  "payload": {
    "class_name": "WIZARD",
    "selected_from": "CLASS_SELECTION_SCREEN", // or CONTINUE_SCREEN
    "total_playtime_with_class_s": 0,  // Cumulative time with this class
    "times_selected": 1              // How many times player picked this class
  }
}
```

**Location**: `ClassSelectionActivity.selectClass()` → `GameActivity.onCreate()`  
**Frequency**: Per class selection

---

#### `ability_used`
Fired when player activates an ability.

```json
{
  "event_type": "ability_used",
  "timestamp": "2026-03-03T14:45:30Z",
  "payload": {
    "ability_name": "FIREBALL",      // Ability identifier
    "class": "WIZARD",
    "ability_level": 1,              // Character level ability unlocked at
    "cooldown_remaining_s": 0,       // Turns left on cooldown before this use
    "mp_cost": 5,                    // Mana consumed
    "targets_hit": 1,
    "total_damage": 18,
    "floor_number": 6
  }
}
```

**Location**: `GameActivity.activateAbility()`  
**Frequency**: Per ability activation (combat only)

---

### 5. INVENTORY & PROGRESSION EVENTS

#### `inventory_changed`
Fired when inventory gains/loses items.

```json
{
  "event_type": "inventory_changed",
  "timestamp": "2026-03-03T14:50:00Z",
  "payload": {
    "change_type": "ITEM_GAINED",    // ITEM_GAINED, ITEM_LOST, GOLD_GAINED, GOLD_LOST
    "item_name": "POTION_RED",       // or "GOLD" / "PLATINUM"
    "quantity_change": 1,            // +1 or -1
    "new_total": 3,                  // Total quantity after change
    "source": "COMBAT_LOOT",         // Where item came from
    "floor_number": 5
  }
}
```

**Location**: `InventoryManager.addItem()`, `removeItem()`  
**Frequency**: Per inventory change

---

#### `level_up`
Fired when player character gains a level.

```json
{
  "event_type": "level_up",
  "timestamp": "2026-03-03T14:51:00Z",
  "payload": {
    "new_level": 5,
    "total_xp": 450,
    "xp_to_next": 600,
    "ability_unlocked": "VOLLEY",    // null if no new ability
    "stats_gained": {
      "hp": 5,
      "attack": 1,
      "defense": 0
    },
    "floor_number": 8
  }
}
```

**Location**: `GameActivity.awardXP()` when threshold crossed  
**Frequency**: Per level gain

---

#### `achievement_unlocked`
Fired when achievement is earned.

```json
{
  "event_type": "achievement_unlocked",
  "timestamp": "2026-03-03T14:52:00Z",
  "payload": {
    "achievement_id": "FIRST_BLOOD",
    "achievement_name": "First Blood",
    "trigger_condition": "FIRST_COMBAT_WIN",
    "floor_number": 2,
    "time_to_unlock_s": 180          // Time from run start to unlock
  }
}
```

**Location**: `AchievementManager.unlock()`  
**Frequency**: Per unique achievement earned

---

### 6. SHOP & PURCHASE EVENTS

#### `shop_appeared`
Fired when merchant visits floor.

```json
{
  "event_type": "shop_appeared",
  "timestamp": "2026-03-03T14:47:00Z",
  "payload": {
    "floor_number": 6,
    "merchant_class": "MERCHANT",    // Merchant type (future expansion)
    "items_offered": 4,
    "gold_available": 800,
    "platinum_available": 50
  }
}
```

**Location**: `GameActivity.generateShopFloor()` or merchant spawn  
**Frequency**: Per merchant visit

---

#### `shop_purchase`
Fired when player buys item from shop or premium store.

```json
{
  "event_type": "shop_purchase",
  "timestamp": "2026-03-03T14:48:00Z",
  "payload": {
    "store_type": "MERCHANT",        // MERCHANT or PREMIUM_STORE
    "item_name": "POTION_RED",
    "item_price_gold": 50,
    "item_price_platinum": 0,
    "currency_used": "GOLD",         // GOLD or PLATINUM
    "gold_after": 750,
    "platinum_after": 100,
    "floor_number": 6,
    "times_purchased_before": 0      // Total previous purchases of this item
  }
}
```

**Location**: `ShopActivity.purchaseItem()` or `PremiumStoreActivity.buyItem()`  
**Frequency**: Per purchase

---

### 7. SETTINGS & PREFERENCES

#### `settings_changed`
Fired when player modifies settings.

```json
{
  "event_type": "settings_changed",
  "timestamp": "2026-03-03T14:25:00Z",
  "payload": {
    "setting_name": "SOUND_ENABLED", // SOUND_ENABLED, VIBRATION_ENABLED, DIFFICULTY, COLORBLIND_MODE, etc.
    "old_value": true,
    "new_value": false
  }
}
```

**Location**: `SettingsActivity.saveSetting()`  
**Frequency**: Per setting change

---

### 8. SYSTEM HEALTH & ERRORS

#### `crash_detected`
Fired when app crash is detected on next launch.

```json
{
  "event_type": "crash_detected",
  "timestamp": "2026-03-03T15:05:00Z",
  "payload": {
    "exception_type": "NullPointerException",
    "exception_message": "Monster list null at floor 5",
    "stack_trace": "com.example.clickdungeon.GameActivity.spawnMonster(GameActivity.java:2559)",
    "save_state_recoverable": true,  // Was corrupted save involved?
    "last_known_screen": "GameActivity"
  }
}
```

**Location**: Global exception handler or `ClickDungeonApp.uncaughtExceptionHandler`  
**Frequency**: Per detected crash

---

#### `save_failed`
Fired when save operation fails.

```json
{
  "event_type": "save_failed",
  "timestamp": "2026-03-03T14:56:00Z",
  "payload": {
    "reason": "ENCRYPTION_ERROR",    // ENCRYPTION_ERROR, WRITE_ERROR, CHECKSUM_FAILURE, etc.
    "save_slot": 2,
    "save_size_bytes": 50000,
    "retry_count": 1,
    "recovery_attempted": true       // Did backup restore kick in?
  }
}
```

**Location**: `SaveManager.save()` error handler  
**Frequency**: Per failed save (hopefully rare!)

---

#### `performance_lag`
Fired when frame rate drops or animation jank detected.

```json
{
  "event_type": "performance_lag",
  "timestamp": "2026-03-03T14:59:00Z",
  "payload": {
    "average_fps": 45,               // Average FPS in last 5s
    "min_fps": 20,                   // Minimum FPS spike
    "jank_frame_count": 3,           // # frames > 16ms (60 FPS threshold)
    "location": "CombatAnimation",   // Where lag occurred
    "memory_used_mb": 180,
    "memory_available_mb": 220
  }
}
```

**Location**: Frame performance monitor or profiler integration  
**Frequency**: On detected lag (throttled to once per 10s)

---

### 9. BACKEND SYNC EVENTS (Connected Services / Remote Backend Only)

#### `cloud_sync_attempted`
Fired when cloud sync begins.

```json
{
  "event_type": "cloud_sync_attempted",
  "timestamp": "2026-03-03T14:52:00Z",
  "payload": {
    "sync_type": "SAVE_BACKUP",      // SAVE_BACKUP, CHALLENGE_FETCH, LEADERBOARD_UPDATE
    "direction": "UPLOAD",           // UPLOAD or DOWNLOAD
    "remote_config_version": "123"
  }
}
```

**Location**: `SaveService.syncSave()` or `CloudSyncWorker`  
**Frequency**: Per sync attempt

---

#### `cloud_sync_completed`
Fired when cloud sync finishes (success or failure).

```json
{
  "event_type": "cloud_sync_completed",
  "timestamp": "2026-03-03T14:52:30Z",
  "payload": {
    "sync_type": "SAVE_BACKUP",
    "status": "SUCCESS",             // SUCCESS, FAILURE, PARTIAL
    "duration_ms": 2340,
    "bytes_transferred": 45000,
    "error": null                    // null if success
  }
}
```

**Location**: `SaveService.syncSave()` callback  
**Frequency**: Per sync completion

---

## Event Logging & Storage

### Local Storage

Events are queued in memory and periodically written to SharedPreferences:

```json
{
  "pending_events": [
    { "event_type": "run_start", ... },
    { "event_type": "floor_reached", ... },
    ...
  ],
  "last_sync": "2026-03-03T15:00:00Z",
  "sync_enabled": true
}
```

**Cleanup**: Events older than 90 days are pruned annually.

### Backend Storage (Connected Services)

Events are sent to Firebase via:
- **Firestore collection**: `users/{user_id}/events/{event_id}`
- **BigQuery export**: For analytics aggregation (batch job, not real-time)

---

## Accessing Telemetry Events

### In-App View (QA)

**Settings** > **Telemetry** (if enabled in build variant):
- View recent events (last 100)
- Filter by event type or time range
- Export as JSON for analysis

### Backend Query (Analytics)

```sql
-- BigQuery: Count runs by class in last 7 days
SELECT 
  payload.class_selected as class,
  COUNT(*) as run_count
FROM `project.dataset.events`
WHERE event_type = 'run_start'
  AND timestamp >= TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY)
GROUP BY class
ORDER BY run_count DESC;
```

---

## Best Practices

### For Developers

1. **Always include context**:
   - `floor_number`, `terrain`, etc. for gameplay events
   - `duration_s` for duration-sensitive events
   - `error` field if operation failed

2. **Avoid PII**:
   - Never log usernames, emails, device serials
   - Use anonymized user_id (UUID)
   - Sanitize error messages (strip file paths, credentials)

3. **Keep payloads reasonable**:
   - <5 KB per event
   - Use sparse payloads (only include relevant fields)
   - Batch related events (e.g., `combat_turn` not `every_action`)

4. **Test event generation**:
   ```java
   @Test
   public void testCombatStartEventStructure() {
       telemetryManager.logCombatStarted(enemy, player);
       Event event = telemetryManager.getLastEvent();
       assertNotNull(event.payload.enemy_name);
       assertEquals("combat_started", event.event_type);
   }
   ```

### For QA/Analytics

1. **Monitor event volume**:
   - Setup alerts if `crash_detected` events spike
   - Track `run_start` vs `run_completed` ratio (drop-off indicator)

2. **Analyze retention**:
   - Plot `session_start` count over time
   - Calculate `session_duration_s` average

3. **Identify issues**:
   - Filter for `save_failed` events to spot persistence bugs
   - Track `combat_turn` durations to spot lag

---

## TODO

- [ ] Implement telemetry UI in Settings for QA
- [ ] Add BigQuery export pipeline (backend)
- [ ] Create analytics dashboard (Looker or similar)
- [ ] Add retention policy enforcement (90-day cleanup)
- [ ] Document event schema in OpenAPI/GraphQL

---

## References

- **Code**: [TelemetryManager.java](../app/src/main/java/com/example/clickdungeon/util/TelemetryManager.java)
- **Config**: `firebase.json`, `firebaserc` (backend)
- **Docs**: [ROADMAP.md - Monetization & Live Ops](ROADMAP.md#phase-7---monetization--live-ops)

