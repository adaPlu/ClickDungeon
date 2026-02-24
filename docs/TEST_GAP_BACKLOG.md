# Test Gap Backlog (Prioritized)

Audit basis: method-level heuristic scan (`main` method names not referenced in `app/src/test/java`), plus risk weighting for gameplay, persistence, and security.

Important: this is a prioritization backlog, not proof of zero coverage. Some methods may be indirectly covered through higher-level tests.

## Coverage Status (Updated 2026-02-15)

Current state after comparing backlog targets to `app/src/test/java`:

- `1` Complete: `GameActivitySaveIntegrationTest` covers `requestSave` coalescing, critical save immediacy, and `enqueueSave` metadata snapshot.
- `2` Complete: `GameActivityInteractionTest` covers `applyTrapDamage` mitigation/HP clamp and lethal defeat flow.
- `3` Complete: `GameActivityInteractionTest` covers `applyMonsterLoot` reward and key-side-effect behavior.
- `4` Complete: `ShopTransactionsTest` covers `maybeShowMerchant` eligible/ineligible cadence and no repeat trigger branch.
- `5` Complete: `SaveManagerTest` covers `restoreBackup`, `migrateSave`, and malformed blob parse/load handling.
- `6` Complete: `PersistedBlobStoreTest` covers checksum rejection, valid backup restore, and schema mismatch telemetry/notification path.
- `7` Complete: `SecurePreferencesTest` covers typed decode paths, malformed payload fallback, `getAll`, and `getStringSet` handling.
- `8` Complete: `GameActivityTileViewTest` covers `getMonsterSpriteResource` known mappings plus unknown/null fallback.
- `9` Complete: `GameActivityTileViewTest` covers `getPlayerSpriteSheetResource` class mapping plus fallback.
- `10` Complete: `ContinueActivityTest` covers schema mismatch dialog actions, overwrite confirmation, and new/continue intent extras.
- `11` Complete: `InventoryActivityTest` covers `toggleEquip` weapon/armor equip-unequip and non-equipable false path.
- `12` Complete: `InventoryActivityTest` covers `bindChangeLog` empty and populated ordering branches.
- `13` Complete: `ClassSelectionActivityTest` covers class selection preview wiring and selection button state.
- `14` Complete: `CombatDialogFragmentTest` covers `prepareAnimatedCombatants` and `updateAnimationFrames` fallback/frame-update behavior.
- `15` Complete: `SettingsManagerTest` covers audio diagnostics persistence and difficulty defense-scaling boundaries.
- `16` Complete: `SoundManagerTest` covers `pauseAll`, `resumeAll`, and `getMissingKeys` lifecycle/edge behavior.
- `17` Complete: `MonsterAnimationHelperTest` covers `defaultConfig` via null/unknown fallback mapping.
- `18` Complete: `OnboardingManagerTest` covers `getDifficultyLabel` branches for CASUAL/NORMAL/HARDCORE.
- `19` Complete: `ContinueActivityTest` covers `refreshSlotsUI` update path after slot state changes.
- `20` Complete: `InventoryActivityTest` covers `updateStatViews` label updates from profile values.

Summary: backlog items `1-20` are currently covered by deterministic unit/Robolectric tests.

## P0 - High Risk (do first)

1. Save queue/coalescing behavior
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:975` (`requestSave`)
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:1011` (`enqueueSave`)
- Why: Save correctness affects run recovery and slot integrity.
- Add tests in: `app/src/test/java/com/example/clickdungeon/GameActivitySaveIntegrationTest.java`
- Cases:
  - repeated `requestSave` calls coalesce/debounce as expected
  - critical save path persists immediately
  - metadata in queued save matches active floor/run state

2. Trap damage resolution safety
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:2791` (`applyTrapDamage`)
- Why: core HP/death flow and status interactions.
- Add tests in: `app/src/test/java/com/example/clickdungeon/GameActivityInteractionTest.java`
- Cases:
  - trap damage applies with/without mitigation items
  - lethal damage triggers defeat path
  - HP floor clamps and UI/state sync remain valid

3. Loot application correctness
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:2859` (`applyMonsterLoot`)
- Why: economy integrity (gold, keys, item rewards).
- Add tests in: `app/src/test/java/com/example/clickdungeon/GameActivityInteractionTest.java`
- Cases:
  - loot adds expected inventory/currency deltas
  - no negative/duplicate key side effects
  - reward handling for edge monster types

4. Merchant trigger cadence/eligibility
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:943` (`maybeShowMerchant`)
- Why: shop cadence and run progression logic.
- Add tests in: `app/src/test/java/com/example/clickdungeon/ui/ShopTransactionsTest.java`
- Cases:
  - eligible floors trigger visit with expected probability guard
  - ineligible floors do not trigger
  - state doesn’t re-trigger within same floor visit

5. Save restore and schema recovery
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/SaveManager.java:157` (`restoreBackup`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SaveManager.java:164` (`migrateSave`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SaveManager.java:226` (`parseBlob`)
- Why: corrupted-slot and migration reliability.
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/SaveManagerTest.java`
- Cases:
  - schema mismatch and migration path correctness
  - backup restore success/failure branches
  - malformed blob handling without crash

6. Blob integrity/HMAC enforcement
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/PersistedBlobStore.java:148` (`restoreBackup`)
- Target: `app/src/main/java/com/example/clickdungeon/util/PersistedBlobStore.java:159` (`isValid`)
- Target: `app/src/main/java/com/example/clickdungeon/util/PersistedBlobStore.java:180` (`computeHmac`)
- Why: anti-tamper and backup trust model.
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/PersistedBlobStoreTest.java`
- Cases:
  - checksum mismatch rejection
  - valid backup restore acceptance
  - schema mismatch notification path

7. SecurePreferences typed decode/crypto paths
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/SecurePreferences.java:89` (`encryptValue`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SecurePreferences.java:100` (`decryptValue`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SecurePreferences.java:126` (`getAll`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SecurePreferences.java:168` (`getStringSet`)
- Why: encrypted preference correctness across all primitive/set paths.
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/SecurePreferencesTest.java`
- Cases:
  - round-trip for primitives + string sets
  - malformed payload fallback behavior
  - `getAll` returns decoded typed values

## P1 - Medium Risk (next)

8. Monster sprite fallback mapping after portrait removal
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:1487` (`getMonsterSpriteResource`)
- Why: missing-resource regressions now map through sprite sheets only.
- Add tests in: `app/src/test/java/com/example/clickdungeon/GameActivityTileViewTest.java`
- Cases:
  - known monster types map to expected `*_sprite_sheet`
  - unknown/null type falls back to slime sheet

9. Player sprite fallback mapping
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/GameActivity.java:1561` (`getPlayerSpriteSheetResource`)
- Why: player rendering fallback when animation frame unavailable.
- Add tests in: `app/src/test/java/com/example/clickdungeon/GameActivityTileViewTest.java`
- Cases:
  - class-to-sheet mapping for KNIGHT/THIEF/WIZARD
  - null profile/class fallback

10. Continue flow edge cases
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/ContinueActivity.java:107` (`showSchemaMismatchDialog`)
- Target: `app/src/main/java/com/example/clickdungeon/ContinueActivity.java:164` (`startNewGame`)
- Target: `app/src/main/java/com/example/clickdungeon/ContinueActivity.java:173` (`continueGame`)
- Why: slot safety and player trust.
- Add tests in: `app/src/test/java/com/example/clickdungeon/ContinueActivityTest.java`
- Cases:
  - schema mismatch dialog action paths
  - overwrite confirmation behavior
  - intent extras for new/continue actions

11. Inventory equip toggle behavior
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/InventoryActivity.java:146` (`toggleEquip`)
- Why: equipment state correctness and stat calculation.
- Add tests in: `app/src/test/java/com/example/clickdungeon/InventoryActivityTest.java`
- Cases:
  - equip/unequip weapon and armor
  - non-equipable items return false
  - summary labels reflect toggles

12. Inventory change-log rendering
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/InventoryActivity.java:268` (`bindChangeLog`)
- Why: UX consistency called out in roadmap.
- Add tests in: `app/src/test/java/com/example/clickdungeon/InventoryActivityTest.java`
- Cases:
  - empty log hides section
  - non-empty log shows latest entries in order

13. Class selection behavior wiring
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/ClassSelectionActivity.java:106` (`selectClass`)
- Target: `app/src/main/java/com/example/clickdungeon/ClassSelectionActivity.java:115` (`updateClassPreview`)
- Target: `app/src/main/java/com/example/clickdungeon/ClassSelectionActivity.java:138` (`highlightSelection`)
- Why: early-run UX + class state correctness.
- Add tests in: `app/src/test/java/com/example/clickdungeon/ClassSelectionActivityTest.java`
- Cases:
  - selecting class updates preview drawable to sprite sheet
  - selected button disabled, others enabled

14. Combat fragment animation fallback paths
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/ui/CombatDialogFragment.java:700` (`prepareAnimatedCombatants`)
- Target: `app/src/main/java/com/example/clickdungeon/ui/CombatDialogFragment.java:749` (`updateAnimationFrames`)
- Why: runtime stability when animated assets fail/are null.
- Add tests in: `app/src/test/java/com/example/clickdungeon/ui/CombatDialogFragmentTest.java`
- Cases:
  - no animator -> placeholder sprite-sheet resource used
  - frame updates push bitmaps for both actors

15. Settings diagnostics/difficulty branch coverage
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/SettingsManager.java:84` (`setAudioDiagnosticsEnabled`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SettingsManager.java:155` (`Difficulty.scaleMonsterDefense`)
- Why: roadmap calls out diagnostics and difficulty tuning.
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/SettingsManagerTest.java`
- Cases:
  - audio diagnostics toggle persistence
  - defense scaling boundaries per difficulty mode

## P2 - Lower Risk / Backfill

16. Sound manager lifecycle coverage
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/SoundManager.java:232` (`pauseAll`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SoundManager.java:239` (`resumeAll`)
- Target: `app/src/main/java/com/example/clickdungeon/util/SoundManager.java:270` (`getMissingKeys`)
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/SoundManagerTest.java`

17. MonsterAnimationHelper default config branch
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/MonsterAnimationHelper.java:234` (`defaultConfig`)
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/MonsterAnimationHelperTest.java`

18. Onboarding difficulty label branch
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/util/OnboardingManager.java:63` (`getDifficultyLabel`)
- Add tests in: `app/src/test/java/com/example/clickdungeon/util/OnboardingManagerTest.java`

19. Continue slot refresh UI branch
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/ContinueActivity.java:182` (`refreshSlotsUI`)
- Add tests in: `app/src/test/java/com/example/clickdungeon/ContinueActivityTest.java`

20. Inventory stat view updater branch
- Status: `COMPLETE`
- Target: `app/src/main/java/com/example/clickdungeon/InventoryActivity.java:255` (`updateStatViews`)
- Add tests in: `app/src/test/java/com/example/clickdungeon/InventoryActivityTest.java`

---

## Suggested Execution Order

1. P0 items 1-3 (save/trap/loot)  
2. P0 items 5-7 (save-store + crypto validation)  
3. P1 items 8-14 (sprite/continue/UI flow hardening)  
4. P2 backfill for utilities and lifecycle branches

## Definition of Done for this backlog

- Each item has at least one deterministic Robolectric/unit test.
- Tests assert both success and at least one failure/edge branch.
- `./gradlew.bat test build` remains green after each batch.
