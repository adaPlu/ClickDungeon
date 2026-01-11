# ClickDungeon Performance Optimization Plan

This document outlines a phased approach to resolve the current UI lag and main-thread bottlenecks identified during the January 2026 audit.

## Phase 1: Grid Rendering Optimization (Target: Redundant Re-binding)
**Issue:** `renderGrid()` iterates 25 tiles and calls `bindTileView` on every change, causing massive UI thread overhead (Bitmap cache checks, animation re-instantiation).

- **Action 1.1:** Implement a "Dirty" flag system on the `Tile` model.
- **Action 1.2:** Refactor `GameActivity.renderGrid()` to only update tiles where `tile.isDirty()` is true.
- **Action 1.3:** Create a targeted `refreshTile(row, col)` method to be used by status effects and combat updates instead of a full grid refresh.

## Phase 2: Animation Loop Refinement (Target: Handler Overhead)
**Issue:** `gridAnimationRunnable` iterates all tiles every 120ms and performs expensive `getGlobalVisibleRect` visibility checks.

- **Action 2.1:** Maintain a `List<TileView>` of "Active Animated Tiles" that only includes revealed enemies and the player.
- **Action 2.2:** Update this list only when tiles are revealed or combat ends, removing the need to poll 25 views every tick.
- **Action 2.3:** Increase frame delay slightly (e.g., to 150ms) if performance remains tight on lower-end devices.

## Phase 3: State & Fragment Hygiene (Target: Bundle Serialization)
**Issue:** `CombatDialogFragment` passes `Bitmap` objects in its arguments, causing lag during configuration changes and "Activity Thread" blockages.

- **Action 3.1:** Refactor `CombatDialogFragment.newInstance()` to pass only the `monsterId` or `type` string.
- **Action 3.2:** Have the fragment fetch its own animation frames from `MonsterAnimationHelper` / `LruCache` upon creation.
- **Action 3.3:** Implement `onSaveInstanceState` properly in `GameActivity` to avoid redundant JSON serialization of the entire profile if only a few fields changed.

## Phase 4: Memory & Cache Tuning (Target: GC Thrashing)
**Issue:** `monsterBitmapCache` size logic may be causing frequent evictions and re-decoding.

- **Action 4.1:** Profile memory using Android Studio Profiler to determine peak heap usage during a 15-floor run.
- **Action 4.2:** Adjust `initBitmapCache` to use a more conservative percentage of available memory or a fixed size based on the current monster sprite-sheet pool.
- **Action 4.3:** Implement a "Pre-warm" phase for the current floor's monster types to ensure Bitmaps are ready before the grid is rendered.

---
**Status:** Planned (Awaiting implementation)
